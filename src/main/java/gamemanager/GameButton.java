package gamemanager;

import javafx.scene.control.Button;
import javafx.scene.text.Font;

import static gameconfig.GameConfig.UI_SCALE;
import static gameconfig.GameConfig.UI_SCALE_X;

public class GameButton extends Button {

    public enum ButtonStyle {
        PRIMARY,
        CATEGORY_UNSELECTED,
        CATEGORY_SELECTED,
        APPLY
    }

    private ButtonStyle currentStyle;

    public GameButton(String text) {
        this(text, ButtonStyle.PRIMARY);
    }

    public GameButton(String text, ButtonStyle style) {
        super(text);
        this.currentStyle = style;
        applyStyle(style);
        setupButton(style);
    }

    private void applyStyle(ButtonStyle style) {
        getStyleClass().removeAll("game-button", "game-button-category-unselected",
                                   "game-button-category-selected", "game-button-apply");

        switch (style) {
            case PRIMARY:
                getStyleClass().add("game-button");
                setStyle("-fx-background-color: #3498db; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-color: #2980b9; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-font-size: " + (24 * UI_SCALE) + "px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");
                setupHoverEffect("#3498db", "#5dade2", "white");
                break;
            case CATEGORY_UNSELECTED:
                getStyleClass().add("game-button-category-unselected");
                setStyle("-fx-background-color: #2c3e50; " +
                        "-fx-text-fill: #95a5a6; " +
                        "-fx-border-color: #34495e; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-font-size: " + (16 * UI_SCALE) + "px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");
                setupHoverEffect("#2c3e50", "#34495e", "#ecf0f1");
                break;
            case CATEGORY_SELECTED:
                getStyleClass().add("game-button-category-selected");
                setStyle("-fx-background-color: transparent; " +
                        "-fx-text-fill: #00d9ff; " +
                        "-fx-border-color: #00d9ff; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-font-size: " + (16 * UI_SCALE) + "px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");
                setupHoverEffect("transparent", "rgba(0, 217, 255, 0.1)", "#00d9ff");
                break;
            case APPLY:
                getStyleClass().add("game-button-apply");
                setStyle("-fx-background-color: #27ae60; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-color: #229954; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-font-size: " + (14 * UI_SCALE) + "px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;");
                setupHoverEffect("#27ae60", "#2ecc71", "white");
                break;
        }
    }

    private void setupHoverEffect(String normalBg, String hoverBg, String textColor) {
        final String normalStyle = getStyle();

        setOnMouseEntered(e -> {
            String hoverStyle = normalStyle.replaceAll("-fx-background-color: [^;]+;",
                                                       "-fx-background-color: " + hoverBg + ";");
            if (!textColor.equals("white") && textColor.contains("#")) {
                hoverStyle = hoverStyle.replaceAll("-fx-text-fill: [^;]+;",
                                                   "-fx-text-fill: " + textColor + ";");
            }
            setStyle(hoverStyle);
        });

        setOnMouseExited(e -> setStyle(normalStyle));
    }

    private void setupButton(ButtonStyle style) {
        switch (style) {
            case PRIMARY:
                setFont(Font.font("Inter", 24 * UI_SCALE));
                setPrefWidth(200 * UI_SCALE_X);
                setPrefHeight(50);
                break;
            case CATEGORY_UNSELECTED:
            case CATEGORY_SELECTED:
                setFont(Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 16 * UI_SCALE));
                setPrefWidth(240 * UI_SCALE_X);
                setPrefHeight(50);
                break;
            case APPLY:
                setFont(Font.font("Inter", 14 * UI_SCALE));
                setPrefWidth(200 * UI_SCALE_X);
                setPrefHeight(50);
                break;
        }
    }

    public void switchStyle(ButtonStyle newStyle) {
        this.currentStyle = newStyle;
        applyStyle(newStyle);
    }

    public ButtonStyle getCurrentStyle() {
        return currentStyle;
    }
}