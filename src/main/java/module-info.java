module arkanoid {
    requires javafx.controls;
    requires javafx.fxml;

    opens menu to javafx.fxml;
    exports menu;
    opens main to javafx.fxml;
    exports main;
    opens gameplay to javafx.fxml;
    exports gameplay;
    opens config to javafx.fxml;
    exports config;
    opens entities to javafx.fxml;
    exports entities;
}
