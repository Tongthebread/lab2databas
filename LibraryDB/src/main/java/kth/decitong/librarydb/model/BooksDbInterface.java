package kth.decitong.librarydb.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This interface declares methods for querying a Books database.
 * Different implementations of this interface handles the connection and
 * queries to a specific DBMS and database, for example a MySQL or a MongoDB
 * database.
 *
 * NB! The methods in the implementation must catch the SQL/MongoDBExceptions thrown
 * by the underlying driver, wrap in a BooksDbException and then re-throw the latter
 * exception. This way the interface is the same for both implementations, because the
 * exception type in the method signatures is the same. More info in BooksDbException.java.
 * 
 * @author anderslm@kth.se
 */
public interface BooksDbInterface {
    
    /**
     * Connect to the database.
     *
     * @param database name of the real database
     */
    public void connect(String database) throws BooksDbException;
    public void disconnect() throws BooksDbException;
    public List<Book> searchBooksByTitle(String title) throws BooksDbException;
    ArrayList<Book> searchBooksByAuthor(String authorName) throws BooksDbException;
    ArrayList<Book> searchBooksByGenre(String genre) throws BooksDbException;
    ArrayList<Book> searchBooksByRating(int rating) throws BooksDbException;
    ArrayList<Book> searchBooksByISBN(String ISBN) throws BooksDbException;

    void deleteBook(int bookID) throws BooksDbException;
    void addBook(Book book) throws BooksDbException;
    void addAuthor(Author author) throws BooksDbException;
    void addAuthorToBook(Author author, Book book) throws BooksDbException;
    List<Author> getAuthorsForBook(int bookID) throws BooksDbException;
    List<Author> getAllAuthors() throws BooksDbException;
}
