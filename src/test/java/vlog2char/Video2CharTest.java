package vlog2char;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

@Slf4j
public class Video2CharTest {
    @Test
    public void testDirs() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        log.info("tmpDir = {}", tmpDir);

        String workDir = System.getProperty("user.dir");
        log.info("workDir = {}", workDir);

        System.out.println("""


                ============================================================
                All system properties:
                """);
        System.getProperties().forEach((key, value) -> log.info("{} === {}%n", key, value));
    }

    @Test
    @SneakyThrows
    public void testBufferedImage() {
        InputStream fontStream = Video2CharTest.class.getResourceAsStream("/JetBrainsMono-Regular.ttf");
        assert fontStream != null;
        Font monoFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(7F);
        BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        graphics.setFont(monoFont);

        graphics.fillRect(100, 100, 640, 360);
        int textBaseHeight = 500;
        int textLineHeight = 9;
        graphics.drawString("Test char draw", 100, textBaseHeight);
        graphics.setColor(Color.CYAN);
        graphics.drawString("··············", 100, textBaseHeight += textLineHeight);
        graphics.setColor(Color.GREEN);
        graphics.drawString("OOOOOOOOOOOOOO", 100, textBaseHeight += textLineHeight);
        graphics.setColor(Color.MAGENTA);
        graphics.drawString("00000000000000", 100, textBaseHeight += textLineHeight);
        graphics.setColor(Color.CYAN);
        graphics.drawString("00000000000000", 100, textBaseHeight += textLineHeight);
        graphics.setColor(Color.ORANGE);
        graphics.drawString("wwwwwwwwwwwwww", 100, textBaseHeight += textLineHeight);
        graphics.setColor(Color.CYAN);
        graphics.drawString("MMMMMMMMMMMMMM", 100, textBaseHeight += textLineHeight);
        graphics.dispose();

        ImageIO.write(image, "jpg", new File("test.jpg"));
    }
}
