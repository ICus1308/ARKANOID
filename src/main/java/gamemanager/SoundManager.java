package gamemanager;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * SoundManager handles all sound effects and background music in the game.
 * Supports volume control, muting, and multiple sound channels.
 */
public class SoundManager extends GamePlay {
    private static SoundManager instance;

    // Sound effect clips for quick playback
    private final Map<SoundType, AudioClip> soundEffects = new HashMap<>();

    // Background music player
    private MediaPlayer musicPlayer;

    // Volume settings
    private double masterVolume = 1.0;
    private double sfxVolume = 1.0;
    private double musicVolume = 0.5;
    private boolean muted = false;

    // Sound types enumeration
    public enum SoundType {
        BALL_PADDLE_HIT,
        BALL_BRICK_HIT,
        BALL_WALL_HIT,
        BRICK_BREAK,
        POWERUP_SPAWN,
        POWERUP_COLLECT,
        GAME_OVER,
        LEVEL_COMPLETE,
        BUTTON_CLICK,
        EXPLOSION,
        MENU_MUSIC,
        GAME_MUSIC
    }

    private SoundManager() {
        loadSoundEffects();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * Load all sound effect files
     */
    private void loadSoundEffects() {
        try {
            // Load sound effects from resources
            loadSound(SoundType.BALL_PADDLE_HIT, "/sounds/paddle_hit.wav");
            loadSound(SoundType.BALL_BRICK_HIT, "/sounds/brick_hit.wav");
            loadSound(SoundType.BALL_WALL_HIT, "/sounds/wall_hit.wav");
            loadSound(SoundType.BRICK_BREAK, "/sounds/brick_break.wav");
            loadSound(SoundType.POWERUP_SPAWN, "/sounds/powerup_spawn.wav");
            loadSound(SoundType.POWERUP_COLLECT, "/sounds/powerup_collect.wav");
            loadSound(SoundType.GAME_OVER, "/sounds/game_over.wav");
            loadSound(SoundType.LEVEL_COMPLETE, "/sounds/level_complete.wav");
            loadSound(SoundType.BUTTON_CLICK, "/sounds/button_click.wav");
            loadSound(SoundType.EXPLOSION, "/sounds/explosion.wav");

            System.out.println("Sound effects loaded successfully!");
        } catch (Exception e) {
            System.err.println("Error loading sound effects: " + e.getMessage());
        }
    }

    /**
     * Load a single sound effect
     */
    private void loadSound(SoundType type, String path) {
        try {
            URL soundURL = getClass().getResource(path);
            if (soundURL != null) {
                AudioClip clip = new AudioClip(soundURL.toExternalForm());
                soundEffects.put(type, clip);
            } else {
                System.err.println("Sound file not found: " + path);
            }
        } catch (Exception e) {
            System.err.println("Failed to load sound: " + path + " - " + e.getMessage());
        }
    }

    /**
     * Play a sound effect
     */
    public void playSound(SoundType type) {
        if (muted) return;

        AudioClip clip = soundEffects.get(type);
        if (clip != null) {
            clip.setVolume(masterVolume * sfxVolume);
            clip.play();
        }
    }

    /**
     * Play a sound effect with custom volume
     */
    public void playSound(SoundType type, double volume) {
        if (muted) return;

        AudioClip clip = soundEffects.get(type);
        if (clip != null) {
            clip.setVolume(Math.min(1.0, masterVolume * sfxVolume * volume));
            clip.play();
        }
    }

    /**
     * Load and play background music
     */
    public void playMusic(SoundType type, boolean loop) {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
        }

        try {
            String path = type == SoundType.MENU_MUSIC ?
                    "/sounds/menu_music.mp3" : "/sounds/game_music.mp3";

            URL musicURL = getClass().getResource(path);
            if (musicURL != null) {
                Media media = new Media(musicURL.toExternalForm());
                musicPlayer = new MediaPlayer(media);
                musicPlayer.setVolume(muted ? 0 : masterVolume * musicVolume);

                if (loop) {
                    musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                }

                musicPlayer.play();
            } else {
                System.err.println("Music file not found: " + path);
            }
        } catch (Exception e) {
            System.err.println("Failed to play music: " + e.getMessage());
        }
    }

    /**
     * Stop background music
     */
    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }

    /**
     * Pause background music
     */
    public void pauseMusic() {
        if (musicPlayer != null) {
            musicPlayer.pause();
        }
    }

    /**
     * Resume background music
     */
    public void resumeMusic() {
        if (musicPlayer != null) {
            musicPlayer.play();
        }
    }

    /**
     * Set master volume (0.0 to 1.0)
     */
    public void setMasterVolume(double volume) {
        this.masterVolume = Math.max(0.0, Math.min(1.0, volume));
        updateMusicVolume();
    }

    /**
     * Set sound effects volume (0.0 to 1.0)
     */
    public void setSfxVolume(double volume) {
        this.sfxVolume = Math.max(0.0, Math.min(1.0, volume));
    }

    /**
     * Set music volume (0.0 to 1.0)
     */
    public void setMusicVolume(double volume) {
        this.musicVolume = Math.max(0.0, Math.min(1.0, volume));
        updateMusicVolume();
    }

    /**
     * Update music player volume
     */
    private void updateMusicVolume() {
        if (musicPlayer != null) {
            musicPlayer.setVolume(muted ? 0 : masterVolume * musicVolume);
        }
    }

    /**
     * Mute/unmute all sounds
     */
    public void setMuted(boolean muted) {
        this.muted = muted;
        updateMusicVolume();
    }

    /**
     * Check if sounds are muted
     */
    public boolean isMuted() {
        return muted;
    }

    /**
     * Get master volume
     */
    public double getMasterVolume() {
        return masterVolume;
    }

    /**
     * Get SFX volume
     */
    public double getSfxVolume() {
        return sfxVolume;
    }

    /**
     * Get music volume
     */
    public double getMusicVolume() {
        return musicVolume;
    }

    /**
     * Clean up resources
     */
    public void dispose() {
        stopMusic();
        if (musicPlayer != null) {
            musicPlayer.dispose();
        }
        soundEffects.clear();
    }
}
