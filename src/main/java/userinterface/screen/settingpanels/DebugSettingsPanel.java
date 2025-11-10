package userinterface.screen.settingpanels;

import gamemanager.ui.UIManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static gameconfig.GameConfig.*;

public class DebugSettingsPanel extends VBox {
    private final UIManager uiManager;
    private Slider paddleLengthSlider;
    private Label paddleLengthValueLabel;
    private CheckBox invincibleModeCheckbox;

    public DebugSettingsPanel(UIManager uiManager) {
        this.uiManager = uiManager;
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

        createPaddleLengthSlider();
        createInvincibleModeCheckbox();
    }

    private void createPaddleLengthSlider() {
        Label label = uiManager.createLabel("Paddle Length Multiplier:");

        paddleLengthSlider = uiManager.createSlider(1.0, 10.0, DEBUG_PADDLE_LENGTH_MULTIPLIER);
        paddleLengthValueLabel = uiManager.createValueLabel(String.format("%.1fx", DEBUG_PADDLE_LENGTH_MULTIPLIER));

        paddleLengthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double val = Math.round(newValue.doubleValue() * 10.0) / 10.0;
            paddleLengthValueLabel.setText(String.format("%.1fx", val));
        });

        HBox sliderRow = new HBox(10, paddleLengthSlider, paddleLengthValueLabel);
        sliderRow.setAlignment(Pos.CENTER_LEFT);

        VBox paddleSection = new VBox(5, label, sliderRow);
        this.getChildren().add(paddleSection);
    }

    private void createInvincibleModeCheckbox() {
        invincibleModeCheckbox = new CheckBox("Invincible Mode");
        invincibleModeCheckbox.setStyle("-fx-text-fill: white; -fx-font-size: " + (16 * UI_SCALE) + "px;");
        invincibleModeCheckbox.setSelected(DEBUG_INVINCIBLE_MODE);


        VBox invincibleSection = new VBox(5, invincibleModeCheckbox);
        this.getChildren().add(invincibleSection);
    }

    public void applySettings() {
        DEBUG_PADDLE_LENGTH_MULTIPLIER = Math.round(paddleLengthSlider.getValue() * 10.0) / 10.0;
        DEBUG_INVINCIBLE_MODE = invincibleModeCheckbox.isSelected();
    }
}

