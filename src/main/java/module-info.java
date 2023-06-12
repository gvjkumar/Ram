module com.samsung.ramdashboard.ramdashboard {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.kordamp.bootstrapfx.core;
    // requires eu.hansolo.tilesfx;

    opens com.samsung.ramdashboard to javafx.fxml;
    exports com.samsung.ramdashboard;
}