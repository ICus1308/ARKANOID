package gamemanager.manager;

import java.io.Serial;
import java.io.Serializable;

public class Score implements Serializable {

    @Serial
    private static final long serialVersionUID = 8319232186128534895L;

    private final String player;
    private final int score;

    public Score(String player, int score) {
        this.player = player;
        this.score = score;
    }

    public String getPlayer() {
        return player;
    }

    public int getScore() {
        return score;
    }
}