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
    private String selectedSkin = "default";

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

    public boolean isSkinOwned(String skin) {
        if (skin == null) return false;
        return ownedSkins.contains(skin);
    }

    public boolean buySkin(String skin, int price) {
        if (isSkinOwned(skin)) return false;
        if (!spendCoins(price)) return false;
        ownedSkins.add(skin);
        save();
        return true;
    }

    public String getSelectedSkin() {
        return selectedSkin;
    }

    public boolean setSelectedSkin(String skin) {
        if (skin == null) return false;
        if (!isSkinOwned(skin) && !"default".equals(skin)) return false;
        selectedSkin = skin;
        save();
        return true;
    }

    private void load() {
        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(COIN_FILE)) {
            p.load(fis);
            coins = Integer.parseInt(p.getProperty("coins", "0"));
            selectedSkin = p.getProperty("selected", "default");
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
            ownedSkins.add("default");
            selectedSkin = "default";
        }
    }

    private void save() {
        Properties p = new Properties();
        p.setProperty("coins", Integer.toString(coins));
        p.setProperty("selected", selectedSkin == null ? "default" : selectedSkin);
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

