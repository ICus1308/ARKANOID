package userinterface.screen.settingpanels;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import static gameconfig.GameConfig.*;

public class DebugSettingsPanel extends VBox {
    private Slider paddleLengthSlider;
    private Label paddleLengthValueLabel;
    private CheckBox invincibleModeCheckbox;

    public DebugSettingsPanel() {
        initializePanel();
    }

    private void initializePanel() {
        this.setSpacing(25);
        this.setPadding(new Insets(40, 40, 40, 40));

        // Panel styling matching UIManager conventions
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
        VBox sliderSection = createSliderSection(
                DEBUG_PADDLE_LENGTH_MULTIPLIER,
            String.format("%.1fx", DEBUG_PADDLE_LENGTH_MULTIPLIER)
        );

        HBox sliderRow = (HBox) sliderSection.getChildren().get(1);
        paddleLengthSlider = (Slider) sliderRow.getChildren().get(0);
        paddleLengthValueLabel = (Label) sliderRow.getChildren().get(1);

        paddleLengthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double val = Math.round(newValue.doubleValue() * 10.0) / 10.0;
            paddleLengthValueLabel.setText(String.format("%.1fx", val));
        });

        this.getChildren().add(sliderSection);
    }

    private void createInvincibleModeCheckbox() {
        invincibleModeCheckbox = new CheckBox("Invincible Mode");
        invincibleModeCheckbox.setStyle("-fx-text-fill: white; -fx-font-size: " + (16 * UI_SCALE) + "px;");
        invincibleModeCheckbox.setSelected(DEBUG_INVINCIBLE_MODE);

        Label descLabel = createDescriptionLabel();

        VBox invincibleSection = new VBox(5, invincibleModeCheckbox, descLabel);
        this.getChildren().add(invincibleSection);
    }

    private VBox createSliderSection(double initialValue, String valueText) {
        Label label = createLabel();

        Slider slider = createSlider(initialValue);
        Label valueLabel = createValueLabel(valueText);

        HBox sliderRow = new HBox(10, slider, valueLabel);
        sliderRow.setAlignment(Pos.CENTER_LEFT);

        return new VBox(5, label, sliderRow);
    }

    public void applySettings() {
        DEBUG_PADDLE_LENGTH_MULTIPLIER = Math.round(paddleLengthSlider.getValue() * 10.0) / 10.0;
        DEBUG_INVINCIBLE_MODE = invincibleModeCheckbox.isSelected();
    }

    private Label createLabel() {
        Label label = new Label("Paddle Length Multiplier:");
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-size: " + (18 * UI_SCALE) + "px; -fx-font-weight: bold;");
        return label;
    }

    private Label createDescriptionLabel() {
        Label label = new Label("(Ball bounces on bottom wall, no life loss)");
        label.setTextFill(Color.web("#95a5a6"));
        label.setStyle("-fx-font-size: " + (12 * UI_SCALE) + "px; -fx-font-style: italic;");
        return label;
    }

    private Slider createSlider(double value) {
        Slider slider = new Slider(1.0, 10.0, value);
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
        label.setMinWidth(60 * UI_SCALE_X);
        label.setAlignment(Pos.CENTER_RIGHT);
        return label;
    }
}

