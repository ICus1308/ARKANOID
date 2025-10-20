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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static gameconfig.GameConfig.GAME_HEIGHT;
import static gameconfig.GameConfig.GAME_WIDTH;

/**
 * Simple shop UI to buy/select paddle skins.
 */
public class ShopScreen extends UIManager {
    private StackPane layout;
    private final CoinManager coinManager;
    private final Consumer<String> onApplySkin;
    private final Runnable onBack;
    private Text coinText;

    private final Map<String, Integer> shopItems;

    public ShopScreen(Pane root, CoinManager coinManager, Consumer<String> onApplySkin, Runnable onBack) {
        super(root);
        this.coinManager = coinManager;
        this.onApplySkin = onApplySkin;
        this.onBack = onBack;

        // Initialize shop items with LinkedHashMap to maintain order
        shopItems = new LinkedHashMap<>();
        shopItems.put("red", 100);
        shopItems.put("blue", 200);
        shopItems.put("gold", 500);
    }

    @Override
    protected void initializeUI() {
        layout = new StackPane();
        layout.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        layout.setStyle("-fx-background-color: rgba(44,62,80,0.95);");

        // Title section
        Text title = createStyledText("SHOP", 0, 0, TITLE_FONT, TEXT_COLOR);

        // Coin display
        coinText = createStyledText("Coins: " + coinManager.getCoins(), 0, 0, UI_FONT, GOLD_COLOR);
        HBox coinBox = new HBox(coinText);
        coinBox.setAlignment(Pos.TOP_RIGHT);
        coinBox.setPadding(new Insets(30, 30, 0, 0));
        coinBox.setMouseTransparent(true); // Allow mouse events to pass through

        // Shop items section
        VBox itemsContainer = new VBox(15);
        itemsContainer.setAlignment(Pos.CENTER_LEFT);
        itemsContainer.setPadding(new Insets(20));

        for (Map.Entry<String, Integer> item : shopItems.entrySet()) {
            String skinId = item.getKey();
            int price = item.getValue();

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);

            Label name = new Label(skinId.toUpperCase() + " (" + price + " coins)");
            name.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
            name.setMinWidth(200);

            GameButton buyButton = new GameButton("Buy");
            buyButton.setOnAction(e -> {
                boolean ok = coinManager.buySkin(skinId, price);
                if (ok) {
                    coinText.setText("Coins: " + coinManager.getCoins());
                }
                System.out.println("Bought skin: " + skinId + " - " + (ok ? "Success" : "Failed"));
            });

            GameButton selectButton = new GameButton("Select");
            selectButton.setOnAction(e -> {
                boolean ok = coinManager.setSelectedSkin(skinId);
                if (ok) {
                    if (onApplySkin != null) onApplySkin.accept(skinId);
                }
                System.out.println("Selected skin: " + skinId + " - " + (ok ? "Success" : "Failed"));
            });

            row.getChildren().addAll(name, buyButton, selectButton);
            itemsContainer.getChildren().add(row);
        }

        // Back button
        GameButton backButton = createButton("Back", onBack);

        // Main VBox layout - everything vertically stacked
        VBox mainLayout = new VBox(30);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setPadding(new Insets(50, 20, 20, 20));
        mainLayout.getChildren().addAll(title, itemsContainer, backButton);

        // Add to layout - coinBox last so it's on top, but make it mouse transparent
        layout.getChildren().addAll(mainLayout, coinBox);

        // Set alignment for coinBox within StackPane
        StackPane.setAlignment(coinBox, Pos.TOP_RIGHT);
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
