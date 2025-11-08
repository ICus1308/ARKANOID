package gamemanager.ui;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;

/**
 * Class đơn giản để áp dụng hình ảnh vào game objects
 */
public class ImageManager {

    /**
     * Áp dụng hình ảnh từ file path vào Rectangle (cho Paddle và Brick)
     */
    public static void applyImage(Rectangle shape, String imagePath) {
        try {
            Image image = new Image(ImageManager.class.getResourceAsStream(imagePath));
            if (!image.isError()) {
                shape.setFill(new ImagePattern(image));
            } else {
                System.err.println("Lỗi load hình: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("Không thể load hình: " + imagePath);
            e.printStackTrace();
        }
    }

    /**
     * Áp dụng hình ảnh từ file path vào Circle (cho Ball)
     */
    public static void applyImage(Circle shape, String imagePath) {
        try {
            Image image = new Image(ImageManager.class.getResourceAsStream(imagePath));
            if (!image.isError()) {
                shape.setFill(new ImagePattern(image));
            } else {
                System.err.println("Lỗi load hình: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("Không thể load hình: " + imagePath);
            e.printStackTrace();
        }
    }

    /**
     * Áp dụng hình ảnh vào bất kỳ Shape nào
     */
    public static void applyImage(Shape shape, String imagePath) {
        try {
            Image image = new Image(ImageManager.class.getResourceAsStream(imagePath));
            if (!image.isError()) {
                shape.setFill(new ImagePattern(image));
            } else {
                System.err.println("Lỗi load hình: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("Không thể load hình: " + imagePath);
            e.printStackTrace();
        }
    }
}
