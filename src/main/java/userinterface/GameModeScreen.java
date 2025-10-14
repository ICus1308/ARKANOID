package userinterface;

import gamemanager.GameButton;
import gamemanager.UIManager;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import static gameconfig.GameConfig.GAME_HEIGHT;
import static gameconfig.GameConfig.GAME_WIDTH;

public class GameModeScreen extends UIManager {

    private StackPane layout;
    private final Runnable onSinglePlayer;
    private final Runnable onBack;

    public GameModeScreen(Pane root, Runnable onSinglePlayer, Runnable onBack) {
        super(root);
        this.onSinglePlayer = onSinglePlayer;
        this.onBack = onBack;
    }

    @Override
    protected void initializeUI() {
        layout = new StackPane();
        layout.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        layout.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        Rectangle bgRect = new Rectangle(400, 500);
        bgRect.setArcWidth(20);
        bgRect.setArcHeight(20);
        bgRect.setFill(Color.web("#2c3e50"));
        bgRect.setStroke(Color.GOLD);

        Text title = createStyledText("Select Mode", 0, 0, TITLE_FONT, TEXT_COLOR);

        GameButton singlePlayerButton = new GameButton("Singleplayer");
        singlePlayerButton.setOnAction(e -> onSinglePlayer.run());

        GameButton oneVOneButton = new GameButton("1v1");
        oneVOneButton.setOnAction(e -> {});

        GameButton oneVBotButton = new GameButton("1vBot");
        oneVBotButton.setOnAction(e -> {});

        GameButton endlessButton = new GameButton("Endless Mode");
        endlessButton.setOnAction(e -> {});

        GameButton backButton = new GameButton("Back");
        backButton.setOnAction(e -> onBack.run());

        VBox buttonBox = new VBox(20, title, singlePlayerButton, oneVOneButton, oneVBotButton, endlessButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMaxWidth(400);

        layout.getChildren().addAll(bgRect, buttonBox);
    }

    public void show() {
        if (layout == null) {
            initializeUI();
        }
        if (!root.getChildren().contains(layout)) {
            root.getChildren().add(layout);
        }
    }

    public void hide() {
        if (layout != null) {
            root.getChildren().remove(layout);
        }
    }
}

