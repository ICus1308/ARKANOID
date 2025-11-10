package userinterface.screen;

import gamemanager.ui.GameButton;
import gamemanager.ui.UIManager;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

import static gameconfig.GameConfig.*;

public class MenuScreen extends UIManager {
    private StackPane stackPane;
    private Runnable onStart;
    private Runnable onHighScore;
    private Runnable onSetting;
    private Runnable onShop;

    public MenuScreen(Pane root, Runnable onStart, Runnable onHighScore, Runnable onSetting, Runnable onShop) {
        super(root);
        this.onStart = onStart;
        this.onHighScore = onHighScore;
        this.onSetting = onSetting;
        this.onShop = onShop;
        initializeUI();
    }

    @Override
    protected void initializeUI() {
        if (stackPane == null) {
            stackPane = new StackPane();
        }
        stackPane.getChildren().clear();

        // Load title image instead of text
        ImageView titleImage = createTitleImage();

        GameButton startButton = createButton("Start", onStart);
        GameButton highScoreButton = createButton("High Score", onHighScore);
        GameButton settingButton = createButton("Settings", onSetting);
        GameButton shopButton = createButton("Shop", onShop);

        // Increase button sizes for a fuller look
        double buttonWidth = 280 * UI_SCALE_X;
        double buttonHeight = 65;
        startButton.setPrefWidth(buttonWidth);
        startButton.setPrefHeight(buttonHeight);
        highScoreButton.setPrefWidth(buttonWidth);
        highScoreButton.setPrefHeight(buttonHeight);
        settingButton.setPrefWidth(buttonWidth);
        settingButton.setPrefHeight(buttonHeight);
        shopButton.setPrefWidth(buttonWidth);
        shopButton.setPrefHeight(buttonHeight);

        // Reduce spacing between buttons for a denser layout
        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(startButton, highScoreButton, settingButton, shopButton);
        // Move button box down more to avoid overlapping with title
        buttonBox.setTranslateY(100 * UI_SCALE);

        stackPane.setStyle("-fx-background-color: rgba(44,62,80,0.9);");
        stackPane.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        StackPane.setAlignment(titleImage, Pos.TOP_CENTER);
        StackPane.setAlignment(buttonBox, Pos.CENTER);
        stackPane.getChildren().addAll(titleImage, buttonBox);
    }

    private ImageView createTitleImage() {
        try {
            // Try to load title.png first, fallback to Arkanoid.png if not found
            Image titleImage = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/imagelogo/title.png")));
            ImageView imageView = new ImageView(titleImage);

            // Increase image size for better visual impact
            imageView.setFitWidth(500 * UI_SCALE);
            imageView.setPreserveRatio(true);
            imageView.setTranslateY(30 * UI_SCALE); // Move title up (reduced from 60)

            System.out.println("Title image loaded: /imagelogo/title.png");
            return imageView;
        } catch (Exception e) {
            System.err.println("Error loading title image, trying fallback...");
            try {
                Image titleImage = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/imagelogo/Arkanoid.png")));
                ImageView imageView = new ImageView(titleImage);

                imageView.setFitWidth(500 * UI_SCALE);
                imageView.setPreserveRatio(true);
                imageView.setTranslateY(30 * UI_SCALE); // Move title up (reduced from 60)

                System.out.println("Title image loaded: /imagelogo/Arkanoid.png");
                return imageView;
            } catch (Exception ex) {
                System.err.println("Error loading both title images, using text fallback");
                ex.printStackTrace();
                // Fallback to text if images fail
                return null;
            }
        }
    }

    public StackPane getStackPane() {
        return stackPane;
    }

    @Override
    public void show() {
        if (stackPane == null) {
            initializeUI();
        }
        if (!root.getChildren().contains(stackPane)) {
            root.getChildren().add(stackPane);
        }
        stackPane.setVisible(true);
        stackPane.toFront();
    }

    @Override
    public void hide() {
        if (stackPane != null) {
            stackPane.setVisible(false);
        }
    }

    @Override
    public void refresh() {
        if (stackPane != null) {
            hide();
            stackPane = null;
        }
        initializeUI();
    }

    public void refresh(Runnable onStart, Runnable onHighScore, Runnable onSetting, Runnable onShop) {
        this.onStart = onStart;
        this.onHighScore = onHighScore;
        this.onSetting = onSetting;
        this.onShop = onShop;
        initializeUI();
    }
}
