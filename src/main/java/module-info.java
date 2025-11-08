module Arkanoid {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;

    opens main to javafx.fxml;
    opens gameconfig to javafx.fxml;
    opens gamemanager to javafx.fxml;

    exports main;
    exports gameconfig;
    exports gamemanager;
    exports userinterface.gamescreen;
    opens userinterface.gamescreen to javafx.fxml;
    exports userinterface.screen;
    opens userinterface.screen to javafx.fxml;

    exports gameobject.ball;
    opens gameobject.ball to javafx.fxml;
    exports gameobject.brick;
    opens gameobject.brick to javafx.fxml;
    exports gameobject.paddle;
    opens gameobject.paddle to javafx.fxml;
    exports gameobject.powerup;
    opens gameobject.powerup to javafx.fxml;
    exports gamemanager.core;
    opens gamemanager.core to javafx.fxml;
    exports gamemanager.ui;
    opens gamemanager.ui to javafx.fxml;
    exports gamemanager.manager;
    opens gamemanager.manager to javafx.fxml;
}
