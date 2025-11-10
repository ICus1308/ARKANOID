package userinterface.screen.settingpanels;

import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static gameconfig.GameConfig.*;

/**
 * Video settings panel for the SettingScreen.
 * Handles display mode and resolution configuration.
 */
public class VideoSettingsPanel extends VBox {
    private final Pane root;
    private final Runnable onResolutionChange;

    private ComboBox<String> resolutionCombo;
    private ComboBox<String> displayModeCombo;

    private final String[] resolutions = {"1280x720", "1366x768", "1600x900"};
    private final String[] displayModes = {"Windowed", "Fullscreen"};

    public VideoSettingsPanel(Pane root, Runnable onResolutionChange) {
        this.root = root;
        this.onResolutionChange = onResolutionChange;
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

        createDisplayModeControl();
        createResolutionControl();

        // Disable resolution when fullscreen is selected
        displayModeCombo.valueProperty().addListener((obs, oldVal, newVal) ->
            resolutionCombo.setDisable("Fullscreen".equals(newVal))
        );
        resolutionCombo.setDisable("Fullscreen".equals(displayModeCombo.getValue()));
    }

    private void createDisplayModeControl() {
        Label label = createLabel("Display Mode:");

        displayModeCombo = createComboBox(displayModes);
        Stage stage = (Stage) root.getScene().getWindow();
        if (stage != null && stage.isFullScreen()) {
            displayModeCombo.getSelectionModel().select("Fullscreen");
        } else {
            displayModeCombo.getSelectionModel().select("Windowed");
        }

        VBox displayModeRow = new VBox(5, label, displayModeCombo);
        this.getChildren().add(displayModeRow);
    }

    private void createResolutionControl() {
        Label label = createLabel("Resolution:");

        resolutionCombo = createComboBox(resolutions);
        String currentResolution = ((int)GAME_WIDTH) + "x" + ((int)GAME_HEIGHT);
        if (resolutionCombo.getItems().contains(currentResolution)) {
            resolutionCombo.getSelectionModel().select(currentResolution);
        } else {
            resolutionCombo.getSelectionModel().select(0);
        }

        VBox resolutionRow = new VBox(5, label, resolutionCombo);
        this.getChildren().add(resolutionRow);
    }

    /**
     * Apply the video settings changes
     */
    public void applySettings() {
        Stage stage = (Stage) root.getScene().getWindow();
        if (stage == null) return;

        String displayMode = displayModeCombo.getValue();

        if ("Fullscreen".equals(displayMode)) {
            applyFullscreenMode(stage);
        } else {
            applyWindowedMode(stage);
        }
    }

    private void applyFullscreenMode(Stage stage) {
        stage.setFullScreen(true);

        // Get screen dimensions and update game dimensions
        Screen screen = Screen.getPrimary();
        javafx.geometry.Rectangle2D bounds = screen.getBounds();

        GAME_WIDTH = bounds.getWidth();
        GAME_HEIGHT = bounds.getHeight();
        updateUIScale();

        root.setPrefSize(GAME_WIDTH, GAME_HEIGHT);

        if (onResolutionChange != null) {
            onResolutionChange.run();
        }
    }

    private void applyWindowedMode(Stage stage) {
        stage.setFullScreen(false);

        String resolution = resolutionCombo.getValue();
        if (resolution != null && resolution.contains("x")) {
            String[] parts = resolution.split("x");
            try {
                int width = Integer.parseInt(parts[0]);
                int height = Integer.parseInt(parts[1]);

                GAME_WIDTH = width;
                GAME_HEIGHT = height;
                updateUIScale();

                root.setPrefSize(width, height);
                root.setMinSize(width, height);
                root.setMaxSize(width, height);
                stage.sizeToScene();

                if (onResolutionChange != null) {
                    onResolutionChange.run();
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid resolution format: " + resolution);
            }
        }
    }

    // Helper methods matching UIManager style
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-size: " + (18 * UI_SCALE) + "px; -fx-font-weight: bold;");
        return label;
    }

    private ComboBox<String> createComboBox(String... items) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(items);
        comboBox.setPrefWidth(300 * UI_SCALE_X);
        comboBox.setStyle("-fx-font-size: " + (14 * UI_SCALE) + "px;");
        return comboBox;
    }
}

