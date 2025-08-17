module com.ahccode.common {
    requires javafx.controls;
    requires javafx.graphics;
    requires static lombok;
    requires static org.slf4j;

    exports com.ahccode.common.context;
    exports com.ahccode.common.network;
    exports com.ahccode.common.card;
    exports com.ahccode.common.game;
}
