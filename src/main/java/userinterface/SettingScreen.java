package userinterface;

import gamemanager.GameButton;
import gamemanager.UIManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import static gameconfig.GameConfig.*;

public class SettingScreen extends UIManager {
    private BorderPane layout;
    private final Runnable onBack;
    private final Runnable onResolutionChange;
    private VBox contentArea;

    private GameButton videoButton;
    private GameButton audioButton;
    private GameButton debugButton;

    private ComboBox<String> resolutionCombo;

    private Slider masterVolumeSlider;
    private CheckBox muteCheckbox;
    private Label volumeValueLabel;
    private double masterVolume = 100.0;
    private boolean muted = false;

    private final String[] resolutions = {"800x600", "1000x600"};
    private final String[] displayModes = {"Windowed"};

    public SettingScreen(Pane root, Runnable onBack, Runnable onResolutionChange) {
        super(root);
        this.onBack = onBack;
        this.onResolutionChange = onResolutionChange;
    }

    @Override
    protected void initializeUI() {
        layout = new BorderPane();
        layout.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        layout.setStyle("-fx-background-color: rgba(44, 62, 80, 0.9);");

        VBox sidebar = createSidebar();
        layout.setLeft(sidebar);

        contentArea = createContentArea();
        layout.setCenter(contentArea);

        updateContentArea("VIDEO");
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(280 * UI_SCALE_X);
        sidebar.setStyle("-fx-background-color: rgba(34, 52, 70, 0.9);");
        sidebar.setPadding(new Insets(40, 20, 40, 20));
        sidebar.setSpacing(15);

        createCategoryButtons();

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        GameButton backButton = createButton("BACK", onBack);

        sidebar.getChildren().addAll(videoButton, audioButton, debugButton, spacer, backButton);
        return sidebar;
    }

    private void createCategoryButtons() {
        videoButton = createButton("VIDEO", GameButton.ButtonStyle.CATEGORY_UNSELECTED, null);
        audioButton = createButton("AUDIO", GameButton.ButtonStyle.CATEGORY_UNSELECTED, null);
        debugButton = createButton("DEBUG", GameButton.ButtonStyle.CATEGORY_UNSELECTED, null);

        videoButton.switchStyle(GameButton.ButtonStyle.CATEGORY_SELECTED);

        videoButton.setOnAction(e -> selectCategory("VIDEO", videoButton));
        audioButton.setOnAction(e -> selectCategory("AUDIO", audioButton));
        debugButton.setOnAction(e -> selectCategory("DEBUG", debugButton));
    }

    private void selectCategory(String category, GameButton selectedButton) {
        videoButton.switchStyle(GameButton.ButtonStyle.CATEGORY_UNSELECTED);
        audioButton.switchStyle(GameButton.ButtonStyle.CATEGORY_UNSELECTED);
        debugButton.switchStyle(GameButton.ButtonStyle.CATEGORY_UNSELECTED);
        selectedButton.switchStyle(GameButton.ButtonStyle.CATEGORY_SELECTED);

        updateContentArea(category);
    }

    private VBox createContentArea() {
        VBox content = new VBox();
        content.setStyle("-fx-background-color: transparent;");
        content.setPadding(new Insets(40, 40, 40, 40));
        return content;
    }

    private void updateContentArea(String category) {
        contentArea.getChildren().clear();

        switch (category) {
            case "VIDEO":
                showVideoSettings();
                break;
            case "AUDIO":
                showAudioSettings();
                break;
            case "DEBUG":
                showDebugSettings();
                break;
        }
    }

    private void showVideoSettings() {
        Label titleLabel = createTitleLabel("VIDEO SETTINGS");
        VBox settingsPanel = createVideoSettingsPanel();
        GameButton applyButton = createButton("✓ APPLY CHANGES", GameButton.ButtonStyle.APPLY, this::applyVideoSettings);

        HBox applyButtonBox = new HBox(applyButton);
        applyButtonBox.setAlignment(Pos.BOTTOM_RIGHT);
        applyButtonBox.setPadding(new Insets(30, 0, 0, 0));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        contentArea.getChildren().addAll(titleLabel, settingsPanel, spacer, applyButtonBox);
    }

    private VBox createVideoSettingsPanel() {
        VBox settingsPanel = createSettingsPanel();

        ComboBox<String> displayModeCombo = new ComboBox<>();
        displayModeCombo.getItems().addAll(displayModes);
        displayModeCombo.getSelectionModel().select("Windowed");
        Label displayModeLabel = createSettingRow("Display Mode:", displayModeCombo);

        resolutionCombo = new ComboBox<>();
        resolutionCombo.getItems().addAll(resolutions);
        String currentResolution = ((int)GAME_WIDTH) + "x" + ((int)GAME_HEIGHT);
        if (resolutionCombo.getItems().contains(currentResolution)) {
            resolutionCombo.getSelectionModel().select(currentResolution);
        } else {
            resolutionCombo.getSelectionModel().select(0);
        }
        Label resolutionLabel = createSettingRow("Resolution:", resolutionCombo);

        VBox displayModeRow = new VBox(5, displayModeLabel, displayModeCombo);
        VBox resolutionRow = new VBox(5, resolutionLabel, resolutionCombo);

        settingsPanel.getChildren().addAll(displayModeRow, resolutionRow);
        return settingsPanel;
    }

