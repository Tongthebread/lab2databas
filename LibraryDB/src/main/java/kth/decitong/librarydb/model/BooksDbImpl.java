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

/**
 * A mock implementation of the BooksDBInterface interface to demonstrate how to
 * use it together with the user interface.
 * <p>
 * Your implementation must access a real database.
 *
 * @author anderslm@kth.se
 */
public class BooksDbImpl implements BooksDbInterface {
    private MongoClient mongoClient;
    private MongoDatabase database;

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
            mongoClient = MongoClients.create(settings); // Assign to the class member
            this.database = mongoClient.getDatabase(databaseName); // Assign the database
            database.runCommand(new Document("ping", 1)); // Ping command
            System.out.println("Successfully connected to MongoDB!");
        } catch (MongoException e) {
            throw new BooksDbException("Error connecting to MongoDB: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() throws BooksDbException {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    @Override
    public ArrayList<Book> searchBooksByTitle(String title) throws BooksDbException {
        ArrayList<Book> matchingBooks = new ArrayList<>();

        try {
            // Query to find books with a title matching the search string
            Pattern titlePattern = Pattern.compile(title, Pattern.CASE_INSENSITIVE);
            FindIterable<Document> foundBooks = database.getCollection("books")
                    .find(Filters.regex("title", titlePattern));

            for (Document bookDoc : foundBooks) {
                Book book = documentToBook(bookDoc); // Convert Document to Book

                // Fetch and add authors to the book
                List<Integer> authorIds = bookDoc.getList("authors", Integer.class);
                for (int authorId : authorIds) {
                    Document authorDoc = database.getCollection("authors").find(Filters.eq("authorID", authorId)).first();
                    if (authorDoc != null) {
                        Author author = documentToAuthor(authorDoc); // Convert Document to Author
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
    @Override
    public ArrayList<Book> searchBooksByAuthor(String authorName) throws BooksDbException {
        ArrayList<Book> matchingBooks = new ArrayList<>();

        try {
            // First, find the authors matching the name
            Pattern namePattern = Pattern.compile(authorName, Pattern.CASE_INSENSITIVE);
            FindIterable<Document> foundAuthors = database.getCollection("authors")
                    .find(Filters.or(Filters.regex("firstName", namePattern), Filters.regex("lastName", namePattern)));

            List<Integer> authorIds = new ArrayList<>();
            for (Document author : foundAuthors) {
                authorIds.add(author.getInteger("authorID"));
            }

            // Then, find the books that have these author IDs
            if (!authorIds.isEmpty()) {
                FindIterable<Document> foundBooks = database.getCollection("books")
                        .find(Filters.in("authors", authorIds));

                // Convert documents to Book objects and add authors
                for (Document bookDoc : foundBooks) {
                    Book book = documentToBook(bookDoc); // Convert Document to Book

                    // Fetch and add authors to the book
                    List<Integer> bookAuthorIds = bookDoc.getList("authors", Integer.class);
                    for (int authorId : bookAuthorIds) {
                        Document authorDoc = database.getCollection("authors").find(Filters.eq("authorID", authorId)).first();
                        if (authorDoc != null) {
                            Author author = documentToAuthor(authorDoc); // Convert Document to Author
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
    @Override
    public ArrayList<Book> searchBooksByGenre(String genre) throws BooksDbException {
        ArrayList<Book> matchingBooks = new ArrayList<>();

        try {
            // Convert the input genre to uppercase to match the database format
            String uppercaseGenre = genre.toUpperCase();

            // Query to find books with the specified uppercase genre
            FindIterable<Document> foundBooks = database.getCollection("books")
                    .find(Filters.eq("genre", uppercaseGenre));

            for (Document bookDoc : foundBooks) {
                Book book = documentToBook(bookDoc); // Convert Document to Book

                // Fetch and add authors to the book
                List<Integer> authorIds = bookDoc.getList("authors", Integer.class);
                for (int authorId : authorIds) {
                    Document authorDoc = database.getCollection("authors").find(Filters.eq("authorID", authorId)).first();
                    if (authorDoc != null) {
                        Author author = documentToAuthor(authorDoc); // Convert Document to Author
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
    @Override
    public ArrayList<Book> searchBooksByRating(int rating) throws BooksDbException {
        ArrayList<Book> matchingBooks = new ArrayList<>();

        try {
            // Query to find books with the specified rating
            FindIterable<Document> foundBooks = database.getCollection("books")
                    .find(Filters.eq("rating", rating));

            for (Document bookDoc : foundBooks) {
                Book book = documentToBook(bookDoc); // Convert Document to Book

                // Fetch and add authors to the book
                List<Integer> authorIds = bookDoc.getList("authors", Integer.class);
                for (int authorId : authorIds) {
                    Document authorDoc = database.getCollection("authors").find(Filters.eq("authorID", authorId)).first();
                    if (authorDoc != null) {
                        Author author = documentToAuthor(authorDoc); // Convert Document to Author
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



    @Override
    public ArrayList<Book> searchBooksByISBN(String ISBN) throws BooksDbException {
        ArrayList<Book> matchingBooks = new ArrayList<>();

        try {
            // Query to find books with the specified ISBN
            FindIterable<Document> foundBooks = database.getCollection("books")
                    .find(Filters.eq("isbn", ISBN));

            for (Document bookDoc : foundBooks) {
                Book book = documentToBook(bookDoc); // Convert Document to Book

                // Fetch and add authors to the book
                List<Integer> authorIds = bookDoc.getList("authors", Integer.class);
                for (int authorId : authorIds) {
                    Document authorDoc = database.getCollection("authors").find(Filters.eq("authorID", authorId)).first();
                    if (authorDoc != null) {
                        Author author = documentToAuthor(authorDoc); // Convert Document to Author
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


    @Override
    public void addAuthorToBook(Author author, Book book) throws BooksDbException {
        if (book == null || author == null) {
            throw new IllegalArgumentException("Book and Author cannot be null");
        }

        try {
            // Create a filter to find the book by its ID
            Document bookFilter = new Document("bookId", book.getBookId());

            // Create an update operation to add the author's ID to the book's authors list
            Document updateOperation = new Document("$addToSet", new Document("authors", author.getAuthorID()));

            // Perform the update operation on the books collection
            UpdateResult updateResult = database.getCollection("books").updateOne(bookFilter, updateOperation);

            if (updateResult.getMatchedCount() == 0) {
                throw new BooksDbException("No book found with bookId: " + book.getBookId());
            }
        } catch (MongoException e) {
            throw new BooksDbException("Error adding author to book in MongoDB: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Author> getAuthorsForBook(int bookID) throws BooksDbException {
        List<Author> authors = new ArrayList<>();

        try {
            // Find the book document by its ID
            Document book = database.getCollection("books").find(new Document("bookId", bookID)).first();
            if (book == null) {
                throw new BooksDbException("No book found with bookId: " + bookID);
            }

            // Extract the list of author IDs from the book document
            List<Integer> authorIds = book.getList("authors", Integer.class);

            // For each author ID, find the author document and create an Author object
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

        // Assuming the document contains an array of author IDs
        List<Integer> authorIDs = doc.getList("authorIDs", Integer.class);
        if (authorIDs != null) {
            for (int authorID : authorIDs) {
                // Here you would fetch the author details based on the authorID
                // This is a simplified example. In a real application, you might want to optimize this.
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

        // Create a new Author object
        Author author = new Author(authorID, firstName, lastName, birthDate);

        return author;
    }
    private Author getAuthorById(int authorId) {
        // Fetch the author document from the database
        Document authorDoc = database.getCollection("authors").find(Filters.eq("authorID", authorId)).first();
        if (authorDoc != null) {
            return documentToAuthor(authorDoc);
        }
        return null;
    }
}

