package userinterface;

import javafx.scene.control.Button;
import javafx.scene.text.Font;

public class GameButton extends Button {

    // Define final strings for your styles
    private static final String BASE_STYLE = "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-border-color: #2980b9; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 5px; " +
            "-fx-background-radius: 5px; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold;";

    private static final String HOVER_STYLE = "-fx-background-color: derive(#3498db, 20%); " + // Brighter on hover
            "-fx-text-fill: white; " +
            "-fx-border-color: #2980b9; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 5px; " +
            "-fx-background-radius: 5px; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold;";


    public enum ButtonStyle {
        PRIMARY
    }

    public GameButton(String text) {
        super(text);
        applyStyle(ButtonStyle.PRIMARY); // Apply the initial style
        setupButton();
    }
    private void applyStyle(ButtonStyle style) {
        switch (style) {
            case PRIMARY:
                // Set the initial style
                setStyle(BASE_STYLE);
                break;
        }
    }

    private void setupButton() {
        setFont(Font.font("Inter", 24));
        setPrefWidth(200);
        setPrefHeight(50);

        setOnMouseEntered(e -> setStyle(HOVER_STYLE));
        setOnMouseExited(e -> setStyle(BASE_STYLE));
    }
}