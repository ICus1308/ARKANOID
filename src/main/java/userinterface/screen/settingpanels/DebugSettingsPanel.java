package userinterface.screen.settingpanels;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import static gameconfig.GameConfig.*;

/**
 * Debug settings panel for the SettingScreen.
 * Placeholder for future debug options.
 */
public class DebugSettingsPanel extends VBox {

    public DebugSettingsPanel() {
        initializePanel();
    }

    private void initializePanel() {
        this.setSpacing(25);
        this.setPadding(new Insets(40, 40, 40, 40));

        String panelStyle = "-fx-border-color: #00d9ff; " +
                          "-fx-border-width: 2px; " +
                          "-fx-border-radius: 5px; " +
                          "-fx-background-color: rgba(44, 62, 80, 0.6); " +
                          "-fx-background-radius: 5px;";
        this.setStyle(panelStyle);
        this.setPrefWidth(500 * UI_SCALE_X);
        this.setPrefHeight(300);
        this.setMaxWidth(500 * UI_SCALE_X);
        this.setMaxHeight(300);

        Label placeholderLabel = createLabel("Debug settings coming soon...");
        placeholderLabel.setStyle("-fx-font-size: " + (16 * UI_SCALE) + "px; -fx-text-fill: #7f8c8d;");

        this.getChildren().add(placeholderLabel);
    }

    /**
     * Apply the debug settings changes (placeholder for future implementation)
     */
    public void applySettings() {
        // TODO: Implement debug settings application when features are added
    }

    // Helper method matching UIManager style
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-size: " + (18 * UI_SCALE) + "px;");
        return label;
    }
}

