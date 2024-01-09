module kth.decitong.librarydb.librarydb {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;


    opens kth.decitong.librarydb to javafx.fxml;
    exports kth.decitong.librarydb;
}