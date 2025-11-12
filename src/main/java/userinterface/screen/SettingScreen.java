package userinterface.screen;

import gamemanager.ui.GameButton;
import gamemanager.ui.UIManager;
import gamemanager.manager.SoundManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import userinterface.screen.settingpanels.*;

import static gameconfig.GameConfig.*;

public class SettingScreen extends UIManager {
    private BorderPane layout;
    private final Runnable onBack;
    private final Runnable onResolutionChange;
    private VBox contentArea;

    private GameButton videoButton;
    private GameButton audioButton;
    private GameButton debugButton;

    // Settings panels
    private VideoSettingsPanel videoSettingsPanel;
    private AudioSettingsPanel audioSettingsPanel;
    private DebugSettingsPanel debugSettingsPanel;

    public SettingScreen(Pane root, Runnable onBack, Runnable onResolutionChange) {
        super(root);
        this.onBack = onBack;
        this.onResolutionChange = onResolutionChange;
    }

    @Override
    protected void initializeUI() {
        layout = new BorderPane();
        layout.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        layout.setStyle("-fx-background-color: rgba(44, 62, 80, 0.1);");

        VBox sidebar = createSidebar();
        layout.setLeft(sidebar);

        contentArea = createContentArea();
        layout.setCenter(contentArea);

        updateContentArea("VIDEO");
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(280 * UI_SCALE_X);
        sidebar.setStyle("-fx-background-color: rgba(34, 52, 70, 0.1);");
        sidebar.setPadding(new Insets(40, 20, 40, 20));
        sidebar.setSpacing(15);

        createCategoryButtons();

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        GameButton backButton = createButton("BACK", onBack);

        sidebar.getChildren().addAll(videoButton, audioButton, debugButton, spacer, backButton);
        return sidebar;
    }

    private void createCategoryButtons() {
        videoButton = createButton("VIDEO", ButtonStyle.CATEGORY_UNSELECTED, null);
        audioButton = createButton("AUDIO", ButtonStyle.CATEGORY_UNSELECTED, null);
        debugButton = createButton("DEBUG", ButtonStyle.CATEGORY_UNSELECTED, null);

        videoButton.switchStyle(ButtonStyle.CATEGORY_SELECTED);

        videoButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.SoundType.BUTTON_CLICK);
            selectCategory("VIDEO", videoButton);
        });
        audioButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.SoundType.BUTTON_CLICK);
            selectCategory("AUDIO", audioButton);
        });
        debugButton.setOnAction(e -> {
            SoundManager.getInstance().playSound(SoundManager.SoundType.BUTTON_CLICK);
            selectCategory("DEBUG", debugButton);
        });
    }

    private void selectCategory(String category, GameButton selectedButton) {
        videoButton.switchStyle(ButtonStyle.CATEGORY_UNSELECTED);
        audioButton.switchStyle(ButtonStyle.CATEGORY_UNSELECTED);
        debugButton.switchStyle(ButtonStyle.CATEGORY_UNSELECTED);
        selectedButton.switchStyle(ButtonStyle.CATEGORY_SELECTED);

        updateContentArea(category);
    }

    private VBox createContentArea() {
        VBox content = new VBox();
        content.setStyle("-fx-background-color: transparent;");
        content.setPadding(new Insets(40, 40, 40, 40));
        return content;
    }

    private void updateContentArea(String category) {
        contentArea.getChildren().clear();

        switch (category) {
            case "VIDEO":
                showVideoSettings();
                break;
            case "AUDIO":
                showAudioSettings();
                break;
            case "DEBUG":
                showDebugSettings();
                break;
        }
    }

    private void showVideoSettings() {
        Label titleLabel = createTitleLabel("VIDEO SETTINGS");

        videoSettingsPanel = new VideoSettingsPanel(this, root, onResolutionChange);

        GameButton applyButton = createButton("✓ APPLY CHANGES", ButtonStyle.APPLY, this::applyVideoSettings);

        HBox applyButtonBox = new HBox(applyButton);
        applyButtonBox.setAlignment(Pos.BOTTOM_RIGHT);
        applyButtonBox.setPadding(new Insets(30, 0, 0, 0));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        contentArea.getChildren().addAll(titleLabel, videoSettingsPanel, spacer, applyButtonBox);
    }

    private void showAudioSettings() {
        Label titleLabel = createTitleLabel("AUDIO SETTINGS");

        audioSettingsPanel = new AudioSettingsPanel(this);

        GameButton applyButton = createButton("✓ APPLY CHANGES", ButtonStyle.APPLY, this::applyAudioSettings);
        HBox applyButtonBox = new HBox(applyButton);
        applyButtonBox.setAlignment(Pos.BOTTOM_RIGHT);
        applyButtonBox.setPadding(new Insets(30, 0, 0, 0));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        contentArea.getChildren().addAll(titleLabel, audioSettingsPanel, spacer, applyButtonBox);
    }

    private void showDebugSettings() {
        Label titleLabel = createTitleLabel("DEBUG SETTINGS");

        debugSettingsPanel = new DebugSettingsPanel(this);

        GameButton applyButton = createButton("✓ APPLY CHANGES", ButtonStyle.APPLY, this::applyDebugSettings);
        HBox applyButtonBox = new HBox(applyButton);
        applyButtonBox.setAlignment(Pos.BOTTOM_RIGHT);
        applyButtonBox.setPadding(new Insets(30, 0, 0, 0));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        contentArea.getChildren().addAll(titleLabel, debugSettingsPanel, spacer, applyButtonBox);
    }

    private void applyVideoSettings() {
        if (videoSettingsPanel != null) {
            videoSettingsPanel.applySettings();
        }
    }

    private void applyAudioSettings() {
        if (audioSettingsPanel != null) {
            audioSettingsPanel.applySettings();
        }
    }

    private void applyDebugSettings() {
        if (debugSettingsPanel != null) {
            debugSettingsPanel.applySettings();
        }
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
        contentArea = null;
    }
}
