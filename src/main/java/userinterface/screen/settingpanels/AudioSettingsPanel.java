package userinterface.screen.settingpanels;

import gamemanager.manager.SoundManager;
import gamemanager.ui.UIManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.*;
import javafx.scene.image.Image;

import static gameconfig.GameConfig.*;


public class AudioSettingsPanel extends VBox {
    private final UIManager uiManager;
    private Slider masterVolumeSlider;
    private Slider sfxVolumeSlider;
    private Slider musicVolumeSlider;
    private CheckBox muteCheckbox;

    public AudioSettingsPanel(UIManager uiManager) {
        this.uiManager = uiManager;
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

        SoundManager soundManager = SoundManager.getInstance();

        VBox masterVolumeSection = createVolumeSliderSection(
            "Master Volume:",
            soundManager.getMasterVolume() * 100
        );
        masterVolumeSlider = (Slider) ((HBox) masterVolumeSection.getChildren().get(1)).getChildren().get(0);

        VBox sfxVolumeSection = createVolumeSliderSection(
            "Sound Effects Volume:",
            soundManager.getSfxVolume() * 100
        );
        sfxVolumeSlider = (Slider) ((HBox) sfxVolumeSection.getChildren().get(1)).getChildren().get(0);

        VBox musicVolumeSection = createVolumeSliderSection(
            "Music Volume:",
            soundManager.getMusicVolume() * 100
        );
        musicVolumeSlider = (Slider) ((HBox) musicVolumeSection.getChildren().get(1)).getChildren().get(0);

        muteCheckbox = new CheckBox("Mute All");
        muteCheckbox.setStyle("-fx-text-fill: white; -fx-font-size: " + (16 * UI_SCALE) + "px;");
        muteCheckbox.setSelected(soundManager.isMuted());

        muteCheckbox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            masterVolumeSlider.setDisable(isSelected);
            sfxVolumeSlider.setDisable(isSelected);
            musicVolumeSlider.setDisable(isSelected);
        });

        VBox muteBox = new VBox(muteCheckbox);
        muteBox.setAlignment(Pos.CENTER);

        this.getChildren().addAll(
            masterVolumeSection,
            sfxVolumeSection,
            musicVolumeSection,
            muteBox
        );
    }



    private VBox createVolumeSliderSection(String labelText, double initialValue) {
        Label label = uiManager.createLabel(labelText);

        Slider slider = uiManager.createSlider(0, 100, initialValue);
        Label valueLabel = uiManager.createValueLabel((int) initialValue + "%");

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int val = (int) Math.round(newValue.doubleValue());
            valueLabel.setText(val + "%");
        });

        HBox sliderRow = new HBox(10, slider, valueLabel);
        sliderRow.setAlignment(Pos.CENTER);

        VBox section = new VBox(5, label, sliderRow);
        section.setAlignment(Pos.CENTER);
        return section;
    }

    public void applySettings() {
        SoundManager soundManager = SoundManager.getInstance();

        soundManager.setMuted(muteCheckbox.isSelected());

        soundManager.setMasterVolume(masterVolumeSlider.getValue() / 100.0);
        soundManager.setSfxVolume(sfxVolumeSlider.getValue() / 100.0);
        soundManager.setMusicVolume(musicVolumeSlider.getValue() / 100.0);
    }
}
