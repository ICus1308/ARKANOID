package userinterface.screen;

import gamemanager.ui.GameButton;
import gamemanager.ui.UIManager;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import static gameconfig.GameConfig.*;

public class PauseScreen extends UIManager {
    private StackPane layout;
    private final Runnable onContinue;
    private final Runnable onMainMenu;

    // Cho phép bạn dễ dàng chỉnh kích thước khung giữa ở đây
    private static final double CONTAINER_WIDTH = 400;
    private static final double CONTAINER_HEIGHT = 250;

    public PauseScreen(Pane root, Runnable onContinue, Runnable onMainMenu) {
        super(root);
        this.onContinue = onContinue;
        this.onMainMenu = onMainMenu;
    }

    @Override
    protected void initializeUI() {

        layout = new StackPane();
        layout.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        layout.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");


        VBox container = new VBox(30);
        container.setAlignment(Pos.CENTER);
        container.setPrefSize(CONTAINER_WIDTH, CONTAINER_HEIGHT);
        container.setMinSize(CONTAINER_WIDTH, CONTAINER_HEIGHT);
        container.setMaxSize(CONTAINER_WIDTH, CONTAINER_HEIGHT);

        container.setStyle("""
            -fx-background-color: #2c3e50;
            -fx-padding: 50;
            -fx-border-color: #00d9ff;
            -fx-border-width: 3;
        """);


        Text title = createStyledText("PAUSED", 0, 0, TITLE_FONT, TEXT_COLOR);

        GameButton continueBtn = createButton("Continue", onContinue);
        GameButton menuBtn = createButton("Menu", onMainMenu);

        container.getChildren().addAll(title, continueBtn, menuBtn);
        layout.getChildren().add(container);
    }

    @Override
    public void show() {
        if (layout == null) initializeUI();
        if (!root.getChildren().contains(layout)) {
            root.getChildren().add(layout);
        }
        layout.toFront();
    }

    @Override
    public void hide() {
        if (layout != null) {
            root.getChildren().remove(layout);
        }
    }

    @Override
    public void refresh() {
        hide();
        layout = null;
    }
}
