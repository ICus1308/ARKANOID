package userinterface;

import gamemanager.CoinManager;
import gamemanager.GameButton;
import gamemanager.UIManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Map;
import java.util.function.Consumer;

import static gameconfig.GameConfig.GAME_HEIGHT;
import static gameconfig.GameConfig.GAME_WIDTH;

/**
 * Simple shop UI to buy/select paddle skins.
 */
public class ShopScreen extends UIManager {
    private Pane pane;
    private final CoinManager coinManager;
    private final Consumer<String> onApplySkin;
    private final Runnable onBack;
    private Text coinText;
    private Text title;
    private VBox content;
    private VBox box;

    private final Map<String, Integer> shopItems = Map.of(
            "red", 100,
            "blue", 200,
            "gold", 500
    );

    public ShopScreen(Pane pane, CoinManager coinManager, Consumer<String> onApplySkin, Runnable onBack) {
        super(null);
        this.coinManager = coinManager;
        this.onApplySkin = onApplySkin;
        this.onBack = onBack;
        this.pane = pane;
        this.root = pane;
        initializeUI();
    }

    @Override
    protected void initializeUI() {
        pane.getChildren().clear();

        title = createStyledText("SHOP", 0, 0, TITLE_FONT, TEXT_COLOR);

        coinText = createStyledText("Coins: " + coinManager.getCoins(), GAME_WIDTH - 180, 30, UI_FONT, GOLD_COLOR);

        content = new VBox(12);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER_LEFT);

        for (Map.Entry<String, Integer> item : shopItems.entrySet()) {
            String skinId = item.getKey();
            int price = item.getValue();
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            Label name = new Label(skinId.toUpperCase() + " (" + price + " coins)");
            name.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

            GameButton buyButton = new GameButton("Buy");
            buyButton.setOnAction(e -> {
                boolean ok = coinManager.buySkin(skinId, price);
                if (ok) {
                    coinText.setText("Coins: " + coinManager.getCoins());
                }
            });

            GameButton selectButton = new GameButton("Select");
            selectButton.setOnAction(e -> {
                boolean ok = coinManager.setSelectedSkin(skinId);
                if (ok) {
                    if (onApplySkin != null) onApplySkin.accept(skinId);
                }
            });

            row.getChildren().addAll(name, buyButton, selectButton);
            content.getChildren().add(row);
        }

        GameButton back = new GameButton("Back");
        back.setOnAction(e -> onBack.run());

        box = new VBox(20, title, content, back);
        box.setAlignment(Pos.TOP_CENTER);

//        pane.setStyle("-fx-background-color: rgba(44,62,80,0.95);");
//        pane.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
//        StackPane.setAlignment(title, Pos.TOP_CENTER);
//        StackPane.setAlignment(content, Pos.CENTER);
//        StackPane.setAlignment(coinText, Pos.TOP_RIGHT);
//
//        pane.getChildren().addAll(box, coinText);
    }

    public void refresh() {
        initializeUI();
    }

    public Pane getPane() { return pane; }

    public void show() {
        pane.setStyle("-fx-background-color: rgba(44,62,80,0.95);");
        pane.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        StackPane.setAlignment(title, Pos.TOP_CENTER);
        StackPane.setAlignment(content, Pos.CENTER);
        StackPane.setAlignment(coinText, Pos.TOP_RIGHT);

        pane.getChildren().addAll(box, coinText);
    }
    public void hide() {
        pane.getChildren().clear();
    }
}

