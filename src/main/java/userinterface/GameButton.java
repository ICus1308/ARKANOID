package userinterface;

import javafx.scene.control.Button;
import javafx.scene.text.Font;

public class GameButton extends Button {

    public enum ButtonStyle {
        PRIMARY
    }

    public GameButton(String text) {
        this(text, ButtonStyle.PRIMARY);
    }

    public GameButton(String text, ButtonStyle style) {
        super(text);
        applyStyle(style);
        setupButton();
    }

    public GameButton(String text, double width, double height) {
        this(text);
        setPrefWidth(width);
        setPrefHeight(height);
    }

    public GameButton(String text, ButtonStyle style, double width, double height) {
        this(text, style);
        setPrefWidth(width);
        setPrefHeight(height);
    }

    private void applyStyle(ButtonStyle style) {
        switch (style) {
            case PRIMARY:
                setStyle("-fx-background-color: #3498db; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-color: #2980b9; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold;");
                break;
        }
    }

    private void setupButton() {
        setFont(Font.font("Inter", 24));
        setPrefWidth(200);
        setPrefHeight(50);

        setOnMouseEntered(e -> {
            String currentStyle = getStyle();
            String hoverStyle = currentStyle.replace(
                "-fx-background-color: ", 
                "-fx-background-color: derive("
            ).replace(";", ", 20%);");
            setStyle(hoverStyle);
        });
        
        setOnMouseExited(e -> {
            String currentStyle = getStyle();
            String resetStyle = currentStyle.replace(
                "-fx-background-color: derive(", 
                "-fx-background-color: "
            ).replace(", 20%);", ";");
            setStyle(resetStyle);
        });
    }
}
