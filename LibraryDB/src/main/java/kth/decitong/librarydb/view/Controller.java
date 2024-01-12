package kth.decitong.librarydb.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import kth.decitong.librarydb.model.Author;
import kth.decitong.librarydb.model.Book;
import kth.decitong.librarydb.model.BooksDbInterface;
import kth.decitong.librarydb.model.SearchMode;

import java.util.List;

import static javafx.scene.control.Alert.AlertType.*;

/**
 * The controller is responsible for handling user requests and update the view
 * (and in some cases the model).
 *
 * @author anderslm@kth.se
 */
public class Controller {

    private static BooksPane booksView;
    private static BooksDbInterface booksDb;

    public Controller(BooksDbInterface booksDb, BooksPane booksView) {
        Controller.booksDb = booksDb;
        Controller.booksView = booksView;
    }

    public static void connect() {
        new Thread(() -> {
            try {
                booksDb.connect("db_library");
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Connected to database successfully", Alert.AlertType.INFORMATION));
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Failed to connect to database: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }


    public static void disconnect() {
        new Thread(() -> {
            try {
                booksDb.disconnect();
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Disconnected from database.", INFORMATION));
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Error disconnecting from database: " + e.getMessage(), ERROR));
            }
        }).start();
    }


    public static void addBook(Book book) {
        new Thread(() -> {
            try {
                booksDb.addBook(book);
                for (Author author : book.getAuthors()) {
                    booksDb.addAuthorToBook(author, book);
                }
                List<Author> fetchedAuthors = booksDb.getAuthorsForBook(book.getBookId());
                book.getAuthors().clear();
                book.getAuthors().addAll(fetchedAuthors);

                Platform.runLater(() ->
                        booksView.showAlertAndWait("Book and authors added successfully", INFORMATION));
            } catch (Exception e){
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Error adding book and authors to database", ERROR));
            }
        }).start();
    }

    public static void addAuthor(Author author){
        new Thread(() -> {
            try {
                booksDb.addAuthor(author);
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Author added successfully.", INFORMATION));
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Error adding author to database", ERROR));
            }
        }).start();
    }


    public static void deleteBook(int bookId) {
        new Thread(() -> {
            try {
                booksDb.deleteBook(bookId);
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Book removed successfully.", Alert.AlertType.INFORMATION));
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Error removing book from database", Alert.AlertType.ERROR));
            }
        }).start();
    }


    public static void getAllAuthors(TableView<Author> authorTable) {
        new Thread(() -> {
            try {
                System.out.print("fetching");
                List<Author> authors = booksDb.getAllAuthors();
                Platform.runLater(() ->
                        authorTable.setItems(FXCollections.observableArrayList(authors)));
                System.out.print("fetched");
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Error fetching authors from database", ERROR));
            }
        }).start();
    }


    protected void searchBooksByISBN(String isbn) {
        new Thread(() -> {
            try {
                List<Book> result = booksDb.searchBooksByISBN(isbn);
                for (Book book : result) {
                    List<Author> authors = booksDb.getAuthorsForBook(book.getBookId());
                    book.getAuthors().clear();
                    book.getAuthors().addAll(authors);
                }
                Platform.runLater(() -> {
                    if (result.isEmpty()) {
                        booksView.showAlertAndWait("No books found with the given ISBN.", INFORMATION);
                    } else {
                        booksView.displayBooks(result);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Error searching books by ISBN: " + e.getMessage(), ERROR));
            }
        }).start();
    }


    protected void searchBooksByAuthor(String authorName) {
        new Thread(() -> {
            try {
                List<Book> result = booksDb.searchBooksByAuthor(authorName);
                for (Book book : result) {
                    List<Author> authors = booksDb.getAuthorsForBook(book.getBookId());
                    book.getAuthors().clear();
                    book.getAuthors().addAll(authors);
                }
                Platform.runLater(() -> {
                    if (result.isEmpty()) {
                        booksView.showAlertAndWait("No books found for the author: " + authorName, INFORMATION);
                    } else {
                        booksView.displayBooks(result);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Error searching books by author: " + e.getMessage(), ERROR));
            }
        }).start();
    }


    protected void searchBooksByTitle(String title) {
        new Thread(() -> {
            try {
                List<Book> result = booksDb.searchBooksByTitle(title);
                for (Book book : result) {
                    List<Author> authors = booksDb.getAuthorsForBook(book.getBookId());
                    book.getAuthors().clear();
                    book.getAuthors().addAll(authors);
                }
                Platform.runLater(() -> {
                    if (result.isEmpty()) {
                        booksView.showAlertAndWait("No books found for the title: " + title, INFORMATION);
                    } else {
                        booksView.displayBooks(result);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Error searching books by title: " + e.getMessage(), ERROR));
            }
        }).start();
    }


    protected void searchBooksByRating(int rating) {
        new Thread(() -> {
            try {
                List<Book> result = booksDb.searchBooksByRating(rating);
                for (Book book : result) {
                    List<Author> authors = booksDb.getAuthorsForBook(book.getBookId());
                    book.getAuthors().clear();
                    book.getAuthors().addAll(authors);
                }
                Platform.runLater(() -> {
                    if (result.isEmpty()) {
                        booksView.showAlertAndWait("No books found for the rating: " + rating, INFORMATION);
                    } else {
                        booksView.displayBooks(result);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Error searching books by rating: " + e.getMessage(), ERROR));
            }
        }).start();
    }

    protected void searchBooksByGenre(String genre) {
        new Thread(() -> {
            try {
                List<Book> result = booksDb.searchBooksByGenre(String.valueOf(genre));
                for (Book book : result) {
                    List<Author> authors = booksDb.getAuthorsForBook(book.getBookId());
                    book.getAuthors().clear();
                    book.getAuthors().addAll(authors);
                }
                Platform.runLater(() -> {
                    if (result.isEmpty()) {
                        booksView.showAlertAndWait("No books found for the genre: " + genre, INFORMATION);
                    } else {
                        booksView.displayBooks(result);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Error searching books by genre: " + e.getMessage(), ERROR));
            }
        }).start();
    }

    protected void onSearchSelected(String searchFor, SearchMode mode) {
        try {
            if (searchFor != null && !searchFor.trim().isEmpty()) {
                switch (mode) {
                    case Title:
                        searchBooksByTitle(searchFor);
                        break;
                    case ISBN:
                        searchBooksByISBN(searchFor);
                        break;
                    case Author:
                        searchBooksByAuthor(searchFor);
                        break;
                    case Rating:
                        try {
                            int rating = Integer.parseInt(searchFor);
                            searchBooksByRating(rating);
                        } catch (NumberFormatException e) {
                            Platform.runLater(() ->
                                    booksView.showAlertAndWait("Invalid rating format. Please enter a numeric value.", Alert.AlertType.ERROR));
                        }
                        break;
                    case Genre:
                        searchBooksByGenre(searchFor);
                        break;
                }
            } else {
                Platform.runLater(() ->
                        booksView.showAlertAndWait("Enter a search string!", WARNING));
            }
        } catch (Exception e) {
            Platform.runLater(() ->
                    booksView.showAlertAndWait("Search error: " + e.getMessage(), ERROR));
        }
    }

}

