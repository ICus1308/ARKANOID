module arkanoid {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    exports gameconfig;
    opens gameconfig to javafx.fxml;
}
