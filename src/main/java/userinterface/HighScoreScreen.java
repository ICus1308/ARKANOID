package userinterface;

import javafx.geometry.Pos;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import static gameconfig.GameConfig.GAME_HEIGHT;
import static gameconfig.GameConfig.GAME_WIDTH;

public class HighScoreScreen extends UIManager {

    private VBox layout;
    private final Runnable onBack;
    private final ScoreManager scoreManager;

    public HighScoreScreen(Pane root, Runnable onBack, ScoreManager scoreManager) {
        super(root);
        this.onBack = onBack;
        this.scoreManager = scoreManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initializeUI() {
        layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: rgba(44, 62, 80, 0.9);");
        layout.setPrefSize(GAME_WIDTH, GAME_HEIGHT);

        Text title = createStyledText("High Scores", 0, 0, TITLE_FONT, TEXT_COLOR);

        TableView<Score> scoreTable = new TableView<>();
        scoreTable.setPrefWidth(450);
        scoreTable.setPrefHeight(500);
        scoreTable.setStyle("-fx-font-size: 18px; -fx-background-color: #34495e; -fx-alternative-row-fill-visible: true;");
        scoreTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Score, String> nameColumn = new TableColumn<>("Player");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("playerName"));

        TableColumn<Score, Integer> scoreColumn = new TableColumn<>("Score");
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        scoreColumn.setStyle("-fx-alignment: CENTER;");

        scoreTable.getColumns().addAll(nameColumn, scoreColumn);

        scoreTable.setItems(scoreManager.getScores());

        GameButton backButton = new GameButton("Back");
        backButton.setOnAction(e -> onBack.run());

        layout.getChildren().addAll(title, scoreTable, backButton);
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