    private void showAudioSettings() {
        Label titleLabel = createTitleLabel("AUDIO SETTINGS");
        VBox settingsPanel = createSettingsPanel();

        Label volumeLabel = createLabel("Master Volume:", TEXT_COLOR);
        masterVolumeSlider = new Slider(0, 100, masterVolume);
        masterVolumeSlider.setPrefWidth(360 * UI_SCALE_X);
        masterVolumeSlider.setPrefHeight(35);
        masterVolumeSlider.setMajorTickUnit(10);
        masterVolumeSlider.setShowTickMarks(false);
        masterVolumeSlider.setShowTickLabels(false);

        volumeValueLabel = createLabel((int) masterVolume + "%", TEXT_COLOR);
        volumeValueLabel.setPadding(new Insets(0, 0, 0, 10));

        masterVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int val = (int) Math.round(newValue.doubleValue());
            volumeValueLabel.setText(val + "%");
        });

        HBox volumeRow = new HBox(10, masterVolumeSlider, volumeValueLabel);
        volumeRow.setAlignment(Pos.CENTER_LEFT);

        muteCheckbox = new CheckBox("Mute");
        muteCheckbox.setStyle("-fx-text-fill: white;");
        muteCheckbox.setSelected(muted);
        muteCheckbox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            masterVolumeSlider.setDisable(isSelected);
        });

        VBox volumeSection = new VBox(5, volumeLabel, volumeRow, muteCheckbox);

        settingsPanel.getChildren().addAll(volumeSection);

        GameButton applyButton = createButton("✓ APPLY CHANGES", GameButton.ButtonStyle.APPLY, this::applyAudioSettings);
        HBox applyButtonBox = new HBox(applyButton);
        applyButtonBox.setAlignment(Pos.BOTTOM_RIGHT);
        applyButtonBox.setPadding(new Insets(30, 0, 0, 0));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        contentArea.getChildren().addAll(titleLabel, settingsPanel, spacer, applyButtonBox);
    }

    private void showDebugSettings() {
        Label titleLabel = createTitleLabel("DEBUG SETTINGS");
        VBox settingsPanel = createSettingsPanel();

        Label placeholderLabel = createLabel("Debug settings coming soon...", TEXT_COLOR.darker());

        settingsPanel.getChildren().add(placeholderLabel);
        contentArea.getChildren().addAll(titleLabel, settingsPanel);
    }

    private VBox createSettingsPanel() {
        VBox settingsPanel = new VBox(25);
        settingsPanel.setPadding(new Insets(40, 40, 40, 40));

        String panelStyle = "-fx-border-color: #00d9ff; " +
                "-fx-border-width: 2px; " +
                "-fx-background-color: rgba(22, 33, 62, 0.8); " +
                "-fx-border-radius: 5px; " +
                "-fx-background-radius: 5px;";
        settingsPanel.setStyle(panelStyle);
        settingsPanel.setPrefWidth(500 * UI_SCALE_X);
        settingsPanel.setPrefHeight(300);
        settingsPanel.setMaxWidth(500 * UI_SCALE_X);
        settingsPanel.setMaxHeight(300);
        return settingsPanel;
    }

    private Label createSettingRow(String labelText, ComboBox<String> comboBox) {
        Label label = createLabel(labelText, TEXT_COLOR);

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

        return label;
    }

    private void applyVideoSettings() {
        String resolution = resolutionCombo.getValue();

        if (resolution != null && resolution.contains("x")) {
            String[] parts = resolution.split("x");
            try {
                int width = Integer.parseInt(parts[0]);
                int height = Integer.parseInt(parts[1]);

                GAME_WIDTH = width;
                updateUIScale();

                Stage stage = (Stage) root.getScene().getWindow();
                if (stage != null) {
                    root.setPrefSize(width, height);
                    root.setMinSize(width, height);
                    root.setMaxSize(width, height);
                    stage.sizeToScene();
                }

                if (onResolutionChange != null) {
                    onResolutionChange.run();
                }

                refresh();
                show();

                System.out.println("Applied resolution: " + width + "x" + height + " (UI Scale: " + UI_SCALE + ")");
            } catch (NumberFormatException e) {
                System.err.println("Invalid resolution format: " + resolution);
            }
        }
    }

    private void applyAudioSettings() {
        masterVolume = masterVolumeSlider.getValue();
        muted = muteCheckbox.isSelected();

        System.out.println("Applied audio settings - Volume: " + (int) masterVolume + "%, Muted: " + muted);
    }

    @Override
    public void show() {
        if (layout == null) {
            initializeUI();
        }
        if (!root.getChildren().contains(layout)) {
            root.getChildren().add(layout);
        }
    }

    @Override
    public void hide() {
        if (layout != null) {
            root.getChildren().remove(layout);
        }
    }

    @Override
    public void refresh() {
        if (layout != null) {
            hide();
        }
        layout = null;
        contentArea = null;
    }
}
