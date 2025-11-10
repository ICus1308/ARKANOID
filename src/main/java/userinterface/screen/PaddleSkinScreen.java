package userinterface.screen;

import gamemanager.manager.CoinManager;
import gamemanager.ui.GameButton;
import gamemanager.ui.UIManager;
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

import static gameconfig.GameConfig.GAME_WIDTH;

public class PaddleSkinScreen extends UIManager {
    private StackPane layout;
    private final CoinManager coinManager;
    private final Consumer<String> onApply;
    private final Runnable onBack;

    private final Map<String, Integer> items = new LinkedHashMap<>();

    public PaddleSkinScreen(Pane root, CoinManager coinManager, Consumer<String> onApply, Runnable onBack) {
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
        layout.setPrefSize(GAME_WIDTH, 600);
        layout.setStyle("-fx-background-color: rgba(44,62,80,0.95);");

        Text title = createStyledText("PADDLE SKINS", 0, 0, TITLE_FONT, TEXT_COLOR);

        Text coinText = createStyledText("Coins: " + (coinManager == null ? 0 : coinManager.getCoins()), 0, 0, UI_FONT, GOLD_COLOR);
        StackPane.setAlignment(coinText, Pos.TOP_RIGHT);
        StackPane.setMargin(coinText, new Insets(20, 20, 0, 0));

        VBox itemsBox = new VBox(12);
        itemsBox.setPadding(new Insets(20));
        itemsBox.setAlignment(Pos.CENTER_LEFT);

        for (Map.Entry<String, Integer> e : items.entrySet()) {
            String id = e.getKey();
            int price = e.getValue();

            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);

            Label name = new Label(id.toUpperCase() + (price > 0 ? " (" + price + " coins)" : " (free)"));
            name.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            name.setMinWidth(220);

            boolean owned = coinManager != null && coinManager.isSkinOwned("paddle", id);
            boolean selected = coinManager != null && id.equals(coinManager.getSelectedPaddleSkin());

            // create buttons first (no action) to allow lambdas to reference them safely
            GameButton selectBtn = createButton("Select", null);
            GameButton buyBtn = createButton("Buy", null);

            // configure select button action
            selectBtn.setOnAction(evt -> {
                assert coinManager != null;
                boolean ok = coinManager.setSelectedPaddleSkin(id);
                if (ok) {
                    if (onApply != null) onApply.accept(id);
                    // refresh UI so selected state updates across the list
                    refresh();
                    show();
                }
            });
            // select is disabled if not owned (unless default) or already selected
            selectBtn.setDisable(!(owned || "default".equals(id)) || selected);
            if (selected) {
                name.setText(id.toUpperCase() + (price > 0 ? " (" + price + " coins)" : " (free)") + " - SELECTED");
            }

            // configure buy button action
            buyBtn.setOnAction(evt -> {
                if (price <= 0) return;
                assert coinManager != null;
                boolean ok = coinManager.buySkin("paddle", id, price);
                if (ok) {
                    // After purchase, rebuild UI so ownership/select buttons update
                    refresh();
                    show();
                }
            });
            // disable buy if free or already owned
            buyBtn.setDisable(price <= 0 || owned);

            row.getChildren().addAll(name, buyBtn, selectBtn);
            itemsBox.getChildren().add(row);
        }

        GameButton backBtn = createButton("Back", () -> {
            hide();
            onBack.run();
        });

        VBox main = new VBox(18, title, itemsBox, backBtn);
        main.setAlignment(Pos.TOP_CENTER);
        main.setPadding(new Insets(30));

        layout.getChildren().addAll(main, coinText);
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
