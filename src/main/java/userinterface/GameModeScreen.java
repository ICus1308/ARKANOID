package userinterface;

import gamemanager.GameButton;
import gamemanager.UIManager;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static gameconfig.GameConfig.*;

public class GameModeScreen extends UIManager {

    private StackPane layout;
    private final Runnable onSinglePlayer;
    private final Runnable onOneVOne;
    private final Runnable onBot;
    private final Runnable onBack;

    public GameModeScreen(Pane root,
                          Runnable onSinglePlayer,
                          Runnable onOneVOne,
                          Runnable onBot,
                          Runnable onBack) {
        super(root);
        this.onSinglePlayer = onSinglePlayer;
        this.onOneVOne = onOneVOne;
        this.onBot = onBot;
        this.onBack = onBack;
    }

    @Override
    protected void initializeUI() {
        layout = new StackPane();
        layout.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        layout.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        Rectangle bgRect = createBackgroundRectangle(400 * UI_SCALE_X, Color.web("#2c3e50"));
        VBox buttonBox = createButtonBox();

        layout.getChildren().addAll(bgRect, buttonBox);
    }

    private VBox createButtonBox() {
        GameButton singlePlayerButton = createButton("Singleplayer", onSinglePlayer);
        GameButton oneVOneButton = createButton("1v1", onOneVOne);
        GameButton oneVBotButton = createButton("1vBot", onBot);
        GameButton endlessButton = createButton("Endless Mode", () -> {});
        GameButton backButton = createButton("Back", onBack);

        VBox buttonBox = new VBox(20, singlePlayerButton, oneVOneButton, oneVBotButton, endlessButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMaxWidth(400 * UI_SCALE_X);

        return buttonBox;
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
    }
}
