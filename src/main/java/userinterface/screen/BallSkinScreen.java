package userinterface.screen;

import gamemanager.manager.CoinManager;
import gamemanager.ui.GameButton;
import gamemanager.ui.UIManager;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static gameconfig.GameConfig.*;

public class BallSkinScreen extends UIManager {
    private StackPane layout;
    private final CoinManager coinManager;
    private final Consumer<String> onApply;
    private final Runnable onBack;
    private final Map<String, Integer> items = new LinkedHashMap<>();

    private VBox actionButtonsBox;
    private Text coinText;

    public BallSkinScreen(Pane root, CoinManager coinManager, Consumer<String> onApply, Runnable onBack) {
        super(root);
        this.coinManager = coinManager;
        this.onApply = onApply;
        this.onBack = onBack;
        items.put("default", 0);
        items.put("skin1", 100);
        items.put("skin2", 200);
    }

    @Override
    protected void initializeUI() {
        layout = new StackPane();
        layout.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        layout.setStyle("-fx-background-color: rgba(44,62,80,0.95);");

        Text title = createStyledText("BALL SKINS", 0, 0, TITLE_FONT, TEXT_COLOR);

        // Coin display at top-right
        coinText = createStyledText("Coins: " + (coinManager == null ? 0 : coinManager.getCoins()), 0, 0, UI_FONT, GOLD_COLOR);
        StackPane.setAlignment(coinText, Pos.TOP_RIGHT);
        StackPane.setMargin(coinText, new Insets(20, 20, 0, 0));

        // Items grid
        HBox itemsGrid = new HBox(20);
        itemsGrid.setAlignment(Pos.CENTER);
        itemsGrid.setPadding(new Insets(20));

        for (Map.Entry<String, Integer> e : items.entrySet()) {
            String id = e.getKey();
            int price = e.getValue();

            VBox itemBox = createItemBox(id, price);
            itemsGrid.getChildren().add(itemBox);
        }

        // Action buttons container (initially empty)
        actionButtonsBox = new VBox(10);
        actionButtonsBox.setAlignment(Pos.CENTER);
        actionButtonsBox.setPrefHeight(100);

        GameButton backBtn = createButton("Back", () -> {
            hide();
            if (onBack != null) onBack.run();
        });

        VBox main = new VBox(20, title, itemsGrid, actionButtonsBox, backBtn);
        main.setAlignment(Pos.TOP_CENTER);
        main.setPadding(new Insets(40));

        layout.getChildren().addAll(main, coinText);
    }

    private VBox createItemBox(String id, int price) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPrefSize(180, 220);
        box.setPadding(new Insets(15));

        // White rounded border
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");

        // Load preview image
        try {
            String imagePath = "/imageball/" + id + ".png";
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(80);
            imageView.setFitHeight(80);
            imageView.setPreserveRatio(true);

            box.getChildren().add(imageView);
        } catch (Exception e) {
            System.err.println("Error loading ball image: " + id);
            Text placeholder = new Text("No Preview");
            placeholder.setStyle("-fx-fill: gray;");
            box.getChildren().add(placeholder);
        }

        // Item name and price
        Text nameText = new Text(id.toUpperCase());
        nameText.setStyle("-fx-fill: #2c3e50; -fx-font-size: 16px; -fx-font-weight: bold;");

        Text priceText = new Text(price > 0 ? price + " coins" : "FREE");
        priceText.setStyle("-fx-fill: " + (price > 0 ? "#e67e22" : "#27ae60") + "; -fx-font-size: 14px; -fx-font-weight: bold;");

        box.getChildren().addAll(nameText, priceText);

        // Check if owned or equipped
        boolean owned = coinManager != null && coinManager.isSkinOwned("ball", id);
        boolean equipped = coinManager != null && id.equals(coinManager.getSelectedBallSkin());

        if (equipped) {
            Text equippedLabel = new Text("✓ EQUIPPED");
            equippedLabel.setStyle("-fx-fill: #27ae60; -fx-font-size: 12px; -fx-font-weight: bold;");
            box.getChildren().add(equippedLabel);
        }

        // Hover effect - scale up
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), box);
        scaleUp.setToX(1.1);
        scaleUp.setToY(1.1);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), box);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        box.setOnMouseEntered(e -> {
            scaleDown.stop();
            scaleUp.play();
        });

        box.setOnMouseExited(e -> {
            scaleUp.stop();
            scaleDown.play();
        });

        // Click to select
        box.setOnMouseClicked(e -> {
            updateActionButtons(id, price, owned, equipped);
        });

        return box;
    }

    private void updateActionButtons(String id, int price, boolean owned, boolean equipped) {
        actionButtonsBox.getChildren().clear();

        Text selectedText = new Text("Selected: " + id.toUpperCase());
        selectedText.setStyle("-fx-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        actionButtonsBox.getChildren().add(selectedText);

        if (equipped) {
            // Already equipped - don't show any button
            // The item box already shows "✓ EQUIPPED" label
        } else if (owned || "default".equals(id)) {
            // Owned but not equipped - show equip button
            GameButton equipBtn = createButton("EQUIP", () -> {
                if (coinManager != null) {
                    coinManager.setSelectedBallSkin(id);
                    if (onApply != null) onApply.accept(id);
                    refresh();
                    show();
                }
            });
            actionButtonsBox.getChildren().add(equipBtn);
        } else {
            // Not owned - show buy button
            GameButton buyBtn = createButton("BUY (" + price + " coins)", () -> {
                if (coinManager != null && coinManager.buySkin("ball", id, price)) {
                    // Auto-equip after purchase
                    coinManager.setSelectedBallSkin(id);
                    if (onApply != null) onApply.accept(id);
                    updateCoinDisplay();
                    refresh();
                    show();
                }
            });

            // Disable if not enough coins
            if (coinManager != null && coinManager.getCoins() < price) {
                buyBtn.setDisable(true);
            }

            actionButtonsBox.getChildren().add(buyBtn);
        }
    }

    private void updateCoinDisplay() {
        if (coinText != null && coinManager != null) {
            coinText.setText("Coins: " + coinManager.getCoins());
        }
    }

    @Override
    public void show() {
        if (layout == null) initializeUI();
        if (!root.getChildren().contains(layout)) root.getChildren().add(layout);
    }

    @Override
    public void hide() {
        if (layout != null) root.getChildren().remove(layout);
    }

    @Override
    public void refresh() {
        if (layout != null) hide();
        layout = null;
    }
}
