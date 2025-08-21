module com.ahccode.server {
    requires static lombok;
    requires org.slf4j;
    requires javafx.controls;
    requires com.ahccode.common;

    exports com.ahccode.server.ui to javafx.graphics;
}