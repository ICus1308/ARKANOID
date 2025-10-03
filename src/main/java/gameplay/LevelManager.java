package gameplay;

import entities.Brick;
import javafx.scene.canvas.GraphicsContext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    private List<Brick> bricks;

    public LevelManager() {
        bricks = new ArrayList<>();
    }

    public void loadLevel(String filename) {
        bricks.clear();
        System.out.println("Loading: " + filename);

        try {
            InputStream is = getClass().getResourceAsStream(filename);
            System.out.println("InputStream: " + is);

            if (is == null) {
                System.out.println("FILE NOT FOUND!");
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            int row = 0;

            while ((line = br.readLine()) != null && row < GameConfig.BRICK_ROWS) {
                System.out.println("Row " + row + ": " + line);
                for (int col = 0; col < line.length() && col < GameConfig.BRICK_COLS; col++) {
                    char c = line.charAt(col);

                    if (c == '1') {
                        double x = GameConfig.BRICK_OFFSET_X + col * (GameConfig.BRICK_W + GameConfig.BRICK_GAP);
                        double y = GameConfig.BRICK_OFFSET_Y + row * (GameConfig.BRICK_H + GameConfig.BRICK_GAP);
                        bricks.add(new Brick(x, y, GameConfig.BRICK_W, GameConfig.BRICK_H));
                    }
                }
                row++;
            }

            br.close();
            System.out.println("Total bricks loaded: " + bricks.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(GraphicsContext gc) {
        System.out.println("Drawing " + bricks.size() + " bricks");
        for (Brick brick : bricks) {
            brick.draw(gc);
        }
    }

    public List<Brick> getBricks() {
        return bricks;
    }

    public int getVisibleBrickCount() {
        int count = 0;
        for (Brick brick : bricks) {
            if (brick.isVisible()) {
                count++;
            }
        }
        return count;
    }
}
