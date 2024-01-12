package kth.decitong.librarydb.model;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class BooksDbImpl implements BooksDbInterface {
    private MongoClient mongoClient;
    private MongoDatabase database;

    /**
     * Establishes a connection to a MongoDB database using the specified database name.
     * This method configures and creates a MongoDB client instance with specific settings
     * such as the server API version and connection string. It then connects to the MongoDB
     * server, selects the database, and performs a simple 'ping' command to ensure
     * connectivity.
     * @param databaseName name of the real database
     * @throws BooksDbException if error to connect to database.
     */
    @Override
    public void connect(String databaseName) throws BooksDbException {
        String connectionString = "mongodb://localhost:27017";

        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();

        try {
            mongoClient = MongoClients.create(settings);
            this.database = mongoClient.getDatabase(databaseName);
            database.runCommand(new Document("ping", 1));
        } catch (MongoException e) {
            throw new BooksDbException("Error connecting to MongoDB: " + e.getMessage(), e);
        }
    }

    /**
     * Closes the connection to the MongoDB database.
     * @throws BooksDbException if error do disconnect to database.
     */
    @Override
    public void disconnect() throws BooksDbException {
        if (mongoClient != null) {
            mongoClient.close();
        }
        else{
            throw new BooksDbException("Error disconnecting to MongoDB. ");
        }
    }

    /**
     * Searches for books in the MongoDB database by their title. This method utilizes
     * a case-insensitive regular expression to match the given title with book titles
     * stored in the database. It then converts each matching MongoDB document into a
     * {@link Book} object and fetches associated authors for each book.
     * @param title of Book
     * @return list of books matching the searched title
     * @throws BooksDbException if an error to find matching string
     */
    @Override
    public ArrayList<Book> searchBooksByTitle(String title) throws BooksDbException {
        ArrayList<Book> matchingBooks = new ArrayList<>();

        try {
            Pattern titlePattern = Pattern.compile(title, Pattern.CASE_INSENSITIVE);
            FindIterable<Document> foundBooks = database.getCollection("books")
                    .find(Filters.regex("title", titlePattern));

            for (Document bookDoc : foundBooks) {
                Book book = documentToBook(bookDoc);
                List<Integer> authorIds = bookDoc.getList("authors", Integer.class);
                for (int authorId : authorIds) {
                    Document authorDoc = database.getCollection("authors").find(Filters.eq("authorID", authorId)).first();
                    if (authorDoc != null) {
                        Author author = documentToAuthor(authorDoc);
                        book.addAuthors(author);
                    }
                }
                matchingBooks.add(book);
            }
        } catch (MongoException e) {
            throw new BooksDbException("Error searching for books by title in MongoDB: " + e.getMessage(), e);
        }

        return matchingBooks;
    }

    /**
     * Searches for books in the MongoDB database by a specified author's name. This method
     * uses a case-insensitive regular expression to match the given author's name (either
     * first or last name) with author names stored in the database. It retrieves books
     * associated with any matching author and constructs a list of {@link Book} objects,
     * including details about their authors.
     * @param authorName name of the author
     * @return list of books matching the searched author.
     * @throws BooksDbException if an error to find matching string
     */
    @Override
    public ArrayList<Book> searchBooksByAuthor(String authorName) throws BooksDbException {
        ArrayList<Book> matchingBooks = new ArrayList<>();

        try {
            Pattern namePattern = Pattern.compile(authorName, Pattern.CASE_INSENSITIVE);
            FindIterable<Document> foundAuthors = database.getCollection("authors")
                    .find(Filters.or(Filters.regex("firstName", namePattern),
                            Filters.regex("lastName", namePattern)));

            List<Integer> authorIds = new ArrayList<>();
            for (Document author : foundAuthors) {
                authorIds.add(author.getInteger("authorID"));
            }

            if (!authorIds.isEmpty()) {
                FindIterable<Document> foundBooks = database.getCollection("books")
                        .find(Filters.in("authors", authorIds));

                for (Document bookDoc : foundBooks) {
                    Book book = documentToBook(bookDoc);
                    List<Integer> bookAuthorIds = bookDoc.getList("authors", Integer.class);
                    for (int authorId : bookAuthorIds) {
                        Document authorDoc = database.getCollection("authors").find(Filters.eq("authorID", authorId)).first();
                        if (authorDoc != null) {
                            Author author = documentToAuthor(authorDoc);
                            book.addAuthors(author);
                        }
                    }
                    matchingBooks.add(book);
                }
            }
        } catch (MongoException e) {
            throw new BooksDbException("Error searching for books by author in MongoDB: " + e.getMessage(), e);
        }

        return matchingBooks;
    }

    /**
     * Searches for books in the MongoDB database by a specified genre. This method
     * performs a case-insensitive search by converting the provided genre string to
     * uppercase and matches it against the 'genre' field in the database. It retrieves
     * all books that fall under the specified genre and constructs a list of {@link Book}
     * objects, including details about their authors.
     * @param genre of the book
     * @return list of books matching the searched genre.
     * @throws BooksDbException if an error to find matching string
     */
    @Override
    public ArrayList<Book> searchBooksByGenre(String genre) throws BooksDbException {
        ArrayList<Book> matchingBooks = new ArrayList<>();

        try {
            String uppercaseGenre = genre.toUpperCase();
            FindIterable<Document> foundBooks = database.getCollection("books")
                    .find(Filters.eq("genre", uppercaseGenre));

            for (Document bookDoc : foundBooks) {
                Book book = documentToBook(bookDoc);
                List<Integer> authorIds = bookDoc.getList("authors", Integer.class);
                for (int authorId : authorIds) {
                    Document authorDoc = database.getCollection("authors").find(Filters.eq("authorID", authorId)).first();
                    if (authorDoc != null) {
                        Author author = documentToAuthor(authorDoc);
                        book.addAuthors(author);
                    }
                }
                matchingBooks.add(book);
            }
        } catch (MongoException e) {
            throw new BooksDbException("Error searching for books by genre in MongoDB: " + e.getMessage(), e);
        }

        return matchingBooks;
    }

    /**
     * Searches for books in the MongoDB database by a specific rating. This method
     * finds books that have a rating field exactly matching the provided integer rating.
     * It retrieves the relevant books and constructs an {@link ArrayList} of {@link Book}
     * objects, including details about their authors. Each book in the resulting list
     * will have the specified rating.
     * @param rating of the book 1-5.
     * @return list of books matching the searched rating
     * @throws BooksDbException if error to search for book.
     */
    @Override
    public ArrayList<Book> searchBooksByRating(int rating) throws BooksDbException {
        ArrayList<Book> matchingBooks = new ArrayList<>();

        try {
            FindIterable<Document> foundBooks = database.getCollection("books")
                    .find(Filters.eq("rating", rating));

            for (Document bookDoc : foundBooks) {
                Book book = documentToBook(bookDoc);
                List<Integer> authorIds = bookDoc.getList("authors", Integer.class);
                for (int authorId : authorIds) {
                    Document authorDoc = database.getCollection("authors").find(Filters.eq("authorID", authorId)).first();
                    if (authorDoc != null) {
                        Author author = documentToAuthor(authorDoc);
                        book.addAuthors(author);
                    }
                }

                matchingBooks.add(book);
            }
        } catch (MongoException e) {
            throw new BooksDbException("Error searching for books by rating in MongoDB: " + e.getMessage(), e);
        }

        return matchingBooks;
    }

    /**
     * Searches for books in the MongoDB database by a specific ISBN.
     * This method looks for books that have an 'isbn' field exactly matching the provided ISBN string.
     * It retrieves these books and constructs an {@link ArrayList} of {@link Book} objects, including
     * details about their authors. Each book in the resulting list will have the ISBN that was searched for.
     * @param ISBN of the book
     * @return list of books matching the searched isbn
     * @throws BooksDbException if error searching for book.
     */
    @Override
    public ArrayList<Book> searchBooksByISBN(String ISBN) throws BooksDbException {
        ArrayList<Book> matchingBooks = new ArrayList<>();

        try {
            FindIterable<Document> foundBooks = database.getCollection("books")
                    .find(Filters.eq("isbn", ISBN));

            for (Document bookDoc : foundBooks) {
                Book book = documentToBook(bookDoc);
                List<Integer> authorIds = bookDoc.getList("authors", Integer.class);
                for (int authorId : authorIds) {
                    Document authorDoc = database.getCollection("authors").find(Filters.eq("authorID", authorId)).first();
                    if (authorDoc != null) {
                        Author author = documentToAuthor(authorDoc);
                        book.addAuthors(author);
                    }
                }
                matchingBooks.add(book);
            }
        } catch (MongoException e) {
            throw new BooksDbException("Error searching for books by ISBN in MongoDB: " + e.getMessage(), e);
        }
        return matchingBooks;
    }

    /**
     * Deletes a book from the MongoDB database based on the provided book ID. This method
     * attempts to remove a single book document from the 'books' collection that matches
     * the given bookId.
     * @param bookId id number of the book
     * @throws BooksDbException if error deleting book from database or if no book was found.
     */
    @Override
    public void deleteBook(int bookId) throws BooksDbException {
        try {
            DeleteResult deleteResult = database.getCollection("books").deleteOne(new Document("bookId", bookId));
            if (deleteResult.getDeletedCount() == 0) {
                throw new BooksDbException("No book found with bookId: " + bookId);
            }
        } catch (MongoException e) {
            throw new BooksDbException("Error deleting book from MongoDB: " + e.getMessage(), e);
        }
    }

    /**
     * Adds a new book to the MongoDB database. This method converts a {@link Book} object
     * into a MongoDB document and inserts it into the 'books' collection. The book information
     * includes book ID, ISBN, title, publication date, rating, and genre.
     * @param book object
     * @throws BooksDbException if error adding book to database.
     */
    @Override
    public void addBook(Book book) throws BooksDbException {
        try {
            Document bookDocument = new Document("bookId", book.getBookId())
                    .append("isbn", book.getIsbn())
                    .append("title", book.getTitle())
                    .append("published", book.getPublished())
                    .append("rating", book.getRating())
                    .append("genre", book.getGenre().toString());

            database.getCollection("books").insertOne(bookDocument);
        } catch (MongoException e) {
            throw new BooksDbException("Error adding book to MongoDB: " + e.getMessage(), e);
        }
    }

    /**
     * Adds a new author to the MongoDB database. This method converts an {@link Author} object
     * into a MongoDB document and inserts it into the 'authors' collection. The author's
     * information includes their unique ID, first name, last name, and birth date.
     * @param author object
     * @throws BooksDbException if error adding author to database.
     */
    @Override
    public void addAuthor(Author author) throws BooksDbException {
        try {
            Document authorDocument = new Document("authorID", author.getAuthorID())
                    .append("firstName", author.getFirstName())
                    .append("lastName", author.getLastName())
                    .append("birthDate", author.getBirthDate());
            database.getCollection("authors").insertOne(authorDocument);
        } catch (MongoException e) {
            throw new BooksDbException("Error adding author to MongoDB: " + e.getMessage(), e);
        }
    }

    /**
     * Adds an author to a specified book in the MongoDB database. This method associates
     * an {@link Author} with a {@link Book} by adding the author's ID to the book's
     * 'authors' field. It performs a check to ensure neither the book nor the author is null.
     * @param author object
     * @param book object
     * @throws BooksDbException If there is an error in updating the book document in MongoDB,
     * such as if no book is found with the given ID, or if there's a MongoDB execution issue.
     */
    @Override
    public void addAuthorToBook(Author author, Book book) throws BooksDbException {
        if (book == null || author == null) {
            throw new IllegalArgumentException("Book and Author cannot be null");
        }

        try {
            Document bookFilter = new Document("bookId", book.getBookId());
            Document updateOperation = new Document("$addToSet", new Document("authors", author.getAuthorID()));
            UpdateResult updateResult = database.getCollection("books").updateOne(bookFilter, updateOperation);

            if (updateResult.getMatchedCount() == 0) {
                throw new BooksDbException("No book found with bookId: " + book.getBookId());
            }
        } catch (MongoException e) {
            throw new BooksDbException("Error adding author to book in MongoDB: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a list of authors associated with a specific book from the MongoDB database.
     * This method searches for a book by its unique ID and then fetches the authors related
     * to that book using the authors' IDs stored in the book's document. It constructs and
     * returns a list of {@link Author} objects.
     * @param bookID of the book.
     * @return list of Authors associated to the book's ID.
     * @throws BooksDbException If no book is found with the provided ID or if there is an
     *  error during the retrieval process from MongoDB.
     */
    @Override
    public List<Author> getAuthorsForBook(int bookID) throws BooksDbException {
        List<Author> authors = new ArrayList<>();

        try {
            Document book = database.getCollection("books").find(new Document("bookId", bookID)).first();
            if (book == null) {
                throw new BooksDbException("No book found with bookId: " + bookID);
            }

            List<Integer> authorIds = book.getList("authors", Integer.class);

            for (Integer authorId : authorIds) {
                Document authorDoc = database.getCollection("authors").find(new Document("authorID", authorId)).first();
                if (authorDoc != null) {
                    int authorID = authorDoc.getInteger("authorID");
                    String firstName = authorDoc.getString("firstName");
                    String lastName = authorDoc.getString("lastName");
                    Date birthDate = authorDoc.getDate("birthDate");

                    Author author = new Author(authorID, firstName, lastName, birthDate);
                    authors.add(author);
                }
            }
        } catch (MongoException e) {
            throw new BooksDbException("Error retrieving authors for book from MongoDB: " + e.getMessage(), e);
        }
        return authors;
    }

    /**
     * Retrieves a list of all authors from the MongoDB database. This method queries the
     * 'authors' collection and constructs a list of {@link Author} objects based on the
     * documents found. Each author's ID, first name, last name, and birth date are extracted
     * from the MongoDB documents and used to create the {@link Author} objects.
     * @return A list of {@link Author} objects representing all authors in the database.
     * @throws BooksDbException If there is an error in retrieving author data from MongoDB.
     */
    @Override
    public List<Author> getAllAuthors() throws BooksDbException {
        List<Author> authors = new ArrayList<>();

        try {
            FindIterable<Document> authorDocuments = database.getCollection("authors").find();

            for (Document doc : authorDocuments) {
                int authorID = doc.getInteger("authorID");
                String firstName = doc.getString("firstName");
                String lastName = doc.getString("lastName");
                Date birthDate = doc.getDate("birthDate");

                Author author = new Author(authorID, firstName, lastName, birthDate);
                authors.add(author);
            }
        } catch (MongoException e) {
            throw new BooksDbException("Error retrieving authors from MongoDB: " + e.getMessage(), e);
        }

        return authors;
    }

    private Book documentToBook(Document doc) {
        int bookId = doc.getInteger("bookId");
        String isbn = doc.getString("isbn");
        String bookTitle = doc.getString("title");
        Date published = doc.getDate("published");
        int rating = doc.getInteger("rating");
        Genre genre = Genre.valueOf(doc.getString("genre").toUpperCase());

        Book book = new Book(bookId, isbn, bookTitle, published, rating, genre);

        List<Integer> authorIDs = doc.getList("authorIDs", Integer.class);
        if (authorIDs != null) {
            for (int authorID : authorIDs) {
                Author author = getAuthorById(authorID);
                if (author != null) {
                    book.addAuthors(author);
                }
            }
        }

        return book;
    }

    private Author documentToAuthor(Document doc) {
        int authorID = doc.getInteger("authorID");
        String firstName = doc.getString("firstName");
        String lastName = doc.getString("lastName");
        Date birthDate = doc.getDate("birthDate");

	    return new Author(authorID, firstName, lastName, birthDate);
    }

    private Author getAuthorById(int authorId) {
        Document authorDoc = database.getCollection("authors").find(Filters.eq("authorID", authorId)).first();
        if (authorDoc != null) {
            return documentToAuthor(authorDoc);
        }
        return null;
    }
}

