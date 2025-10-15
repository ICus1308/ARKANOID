module Arkanoid {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires java.desktop;

    opens main to javafx.graphics, javafx.fxml;
    opens userinterface to javafx.graphics, javafx.fxml, javafx.base;
    opens gameobject to javafx.graphics, javafx.fxml;
    opens gameconfig to javafx.graphics, javafx.fxml;
    opens gamemanager to javafx.base, javafx.fxml, javafx.graphics;
}