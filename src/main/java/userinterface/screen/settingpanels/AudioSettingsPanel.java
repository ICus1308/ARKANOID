package userinterface.screen.settingpanels;

import gamemanager.manager.SoundManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import static gameconfig.GameConfig.*;

/**
 * Audio settings panel for the SettingScreen.
 * Handles volume controls and mute functionality.
 */
public class AudioSettingsPanel extends VBox {
    private Slider masterVolumeSlider;
    private Slider sfxVolumeSlider;
    private Slider musicVolumeSlider;
    private CheckBox muteCheckbox;

    public AudioSettingsPanel() {
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

        SoundManager soundManager = SoundManager.getInstance();

        // Master Volume
        VBox masterVolumeSection = createVolumeSliderSection(
            "Master Volume:",
            soundManager.getMasterVolume() * 100
        );
        masterVolumeSlider = (Slider) ((HBox) masterVolumeSection.getChildren().get(1)).getChildren().get(0);

        // SFX Volume
        VBox sfxVolumeSection = createVolumeSliderSection(
            "Sound Effects Volume:",
            soundManager.getSfxVolume() * 100
        );
        sfxVolumeSlider = (Slider) ((HBox) sfxVolumeSection.getChildren().get(1)).getChildren().get(0);

        // Music Volume
        VBox musicVolumeSection = createVolumeSliderSection(
            "Music Volume:",
            soundManager.getMusicVolume() * 100
        );
        musicVolumeSlider = (Slider) ((HBox) musicVolumeSection.getChildren().get(1)).getChildren().get(0);

        // Mute Checkbox
        muteCheckbox = new CheckBox("Mute All");
        muteCheckbox.setStyle("-fx-text-fill: white; -fx-font-size: " + (16 * UI_SCALE) + "px;");
        muteCheckbox.setSelected(soundManager.isMuted());
        muteCheckbox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            masterVolumeSlider.setDisable(isSelected);
            sfxVolumeSlider.setDisable(isSelected);
            musicVolumeSlider.setDisable(isSelected);
        });

        this.getChildren().addAll(
            masterVolumeSection,
            sfxVolumeSection,
            musicVolumeSection,
            muteCheckbox
        );
    }

    /**
     * Creates a volume slider section with label, slider, and value label
     */
    private VBox createVolumeSliderSection(String labelText, double initialValue) {
        Label label = createLabel(labelText);

        Slider slider = createSlider(0, 100, initialValue);
        Label valueLabel = createValueLabel((int) initialValue + "%");

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int val = (int) Math.round(newValue.doubleValue());
            valueLabel.setText(val + "%");
        });

        HBox sliderRow = new HBox(10, slider, valueLabel);
        sliderRow.setAlignment(Pos.CENTER_LEFT);

        return new VBox(5, label, sliderRow);
    }

    /**
     * Apply the audio settings changes
     */
    public void applySettings() {
        SoundManager soundManager = SoundManager.getInstance();

        // Apply mute setting first
        soundManager.setMuted(muteCheckbox.isSelected());

        // Apply all volume settings
        soundManager.setMasterVolume(masterVolumeSlider.getValue() / 100.0);
        soundManager.setSfxVolume(sfxVolumeSlider.getValue() / 100.0);
        soundManager.setMusicVolume(musicVolumeSlider.getValue() / 100.0);
    }

    // Helper methods matching UIManager style
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-size: " + (18 * UI_SCALE) + "px; -fx-font-weight: bold;");
        return label;
    }

    private Slider createSlider(double min, double max, double value) {
        Slider slider = new Slider(min, max, value);
        slider.setShowTickLabels(false);
        slider.setShowTickMarks(false);
        slider.setPrefWidth(300 * UI_SCALE_X);
        slider.setStyle("-fx-font-size: " + (12 * UI_SCALE) + "px;");
        return slider;
    }

    private Label createValueLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-size: " + (16 * UI_SCALE) + "px; -fx-font-weight: bold;");
        label.setMinWidth(50 * UI_SCALE_X);
        label.setAlignment(Pos.CENTER_RIGHT);
        return label;
    }
}

