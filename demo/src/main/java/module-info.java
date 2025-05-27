module todotodone.app.demo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires java.desktop;
    requires jdk.jfr;

    opens todotodone.app.demo to javafx.fxml;
    exports todotodone.app.demo;
    exports todotodone.app.demo.util;
    opens todotodone.app.demo.util to javafx.fxml;
}