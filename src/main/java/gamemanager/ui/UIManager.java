package gamemanager.ui;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ComboBox;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;

import static gameconfig.GameConfig.*;

public abstract class UIManager {
    protected Pane root;
    protected Text gameMessage;

    public UIManager(Pane root) {
        this.root = root;
    }

    /**
     * Initializes the UI components
     */
    protected abstract void initializeUI();

    /**
     * Creates a styled text element
     */
    protected Text createStyledText(String text, double x, double y, Font font, Color color) {
        Text textElement = new Text(x, y, text);
        textElement.setFont(font);
        textElement.setFill(color);
        return textElement;
    }

    /**
     * Creates a basic styled button
     */
    protected GameButton createButton(String text, Runnable action) {
        GameButton button = new GameButton(text);
        if (action != null) {
            button.setOnAction(e -> {
                gamemanager.manager.SoundManager.getInstance().playSound(gamemanager.manager.SoundManager.SoundType.BUTTON_CLICK);
                action.run();
            });
        }
        return button;
    }

    /**
     * Creates a styled button with specific style
     */
    protected GameButton createButton(String text, ButtonStyle style, Runnable action) {
        GameButton button = new GameButton(text, style);
        if (action != null) {
            button.setOnAction(e -> {
                gamemanager.manager.SoundManager.getInstance().playSound(gamemanager.manager.SoundManager.SoundType.BUTTON_CLICK);
                action.run();
            });
        }
        return button;
    }

    /**
     * Creates a styled label for settings
     */
    protected Label createLabel(String text, Color color) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, (double) 18 * UI_SCALE));
        label.setStyle("-fx-text-fill: " + toRgbString(color) + ";");
        return label;
    }

    /**
     * Creates a styled title label
     */
    protected Label createTitleLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 32 * UI_SCALE));
        label.setStyle("-fx-text-fill: white;");
        return label;
    }

    /**
     * Creates a styled slider for settings
     */
    protected Slider createSlider(double min, double max, double value) {
        Slider slider = new Slider(min, max, value);
        slider.setPrefWidth(360 * UI_SCALE_X);
        slider.setPrefHeight(35);
        slider.setMajorTickUnit((max - min) / 10);
        slider.setShowTickMarks(false);
        slider.setShowTickLabels(false);
        return slider;
    }

    /**
     * Creates a styled ComboBox for settings
     */
    protected ComboBox<String> createComboBox(String... items) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(items);
        comboBox.setPrefWidth(400 * UI_SCALE_X);
        comboBox.setPrefHeight(35);

        comboBox.setStyle("-fx-background-color: #2c3e50; " +
                "-fx-font-size: " + (16 * UI_SCALE) + "px;");

        comboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white; -fx-background-color: #2c3e50;");
                }
            }
        });

        comboBox.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white; -fx-background-color: #34495e;");
                }
            }
        });

        return comboBox;
    }

    /**
     * Creates a styled value label with padding
     */
    protected Label createValueLabel(String text) {
        Label label = createLabel(text, TEXT_COLOR);
        label.setPadding(new Insets(0, 0, 0, 10));
        return label;
    }

    /**
     * Creates a background rectangle with rounded corners
     */
    protected Rectangle createBackgroundRectangle(double width, Color fill) {
        Rectangle rect = new Rectangle(width, 500);
        rect.setArcWidth(20);
        rect.setArcHeight(20);
        rect.setFill(fill);
        rect.setStroke(Color.GOLD);
        rect.setStrokeWidth(2);
        return rect;
    }

    /**
     * Converts Color to RGB string for CSS
     */
    private String toRgbString(Color color) {
        return String.format("rgb(%d, %d, %d)",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    /**
     * Shows a game message on the screen
     */
    public void showGameMessage(String message, Color color) {
        if (gameMessage != null) {
            gameMessage.setText(message);
            gameMessage.setFill(color);
            gameMessage.setVisible(true);
        }
    }

    public void hideGameMessage() {
        if (gameMessage != null) {
            gameMessage.setVisible(false);
        }
    }

    public void show() {
    }

    public void hide() {
    }

    public void refresh() {
    }

    // Legacy constants for backward compatibility
    protected static final Font TITLE_FONT = Font.font("Inter", 48);
    protected static final Font UI_FONT = Font.font("Inter", 20);
    protected static final Font MESSAGE_FONT = Font.font("Inter", 40);

    protected static final Color TEXT_COLOR = Color.WHITE;
    protected static final Color GOLD_COLOR = Color.GOLD;
    protected static final Color RED_COLOR = Color.RED;
    protected static final Color GREEN_COLOR = Color.LIMEGREEN;
}
