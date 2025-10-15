package gamemanager;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public abstract class UIManager {
    protected Pane root;
    protected Text gameMessage;
    
    public UIManager(Pane root) {
        this.root = root;
    }

    protected abstract void initializeUI();

    protected Text createStyledText(String text, double x, double y, Font font, Color color) {
        Text textElement = new Text(x, y, text);
        textElement.setFont(font);
        textElement.setFill(color);
        return textElement;
    }

    public void showGameMessage(String message, Color color) {
        if (gameMessage != null) {
            gameMessage.setText(message);
            gameMessage.setFill(color);
            gameMessage.setVisible(true);
        }
    }

    public void hideGameMessage() {
        if (gameMessage != null) {
            gameMessage.setVisible(false);
        }
    }

    public void show() {
    }

    public void hide() {
    }

    public void refresh() {
    }

    protected static final Font TITLE_FONT = Font.font("Inter", 48);
    protected static final Font BUTTON_FONT = Font.font("Inter", 24);
    protected static final Font UI_FONT = Font.font("Inter", 20);
    protected static final Font MESSAGE_FONT = Font.font("Inter", 40);

    protected static final Color TEXT_COLOR = Color.WHITE;
    protected static final Color GOLD_COLOR = Color.GOLD;
    protected static final Color RED_COLOR = Color.RED;
    protected static final Color GREEN_COLOR = Color.LIMEGREEN;
}
