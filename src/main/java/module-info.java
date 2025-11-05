module Arkanoid {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;

    opens main to javafx.fxml;
    opens userinterface to javafx.fxml;
    opens gameobject to javafx.fxml;
    opens gameconfig to javafx.fxml;
    opens gamemanager to javafx.fxml;

    exports main;
    exports userinterface;
    exports gameobject;
    exports gameconfig;
    exports gamemanager;
}
