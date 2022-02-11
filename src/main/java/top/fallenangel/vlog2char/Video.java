package top.fallenangel.vlog2char;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Video {
    // 17个字符按照亮度排序，将255级亮度映射到这17个字符上
    private static final String[] pixels = {
            " ", "·", "-", "+", "*", "1", "T", "V", "U", "O",
            "0", "E", "N", "B", "#", "M", "W"
    };
    private final Font monoFont;

    private JPanel mainPane;
    private JTextField videoPathText;
    private JButton selectVideoBtn;
    private JTextField outputPathText;
    private JButton selectOutputPathBtn;
    private JButton transformVideoBtn;
    private JProgressBar transformProgress;
    private JLabel transformProgressLabel;

    @SneakyThrows
    public Video() {
        InputStream fontStream = Video.class.getResourceAsStream("/JetBrainsMono-Regular.ttf");
        assert fontStream != null;
        monoFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(7F);

        selectVideoBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int status = chooser.showOpenDialog(mainPane);

            if (status == JFileChooser.APPROVE_OPTION) {
                File selectedVideo = chooser.getSelectedFile();
                videoPathText.setText(selectedVideo.getAbsolutePath());
            }
        });

        selectOutputPathBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int status = chooser.showOpenDialog(mainPane);

            if (status == JFileChooser.APPROVE_OPTION) {
                File outputPath = chooser.getSelectedFile();
                outputPathText.setText(outputPath.getAbsolutePath());
                createDirs(outputPath.getAbsolutePath());
            }
        });

        transformVideoBtn.addActionListener(e -> {
            if (StringUtils.isNoneBlank(videoPathText.getText(), outputPathText.getText())) {
                startTransform(videoPathText.getText(), outputPathText.getText());
            }
        });
    }

    public void appear() {
        JFrame frame = new JFrame("视频转字符");
        frame.setContentPane(new Video().mainPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createDirs(String baseDir) {
        File originalDir = new File("%s%c%s%c".formatted(baseDir, File.separatorChar, "original", File.separatorChar));
        if (!originalDir.exists()) {
            originalDir.mkdir();
        }

        File resultDir = new File("%s%c%s%c".formatted(baseDir, File.separatorChar, "result", File.separatorChar));
        if (!resultDir.exists()) {
            resultDir.mkdir();
        }
    }

    @SneakyThrows
    private void startTransform(String videoPath, String targetDir) {
        VideoCapture video = new VideoCapture(videoPath);
        int totalFrame = (int) video.get(Videoio.CAP_PROP_FRAME_COUNT);
        MatOfByte videoImage = new MatOfByte();
        MatOfByte charOriginalImage = new MatOfByte();

        new Thread(() -> {
            transformVideoBtn.setEnabled(false);
            for (int i = 0; i < totalFrame; i++) {
                if (video.read(videoImage)) {
                    Imgproc.resize(videoImage, charOriginalImage, new Size(384, 216));
                    //noinspection SpellCheckingInspection
                    Imgcodecs.imwrite("%s%c%s%coriginal_%d.jpg".formatted(targetDir, File.separatorChar, "original", File.separatorChar, i), charOriginalImage);
                    Imgproc.cvtColor(charOriginalImage, charOriginalImage, Imgproc.COLOR_BGR2RGB);

                    BufferedImage charImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
                    Graphics graphics = charImage.getGraphics();
                    graphics.setFont(monoFont);

                    for (int j = 0; j < charOriginalImage.rows(); j++) {
                        for (int k = 0; k < charOriginalImage.cols(); k++) {
                            int[] color = Arrays.stream(charOriginalImage.get(j, k)).mapToInt(c -> (int) c).toArray();
                            int light = (int) (0.299 * color[0] + 0.587 * color[1] + 0.114 * color[2]);
                            int charOfPixel = (int) (light / 256.0 * 17);
                            graphics.setColor(new Color(color[0], color[1], color[2]));
                            graphics.drawString(pixels[charOfPixel], k * 5, j * 5);
                        }
                    }

                    graphics.dispose();

                    try {
                        //noinspection SpellCheckingInspection
                        ImageIO.write(charImage, "jpg", new File("%s%c%s%cresult_%d.jpg".formatted(targetDir, File.separatorChar, "result", File.separatorChar, i)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    int process = (int) (((double) i / totalFrame) * 100);
                    SwingUtilities.invokeLater(() -> {
                        transformProgress.setValue(process);
                        transformProgressLabel.setText("%d%%".formatted(process));
                    });
                }
            }
            transformProgress.setValue(100);
            transformProgressLabel.setText("100%");
            transformVideoBtn.setEnabled(true);
        }).start();
    }
}
