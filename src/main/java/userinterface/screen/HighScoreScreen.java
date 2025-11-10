package userinterface.screen;

import gamemanager.ui.GameButton;
import gamemanager.manager.Score;
import gamemanager.manager.ScoreManager;
import gamemanager.ui.UIManager;
import javafx.geometry.Pos;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import static gameconfig.GameConfig.*;

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
    protected void initializeUI() {
        layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: rgba(44, 62, 80, 0.9);");
        layout.setPrefSize(GAME_WIDTH, GAME_HEIGHT);

        Text title = createStyledText("High Scores", 0, 0, TITLE_FONT, TEXT_COLOR);
        TableView<Score> scoreTable = createScoreTable();
        GameButton backButton = createButton("Back", onBack);

        layout.getChildren().addAll(title, scoreTable, backButton);
    }

    @SuppressWarnings("unchecked")
    private TableView<Score> createScoreTable() {
        TableView<Score> scoreTable = new TableView<>();
        scoreTable.setPrefWidth(450 * UI_SCALE_X);
        scoreTable.setPrefHeight(400);
        scoreTable.setStyle("-fx-font-size: " + (22 * UI_SCALE) + "px; -fx-background-color: #34495e; -fx-alternative-row-fill-visible: true; -fx-font-family: 'Simple Sunrise';");
        scoreTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Score, String> nameColumn = new TableColumn<>("Player");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("player"));

        TableColumn<Score, Integer> scoreColumn = new TableColumn<>("Score");
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        scoreColumn.setStyle("-fx-alignment: CENTER;");

        scoreTable.getColumns().addAll(nameColumn, scoreColumn);
        scoreTable.setItems(scoreManager.getScores());

        return scoreTable;
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
