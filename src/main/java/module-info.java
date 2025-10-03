module arkanoid {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens menu to javafx.fxml;
    exports menu;
    opens main to javafx.fxml;
    exports main;
    opens gameplay to javafx.fxml;
    exports gameplay;
    opens entities to javafx.fxml;
    exports entities;
    exports gameconfig;
    opens gameconfig to javafx.fxml;
}
