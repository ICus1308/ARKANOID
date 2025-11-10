package gamemanager.ui;

import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import static gameconfig.GameConfig.*;

public class VideoBackgroundManager {
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;

    public VideoBackgroundManager(Pane root, String videoPath) {
        try {
            // Load video
            String videoURL = getClass().getResource(videoPath).toExternalForm();
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

        } catch (Exception e) {
            System.err.println("Không load được video: " + videoPath);
            e.printStackTrace();
        }
    }

    // Gọi method này khi đổi resolution
    public void updateSize() {
        if (mediaView != null) {
            mediaView.setFitWidth(GAME_WIDTH);
            mediaView.setFitHeight(GAME_HEIGHT);
        }
    }
}



