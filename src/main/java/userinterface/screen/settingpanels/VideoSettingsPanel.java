package userinterface.screen.settingpanels;

import gamemanager.ui.UIManager;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import static gameconfig.GameConfig.*;


public class VideoSettingsPanel extends VBox {
    private final UIManager uiManager;
    private final Pane root;
    private final Runnable onResolutionChange;

    private ComboBox<String> resolutionCombo;
    private ComboBox<String> displayModeCombo;

    private final String[] resolutions = {"1280x720", "1366x768", "1600x900"};
    private final String[] displayModes = {"Windowed", "Fullscreen"};

    public VideoSettingsPanel(UIManager uiManager, Pane root, Runnable onResolutionChange) {
        this.uiManager = uiManager;
        this.root = root;
        this.onResolutionChange = onResolutionChange;
        initializePanel();
    }

    private void initializePanel() {
        this.setSpacing(25);
        this.setPadding(new Insets(40, 40, 40, 40));
        this.setAlignment(Pos.CENTER);

        // Load frame image
        try {
            Image frameImage = new Image(getClass().getResourceAsStream("/background/setting.png"));
            BackgroundImage backgroundImage = new BackgroundImage(
                    frameImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
            );
            this.setBackground(new Background(backgroundImage));
        } catch (Exception e) {
            System.err.println("Could not load setting frame image: " + e.getMessage());
        }

        this.setPrefWidth(500 * UI_SCALE_X);
        this.setPrefHeight(300);
        this.setMaxWidth(500 * UI_SCALE_X);
        this.setMaxHeight(300);

        createDisplayModeControl();
        createResolutionControl();

        if (displayModeCombo != null && resolutionCombo != null) {
            displayModeCombo.valueProperty().addListener((obs, oldVal, newVal) ->
                resolutionCombo.setDisable("Fullscreen".equals(newVal))
            );
            resolutionCombo.setDisable("Fullscreen".equals(displayModeCombo.getValue()));
        }
    }

    private void createDisplayModeControl() {
        Label label = uiManager.createLabel("Display Mode:");

        displayModeCombo = uiManager.createComboBox(displayModes);
        styleComboBoxWhite(displayModeCombo);

        Stage stage = (Stage) root.getScene().getWindow();
        if (stage != null && stage.isFullScreen()) {
            displayModeCombo.getSelectionModel().select("Fullscreen");
        } else {
            displayModeCombo.getSelectionModel().select("Windowed");
        }

        VBox displayModeRow = new VBox(5, label, displayModeCombo);
        displayModeRow.setAlignment(Pos.CENTER);
        this.getChildren().add(displayModeRow);
    }

    private void createResolutionControl() {
        Label label = uiManager.createLabel("Resolution:");

        resolutionCombo = uiManager.createComboBox(resolutions);
        styleComboBoxWhite(resolutionCombo);

        String currentResolution = ((int)GAME_WIDTH) + "x" + ((int)GAME_HEIGHT);
        if (resolutionCombo.getItems().contains(currentResolution)) {
            resolutionCombo.getSelectionModel().select(currentResolution);
        } else {
            resolutionCombo.getSelectionModel().select(0);
        }

        VBox resolutionRow = new VBox(5, label, resolutionCombo);
        resolutionRow.setAlignment(Pos.CENTER);
        this.getChildren().add(resolutionRow);
    }

    private void styleComboBoxWhite(ComboBox<String> comboBox) {
        comboBox.setStyle("-fx-background-color: white; " +
                "-fx-font-size: " + (16 * UI_SCALE) + "px;");

        comboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: black; -fx-background-color: white;");
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
                    setStyle("-fx-text-fill: black; -fx-background-color: white; -fx-padding: 5px;");
                }
            }
        });
    }

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
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getBounds();

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
}

