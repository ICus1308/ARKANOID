package gamemanager.ui;

import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import static gameconfig.GameConfig.*;

public class VideoBackgroundManager {
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private final Pane root;
    private final String videoPath;

    public VideoBackgroundManager(Pane root, String videoPath) {
        this.root = root;
        this.videoPath = videoPath;
        loadVideo(videoPath);
    }

    private void loadVideo(String path) {
        try {
            // Load video
            String videoURL = getClass().getResource(path).toExternalForm();
            Media media = new Media(videoURL);
            mediaPlayer = new MediaPlayer(media);
            mediaView = new MediaView(mediaPlayer);

            // Set size - fill toàn màn hình
            mediaView.setFitWidth(GAME_WIDTH);
            mediaView.setFitHeight(GAME_HEIGHT);
            mediaView.setPreserveRatio(false); // Không giữ tỷ lệ - fill full màn hình

            // Loop vô hạn
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setVolume(0); // Tắt tiếng

            // Add vào root
            root.getChildren().add(0, mediaView);
            mediaView.toBack();

            // Play
            mediaPlayer.play();

            System.out.println("Video loaded: " + path);
        } catch (Exception e) {
            System.err.println("Không load được video: " + path);
            e.printStackTrace();
        }
    }

    /**
     * Update video size when resolution changes
     */
    public void updateSize() {
        if (mediaView != null) {
            mediaView.setFitWidth(GAME_WIDTH);
            mediaView.setFitHeight(GAME_HEIGHT);
            System.out.println("Video resized to: " + GAME_WIDTH + "x" + GAME_HEIGHT);
        }
    }

    /**
     * Dispose video properly - cleanup all resources
     * CRITICAL for preventing memory leaks
     */
    public void dispose() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        if (mediaView != null && mediaView.getParent() != null) {
            root.getChildren().remove(mediaView);
            mediaView = null;
        }
        System.out.println("Video disposed");
    }

    /**
     * Restart video after resolution change
     * Dispose old video and load new one
     */
    public void restart() {
        dispose();
        loadVideo(videoPath);
    }

    /**
     * Pause video playback
     */
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    /**
     * Resume video playback
     */
    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }
}
