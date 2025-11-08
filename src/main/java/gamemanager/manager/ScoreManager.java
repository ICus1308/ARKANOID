package gamemanager.manager;

import gameobject.brick.Brick;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScoreManager {

    private static final String HIGH_SCORE_FILE = "highscores.dat";
    private static final int MAX_HIGH_SCORES = 10;

    private final ObservableList<Score> highScores;

    public ScoreManager() {
        highScores = FXCollections.observableArrayList();
        loadScores();
    }

    public ObservableList<Score> getScores() {
        return highScores;
    }

    public int calculateBrickScore(Brick brick, boolean oneshotActive) {
        int score;
        if (oneshotActive) {
            if (brick.getHitCount() > 0) {
                score = 10;
                brick.destroy();
            } else {
                score = 0;
            }
        } else {
            score = brick.hit();
        }
        return score;
    }

    public void addScore(String playerName, int score) {
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player";
        }
        highScores.add(new Score(playerName, score));
        highScores.sort(Comparator.comparingInt(Score::getScore).reversed());

        if (highScores.size() > MAX_HIGH_SCORES) {
            highScores.remove(MAX_HIGH_SCORES, highScores.size());
        }
        saveScores();
    }

    @SuppressWarnings("unchecked")
    private void loadScores() {
        File scoreFile = new File(HIGH_SCORE_FILE);
        if (!scoreFile.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(scoreFile))) {
            List<Score> loadedList = (List<Score>) ois.readObject();
            highScores.setAll(loadedList);
        } catch (ClassNotFoundException e) {
            System.err.println("Error loading scores (incompatible version): " + e.getMessage());
            if (scoreFile.delete()) {
                System.out.println("Deleted corrupted high score file.");
            }
        } catch (IOException e) {
            System.err.println("Error loading scores: " + e.getMessage());
        }
    }

    private void saveScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(HIGH_SCORE_FILE))) {
            oos.writeObject(new ArrayList<>(highScores));
        } catch (IOException e) {
            System.err.println("Error saving scores: " + e.getMessage());
        }
    }
}
