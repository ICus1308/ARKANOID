package userinterface;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScoreManager {
    private static final String HIGH_SCORE_FILE = "highscores.dat";
    private final ObservableList<Score> highScores;

    public ScoreManager() {
        highScores = FXCollections.observableArrayList();
        loadScores();
    }

    public ObservableList<Score> getScores() {
        return highScores;
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
