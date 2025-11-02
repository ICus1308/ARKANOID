package gamemanager;

import gameobject.Brick;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScoreManager extends  GamePlay {
    private static final String HIGH_SCORE_FILE = "highscores.dat";
    private final ObservableList<Score> highScores;
    private int currentScore;

    public ScoreManager() {
        highScores = FXCollections.observableArrayList();
        currentScore = 0;
        loadScores();
    }

    public ObservableList<Score> getScores() {
        return highScores;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void resetCurrentScore() {
        currentScore = 0;
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
        currentScore += score;
        return score;
    }

    public void addScore(String playerName, int score) {
        highScores.add(new Score(playerName, score));
        highScores.sort(Comparator.comparingInt(Score::getScore).reversed());

        if (highScores.size() > 10) {
            highScores.remove(10, highScores.size());
        }
        saveScores();
    }

    @SuppressWarnings("unchecked")
    private void loadScores() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(HIGH_SCORE_FILE))) {
            List<Score> loadedList = (List<Score>) ois.readObject();
            highScores.setAll(loadedList);
        } catch (FileNotFoundException e) {
            System.out.println("High score file not found. A new one will be created.");
        } catch (IOException | ClassNotFoundException e) {
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
