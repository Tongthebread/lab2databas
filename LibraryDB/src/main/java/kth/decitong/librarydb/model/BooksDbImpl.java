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
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<Book> searchBooksByTitle(String title) throws BooksDbException {
        List<Book> books = new ArrayList<>();
        try {
            // Search for books with a title that matches the provided string
            FindIterable<Document> iterable = database.getCollection("books")
                    .find(Filters.eq("title", title));

            for (Document doc : iterable) {
                books.add(documentToBook(doc));
            }
        } catch (MongoException e) {
            throw new BooksDbException("Error searching books by title: " + e.getMessage(), e);
        }
        return books;
    }

    @Override
    public ArrayList<Book> searchBooksByAuthor(String authorName) throws BooksDbException {
        ArrayList<Book> books = new ArrayList<>();
        try {
            // Split the author name into first and last names for searching
            String[] nameParts = authorName.split(" ");
            String firstName = nameParts[0];
            String lastName = (nameParts.length > 1) ? nameParts[1] : "";

            // Find the author's documents
            FindIterable<Document> authors = database.getCollection("authors")
                    .find(Filters.and(Filters.eq("firstName", firstName), Filters.eq("lastName", lastName)));

            for (Document authorDoc : authors) {
                int authorID = authorDoc.getInteger("authorID");

                // Find books written by this author
                FindIterable<Document> bookDocs = database.getCollection("books")
                        .find(Filters.eq("authorIDs", authorID));

                for (Document bookDoc : bookDocs) {
                    books.add(documentToBook(bookDoc));
                }
            }
        } catch (MongoException e) {
            throw new BooksDbException("Error searching books by author: " + e.getMessage(), e);
        }
        return books;
    }

    @Override
    public ArrayList<Book> searchBooksByGenre(String genre) throws BooksDbException {
        ArrayList<Book> books = new ArrayList<>();
        try {
            // Convert the genre string to an enum (assuming Genre is an enum in your model)
            Genre genreEnum;
            try {
                genreEnum = Genre.valueOf(genre.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BooksDbException("Invalid genre: " + genre, e);
            }

            // Search for books with the specified genre
            FindIterable<Document> iterable = database.getCollection("books")
                    .find(Filters.eq("genre", genreEnum.toString()));

            for (Document doc : iterable) {
                books.add(documentToBook(doc));
            }
        } catch (MongoException e) {
            throw new BooksDbException("Error searching books by genre: " + e.getMessage(), e);
        }
        return books;
    }

    @Override
    public ArrayList<Book> searchBooksByRating(int rating) throws BooksDbException {
        ArrayList<Book> books = new ArrayList<>();
        try {
            // Validate the rating
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5.");
            }

            // Search for books with the specified rating
            FindIterable<Document> iterable = database.getCollection("books")
                    .find(Filters.eq("rating", rating));

            for (Document doc : iterable) {
                books.add(documentToBook(doc));
            }
        } catch (IllegalArgumentException e) {
            throw new BooksDbException("Invalid rating: " + e.getMessage(), e);
        } catch (MongoException e) {
            throw new BooksDbException("Error searching books by rating: " + e.getMessage(), e);
        }
        return books;
    }

    @Override
    public ArrayList<Book> searchBooksByISBN(String ISBN) throws BooksDbException {
        ArrayList<Book> books = new ArrayList<>();
        try {
            // Search for books with the specified ISBN
            FindIterable<Document> iterable = database.getCollection("books")
                    .find(Filters.eq("isbn", ISBN));

            for (Document doc : iterable) {
                books.add(documentToBook(doc));
            }
        } catch (MongoException e) {
            throw new BooksDbException("Error searching books by ISBN: " + e.getMessage(), e);
        }
        return books;
    }

    @Override
    public void deleteBook(int bookID) throws BooksDbException {
        try {
            // Delete the book document from the 'books' collection based on bookId
            DeleteResult result = database.getCollection("books")
                    .deleteOne(Filters.eq("bookId", bookID));

            if (result.getDeletedCount() == 0) {
                throw new BooksDbException("Book with ID " + bookID + "not found in database");
            }
        } catch (MongoException e) {
            throw new BooksDbException("Error deleting book from database: " + e.getMessage(), e);
        }
    }

    @Override
    public void addBook(Book book) throws BooksDbException {
        try {
            Document bookDoc = new Document()
                    .append("bookID", book.getBookId())
                    .append("isbn", book.getIsbn())
                    .append("title", book.getTitle())
                    .append("published", book.getPublished()) // assuming getPublished returns a Date in ISO 8601 format
                    .append("rating", book.getRating())
                    .append("genre", book.getGenre().toString())
                    .append("authorIDs", book.getAuthors().stream().map(Author::getAuthorID).collect(Collectors.toList()));

            database.getCollection("books").insertOne(bookDoc);
        } catch (MongoException e) {
            throw new BooksDbException("Error adding book to database: " + e.getMessage(), e);
        }
    }

    @Override
    public void addAuthor(Author author) throws BooksDbException {
        try {
            Document authorDoc = new Document()
                    .append("authorID", author.getAuthorID())
                    .append("firstName", author.getFirstName())
                    .append("lastName", author.getLastName())
                    .append("birthDate", author.getBirthDate()) // assuming getBirthDate returns a Date in ISO 8601 format
                    .append("bookIDs", new ArrayList<>()); // initially empty, books can be added later

            database.getCollection("authors").insertOne(authorDoc);
        } catch (MongoException e) {
            throw new BooksDbException("Error adding author to database: " + e.getMessage(), e);
        }
    }

    @Override
    public void addAuthorToBook(Author author, Book book) throws BooksDbException {
        try {
            // Add author's ID to the book's list of authors
            UpdateResult updateBookResult = database.getCollection("books")
                    .updateOne(Filters.eq("bookID", book.getBookId()),
                            Updates.addToSet("authorIDs", author.getAuthorID()));

            // Add book's ID to the author's list of books
            UpdateResult updateAuthorResult = database.getCollection("authors")
                    .updateOne(Filters.eq("authorID", author.getAuthorID()),
                            Updates.addToSet("bookIDs", book.getBookId()));
        } catch (MongoException e) {
            throw new BooksDbException("Error adding author to book in database: " + e.getMessage(), e);
        }
    }


    @Override
    public List<Author> getAuthorsForBook(int bookID) throws BooksDbException {
        List<Author> authors = new ArrayList<>();
        try {
            // Find the book document by its bookId
            Document bookDoc = database.getCollection("books").find(Filters.eq("bookId", bookID)).first();
            if (bookDoc == null) {
                throw new BooksDbException("Book with ID " + bookID + " not found.");
            }

            // Get the list of authorIDs from the book document
            List<Integer> authorIDs = bookDoc.getList("authorIDs", Integer.class);

            for (int authorID : authorIDs) {
                // Fetch each author by their authorID and add to the list
                Document authorDoc = database.getCollection("authors").find(Filters.eq("authorID", authorID)).first();
                if (authorDoc != null) {
                    authors.add(documentToAuthor(authorDoc));
                }
            }
        } catch (MongoException e) {
            throw new BooksDbException("Error retrieving authors for book: " + e.getMessage(), e);
        }
        return authors;
    }

    @Override
    public List<Author> getAllAuthors() throws BooksDbException {
            List<Author> authors = new ArrayList<>();
            try {
                // Retrieve all documents from the 'authors' collection
                FindIterable<Document> iterable = database.getCollection("authors").find();
                for (Document doc : iterable) {
                    authors.add(documentToAuthor(doc));
                }
            } catch (MongoException e) {
                throw new BooksDbException("Error retrieving all authors: " + e.getMessage(), e);
            }
            return authors;
    }
    private Author documentToAuthor(Document doc) {
        // Convert a MongoDB document to an Author object
        // This is an example and might need adjustments based on your actual MongoDB document structure
        int authorID = doc.getInteger("authorID");
        String firstName = doc.getString("firstName");
        String lastName = doc.getString("lastName");
        Date birthDate = doc.getDate("birthDate");

        Author author = new Author(authorID, firstName, lastName, birthDate);
        // Add books and other necessary fields if required
        return author;
    }
    private Book documentToBook(Document doc) {
        // Convert a MongoDB document to a Book object
        // This is an example and might need adjustments based on your actual MongoDB document structure
        int bookId = doc.getInteger("bookId");
        String isbn = doc.getString("isbn");
        String title = doc.getString("title");
        Date published = doc.getDate("published");
        int rating = doc.getInteger("rating");
        Genre genre = Genre.valueOf(doc.getString("genre")); // Assuming Genre is an enum

        Book book = new Book(bookId, isbn, title, published, rating, genre);
        // Handle authors and other necessary fields
        return book;
    }
}
