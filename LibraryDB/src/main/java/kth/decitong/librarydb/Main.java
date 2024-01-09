package kth.decitong.librarydb;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kth.decitong.librarydb.model.BooksDbImpl;
import kth.decitong.librarydb.view.BooksPane;

/**
 * Application start up.
 *
 * @author anderslm@kth.se
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        BooksDbImpl booksDb = new BooksDbImpl();
        BooksPane root = new BooksPane(booksDb);
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Books Database Client");
        root.setupCloseRequestHandler(primaryStage);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
