package gamemanager.ui;

import gamemanager.manager.SoundManager;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.control.ContentDisplay;
import javafx.util.Duration;

import java.util.Objects;

import static gameconfig.GameConfig.*;

public class GameButton extends Button {

    private ButtonStyle currentStyle;
    private ImageView buttonImageView;

    // hover scale configuration
    private static final double HOVER_SCALE = 1.12;
    private final ScaleTransition hoverGrow;
    private final ScaleTransition hoverShrink;

    public GameButton(String text) {
        this(text, ButtonStyle.PRIMARY);
    }

    public GameButton(String text, ButtonStyle style) {
        super(text);
        this.currentStyle = style;
        setupButton(style);

        // Load button image first
        loadButtonImage();

        applyStyle(style);

        this.setContentDisplay(ContentDisplay.CENTER);
        this.setFocusTraversable(false);

        // initialize scale transitions for smooth hover scale
        this.setScaleX(1.0);
        this.setScaleY(1.0);
        hoverGrow = new ScaleTransition(Duration.millis(120), this);
        hoverGrow.setToX(HOVER_SCALE);
        hoverGrow.setToY(HOVER_SCALE);
        hoverGrow.setInterpolator(Interpolator.EASE_OUT);

        hoverShrink = new ScaleTransition(Duration.millis(100), this);
        hoverShrink.setToX(1.0);
        hoverShrink.setToY(1.0);
        hoverShrink.setInterpolator(Interpolator.EASE_IN);

        setupHoverEffect();

        setOnMouseClicked(e -> SoundManager.getInstance().playSound(SoundManager.SoundType.BUTTON_CLICK));
    }

    private void loadButtonImage() {
        try {
            // Load button.png as background image
            Image buttonImage = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/imagebutton/button.png")));

            buttonImageView = new ImageView(buttonImage);
            buttonImageView.setPreserveRatio(false);
            buttonImageView.setFitWidth(getPrefWidth());
            buttonImageView.setFitHeight(getPrefHeight());

            // Set image as graphic behind text
            this.setGraphic(buttonImageView);
            this.setContentDisplay(ContentDisplay.CENTER);

            // Make button background transparent so only image shows
            this.setStyle("-fx-background-color: transparent; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: " + (24 * UI_SCALE) + "px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: hand;");

            System.out.println("Button image loaded: /imagebutton/button.png");
        } catch (Exception e) {
            System.err.println("Error loading button image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyStyle(ButtonStyle style) {
        getStyleClass().removeAll("game-button", "game-button-category-unselected",
                "game-button-category-selected", "game-button-apply");

        switch (style) {
            case PRIMARY:
                getStyleClass().add("game-button");
                break;
            case CATEGORY_UNSELECTED:
                getStyleClass().add("game-button-category-unselected");
                break;
            case CATEGORY_SELECTED:
                getStyleClass().add("game-button-category-selected");
                break;
            case APPLY:
                getStyleClass().add("game-button-apply");
                break;
        }
    }

    private void setupHoverEffect() {
        setOnMouseEntered(e -> {
            // play grow animation (phóng to khi hover)
            hoverShrink.stop();
            hoverGrow.playFromStart();
        });

        setOnMouseExited(e -> {
            // play shrink animation (trở lại kích thước ban đầu)
            hoverGrow.stop();
            hoverShrink.playFromStart();
        });
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
        setupButton(newStyle);
        applyStyle(newStyle);
        // Reload image with new size
        if (buttonImageView != null) {
            buttonImageView.setFitWidth(getPrefWidth());
            buttonImageView.setFitHeight(getPrefHeight());
        }
    }

    public ButtonStyle getCurrentStyle() {
        return currentStyle;
    }

    /**
     * Thêm hình ảnh vào button
     *
     * @param imagePath Đường dẫn đến hình ảnh (ví dụ: "/imagebutton/button.png")
     * @param width     Chiều rộng hình ảnh (pixel)
     * @param height    Chiều cao hình ảnh (pixel)
     */
    public void setImage(String imagePath, double width, double height) {
        try {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
            buttonImageView = new ImageView(image);
            buttonImageView.setFitWidth(width);
            buttonImageView.setFitHeight(height);
            buttonImageView.setPreserveRatio(false);

            this.setGraphic(buttonImageView);
            this.setContentDisplay(ContentDisplay.CENTER);

            System.out.println("Hình ảnh được thêm: " + imagePath);
        } catch (NullPointerException e) {
            System.err.println("Không tìm thấy hình ảnh: " + imagePath);
        }
    }

    /**
     * Thêm hình ảnh vào button với kích thước của button
     *
     * @param imagePath Đường dẫn đến hình ảnh
     */
    public void setImage(String imagePath) {
        setImage(imagePath, getPrefWidth(), getPrefHeight());
    }

    /**
     * Xóa hình ảnh khỏi button
     */
    public void clearImage() {
        this.setGraphic(null);
        buttonImageView = null;
    }
}
