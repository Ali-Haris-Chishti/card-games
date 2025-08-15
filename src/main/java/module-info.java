module com.ahccode.cards {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires static lombok;
    requires org.slf4j;
    requires java.desktop;

    opens com.ahccode.cards to javafx.fxml;
    opens com.ahccode.cards.ui.network to javafx.graphics;

    exports com.ahccode.cards;
}
