module com.ahccode.cards {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires static lombok;
    requires org.slf4j;

    opens com.ahccode.cards to javafx.fxml;
    exports com.ahccode.cards;
}
