package gamemanager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Simple coin and skin persistence using a properties file.
 */
public class CoinManager extends GamePlay {
    private static final String COIN_FILE = "coins.properties";
    private int coins = 0;
    private final Set<String> ownedSkins = new HashSet<>();
    private String selectedPaddleSkin = "default";
    private String selectedBallSkin = "default";

    public CoinManager() {
        load();
    }

    public int getCoins() {
        return coins;
    }

    public void addCoins(int amount) {
        if (amount <= 0) return;
        coins += amount;
        save();
    }

    public boolean spendCoins(int amount) {
        if (amount <= 0) return false;
        if (coins < amount) return false;
        coins -= amount;
        save();
        return true;
    }

    public boolean isSkinOwned(String category, String skin) {
        if (skin == null || category == null) return false;
        return ownedSkins.contains(category + ":" + skin);
    }

    public boolean buySkin(String category, String skin, int price) {
        if (isSkinOwned(category, skin)) return false;
        if (!spendCoins(price)) return false;
        ownedSkins.add(category + ":" + skin);
        save();
        return true;
    }

    public String getSelectedPaddleSkin() {
        return selectedPaddleSkin;
    }

    public String getSelectedBallSkin() {
        return selectedBallSkin;
    }

    public boolean setSelectedPaddleSkin(String skin) {
        if (skin == null) return false;
        if (!isSkinOwned("paddle", skin) && !"default".equals(skin)) return false;
        selectedPaddleSkin = skin;
        save();
        return true;
    }

    public boolean setSelectedBallSkin(String skin) {
        if (skin == null) return false;
        if (!isSkinOwned("ball", skin) && !"default".equals(skin)) return false;
        selectedBallSkin = skin;
        save();
        return true;
    }

    private void load() {
        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(COIN_FILE)) {
            p.load(fis);
            coins = Integer.parseInt(p.getProperty("coins", "0"));
            selectedPaddleSkin = p.getProperty("selectedPaddle", p.getProperty("selected", "default"));
            selectedBallSkin = p.getProperty("selectedBall", p.getProperty("selected", "default"));
            for (String name : p.stringPropertyNames()) {
                if (name.startsWith("owned.")) {
                    String skin = name.substring("owned.".length());
                    if ("true".equalsIgnoreCase(p.getProperty(name))) {
                        ownedSkins.add(skin);
                    }
                }
            }
        } catch (IOException e) {
            // file not found: initialize defaults
            coins = 0;
            ownedSkins.clear();
            // default ownership: both categories have 'default'
            ownedSkins.add("paddle:default");
            ownedSkins.add("ball:default");
            selectedPaddleSkin = "default";
            selectedBallSkin = "default";
        }
    }

    private void save() {
        Properties p = new Properties();
        p.setProperty("coins", Integer.toString(coins));
        p.setProperty("selectedPaddle", selectedPaddleSkin == null ? "default" : selectedPaddleSkin);
        p.setProperty("selectedBall", selectedBallSkin == null ? "default" : selectedBallSkin);
        for (String skin : ownedSkins) {
            p.setProperty("owned." + skin, "true");
        }
        try (FileOutputStream fos = new FileOutputStream(COIN_FILE)) {
            p.store(fos, "Coin and skin data");
        } catch (IOException e) {
            System.err.println("Failed to save coin data: " + e.getMessage());
        }
    }
}
