module Arkanoid {
    requires javafx.controls;
    requires javafx.graphics;

    opens main to javafx.graphics, javafx.fxml;
    opens userinterface to javafx.graphics, javafx.fxml;
    opens gameobject to javafx.graphics, javafx.fxml;
    opens gamemanager to javafx.graphics, javafx.fxml;
    opens gameconfig to javafx.graphics, javafx.fxml;
}