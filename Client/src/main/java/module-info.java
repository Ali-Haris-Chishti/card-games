module com.ahccode.client {
    requires com.ahccode.common;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires static lombok;
    requires org.slf4j;

    exports com.ahccode.client to javafx.graphics;
}
