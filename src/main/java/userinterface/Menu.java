package userinterface;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import static gameconfig.GameConfig.GAME_HEIGHT;
import static gameconfig.GameConfig.GAME_WIDTH;

public class Menu extends UIManager {
    private final StackPane stackPane;

    public Menu(Runnable onStart) {
        super(null); // We'll create our own root
        this.stackPane = new StackPane();
        
        Text title = createStyledText("ARKANOID", 0, 0, TITLE_FONT, TEXT_COLOR);

        GameButton startButton = new GameButton("Start", GameButton.ButtonStyle.PRIMARY);
        startButton.setOnAction(e -> onStart.run());

        GameButton getHighScoreButton = new GameButton("Get High Score", GameButton.ButtonStyle.PRIMARY);
        getHighScoreButton.setOnAction(e -> onStart.run());

        stackPane.setStyle("-fx-background-color: rgba(44,62,80,0.9);");
        stackPane.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        StackPane.setAlignment(title, Pos.TOP_CENTER);
        StackPane.setAlignment(startButton, Pos.CENTER);
        StackPane.setAlignment(getHighScoreButton, Pos.CENTER_RIGHT);
        stackPane.getChildren().addAll(title, startButton, getHighScoreButton);

        this.root = stackPane;
    }
    
    @Override
    protected void initializeUI() {

    }

    public StackPane getStackPane() {
        return stackPane;
    }
}
