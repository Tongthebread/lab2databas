package kth.decitong.librarydb.view;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import kth.decitong.librarydb.model.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The main pane for the view, extending VBox and including the menus. An
 * internal BorderPane holds the TableView for books and a search utility.
 *
 * @author anderslm@kth.se
 */
public class BooksPane extends VBox {
    private TableView<Book> booksTable;
    private ObservableList<Book> booksInTable;
    private ComboBox<SearchMode> searchModeBox;
    private TextField searchField;
    private Button searchButton;
    private MenuBar menuBar;

    public BooksPane(BooksDbImpl booksDb) {
        final Controller controller = new Controller(booksDb, this);
        this.init(controller);
    }

    /**
     * Display a new set of books, e.g. from a database select, in the
     * booksTable table view.
     *
     * @param books the books to display
     */
    public void displayBooks(List<Book> books) {
        booksInTable.clear();
        booksInTable.addAll(books);
    }

    /**
     * Notify user on input error or exceptions.
     *
     * @param msg  the message
     * @param type types: INFORMATION, WARNING et c.
     */
    protected void showAlertAndWait(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type, msg);
        alert.showAndWait();
    }

    private void init(Controller controller) {

        booksInTable = FXCollections.observableArrayList();

        initBooksTable();
        initSearchView(controller);
        initMenus();

        FlowPane bottomPane = new FlowPane();
        bottomPane.setHgap(10);
        bottomPane.setPadding(new Insets(10, 10, 10, 10));
        bottomPane.getChildren().addAll(searchModeBox, searchField, searchButton);

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(booksTable);
        mainPane.setBottom(bottomPane);
        mainPane.setPadding(new Insets(10, 10, 10, 10));

        this.getChildren().addAll(menuBar, mainPane);
        VBox.setVgrow(mainPane, Priority.ALWAYS);
    }

    private void initBooksTable() {
        booksTable = new TableView<>();
        booksTable.setEditable(false);
        booksTable.setPlaceholder(new Label("No rows to display"));

        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        TableColumn<Book, Integer> bookIDCol = new TableColumn<>("Book ID");
        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        TableColumn<Book, Date> publishedCol = new TableColumn<>("Published");
        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        TableColumn<Book, Integer> ratingCol = new TableColumn<>("Rating");
        TableColumn<Book, Genre> genreCol = new TableColumn<>("Genre");

        titleCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTitle()));
        bookIDCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getBookId()));
        isbnCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getIsbn()));
        publishedCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getPublished()));
        ratingCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getRating()));
        genreCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getGenre()));


        authorCol.setCellValueFactory(cellData -> {
            List<Author> authors = cellData.getValue().getAuthors();
            if (authors != null && !authors.isEmpty()) {
                String authorNames = authors.stream()
                        .map(author -> author.getFirstName() + " " + author.getLastName())
                        .collect(Collectors.joining(", "));
                return new ReadOnlyStringWrapper(authorNames);
            }
            return new ReadOnlyStringWrapper("No Authors");
        });


        booksTable.getColumns().addAll(titleCol, bookIDCol, isbnCol, publishedCol, authorCol, ratingCol, genreCol);
        booksTable.setItems(booksInTable);
    }


    private void initSearchView(Controller controller) {
        searchField = new TextField();
        searchField.setPromptText("Search for...");
        searchModeBox = new ComboBox<>();
        searchModeBox.getItems().addAll(SearchMode.values());
        searchModeBox.setValue(SearchMode.Title);
        searchButton = new Button("Search");

        searchButton.setOnAction(event -> {
            String searchFor = searchField.getText();
            SearchMode mode = searchModeBox.getValue();
            controller.onSearchSelected(searchFor, mode);
        });
    }

    private void initMenus() {

        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> {Controller.disconnect();
        Platform.exit();});
        MenuItem connectItem = new MenuItem("Connect to Db");
        connectItem.setOnAction(e -> Controller.connect());
        MenuItem disconnectItem = new MenuItem("Disconnect");
        disconnectItem.setOnAction(e -> Controller.disconnect());
        fileMenu.getItems().addAll(exitItem, connectItem, disconnectItem);

        Menu manageMenu = new Menu("Manage");
        MenuItem addItem = new MenuItem("Add");
        addItem.setOnAction(e -> showAddBookDialog());
        MenuItem removeItem = new MenuItem("Remove");
        removeItem.setOnAction(e -> showRemoveBookDialog());
        MenuItem updateItem = new MenuItem("Update");
        manageMenu.getItems().addAll(addItem, removeItem, updateItem);

        menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, manageMenu);
    }

    public void setupCloseRequestHandler(Stage primaryStage) {
        primaryStage.setOnCloseRequest(event -> {
            Controller.disconnect();
            Platform.exit();
        });
    }

    private void showRemoveBookDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Remove Book");
        dialog.setHeaderText("Enter Book ID to Remove");
        dialog.setContentText("Book ID:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(bookIdString -> {
            try {
                int bookId = Integer.parseInt(bookIdString);
                Controller.deleteBook(bookId);
                booksInTable.removeIf(book -> book.getBookId() == bookId);
            } catch (NumberFormatException e) {
                showAlertAndWait("Invalid Book ID: " + bookIdString, Alert.AlertType.ERROR);
            } catch (Exception e) {
                showAlertAndWait("Error removing book from database", Alert.AlertType.ERROR);
            }
        });
    }

    private void showAddBookDialog() {
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle("Add New Book");
        dialog.setHeaderText("Enter Book Details");

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);
        List<Author> selectedAuthors = new ArrayList<>();

        TableView<Author> authorTable = createAuthorSelectionTable();
        authorTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Button addNewAuthorButton = new Button("Add New Author");
        addNewAuthorButton.setOnAction(e -> {
            Author author = showAddAuthorDialog();
            if (author != null) {
                authorTable.getItems().add(author);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField bookIDField = new TextField();
        bookIDField.setPromptText("Book ID");
        TextField isbnField = new TextField();
        isbnField.setPromptText("ISBN");
        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        DatePicker publishedDateField = new DatePicker();
        TextField ratingField = new TextField();
        ratingField.setPromptText("Rating (1-5)");
        TextField genreField = new TextField();
        genreField.setPromptText("Genre");

        grid.add(new Label("Book ID:"), 0, 0);
        grid.add(bookIDField, 1, 0);
        grid.add(new Label("ISBN:"), 0, 1);
        grid.add(isbnField, 1, 1);
        grid.add(new Label("Title:"), 0, 2);
        grid.add(titleField, 1, 2);
        grid.add(new Label("Published Date:"), 0, 3);
        grid.add(publishedDateField, 1, 3);
        grid.add(new Label("Rating:"), 0, 4);
        grid.add(ratingField, 1, 4);
        grid.add(new Label("Genre:"), 0, 5);
        grid.add(genreField, 1, 5);
        grid.add(authorTable, 0, 6, 2, 1);
        grid.add(addNewAuthorButton, 0, 7);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                try {
                    int bookId = Integer.parseInt(bookIDField.getText());
                    String isbn = isbnField.getText();
                    String title = titleField.getText();
                    LocalDate localPublishedDate = publishedDateField.getValue();
                    Date publishedDate = null;
                    if (localPublishedDate != null) {
                        publishedDate = Date.from(localPublishedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    }
                    int rating = Integer.parseInt(ratingField.getText());
                    Genre genre = Genre.valueOf(genreField.getText().toUpperCase());

                    Book book = new Book(bookId, isbn, title, publishedDate, rating, genre);

                    selectedAuthors.addAll(authorTable.getSelectionModel().getSelectedItems());
                    selectedAuthors.forEach(book::addAuthors);

                    Controller.addBook(book);
                    booksInTable.add(book);

                    return book;
                } catch (Exception e) {
                    showAlertAndWait("Invalid input: " + e.getMessage(), Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private Author showAddAuthorDialog() {
        Dialog<Author> dialog = new Dialog<>();
        dialog.setTitle("Add New Author");
        dialog.setHeaderText("Enter Author Details");

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField authorIDField = new TextField();
        authorIDField.setPromptText("Author ID");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        DatePicker birthDateField = new DatePicker();

        grid.add(new Label("Author ID:"), 0, 0);
        grid.add(authorIDField, 1, 0);
        grid.add(new Label("First Name:"), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(new Label("Last Name:"), 0, 2);
        grid.add(lastNameField, 1, 2);
        grid.add(new Label("Birth Date:"), 0, 3);
        grid.add(birthDateField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                try {
                    int authorId = Integer.parseInt(authorIDField.getText());
                    String firstName = firstNameField.getText();
                    String lastName = lastNameField.getText();
                    LocalDate localBirthDate = birthDateField.getValue();
                    Date birthDate = null;
                    if (localBirthDate != null) {
                        birthDate = Date.from(localBirthDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    }
                    return new Author(authorId, firstName, lastName, birthDate);
                } catch (Exception e) {
                    showAlertAndWait("Invalid input: " + e.getMessage(), Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        Optional<Author> result = dialog.showAndWait();
        result.ifPresent(author -> {
            Controller.addAuthor(author);
        });
        return result.orElse(null);
    }

    private TableView<Author> createAuthorSelectionTable() {
        TableView<Author> authorTable = new TableView<>();
        authorTable.setEditable(false);
        authorTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<Author, Integer> idCol = new TableColumn<>("Author ID");
        TableColumn<Author, String> firstNameCol = new TableColumn<>("First Name");
        TableColumn<Author, String> lastNameCol = new TableColumn<>("Last Name");
        TableColumn<Author, Date> birthDateCol = new TableColumn<>("Birth Date");

        idCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getAuthorID()));
        firstNameCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getFirstName()));
        lastNameCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getLastName()));
        birthDateCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getBirthDate()));


        authorTable.getColumns().addAll(idCol, firstNameCol, lastNameCol, birthDateCol);
        Controller.fetchAllAuthors(authorTable);

        return authorTable;
    }
}