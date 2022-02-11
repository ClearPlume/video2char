package top.fallenangel.vlog2char;

import lombok.SneakyThrows;

import javax.swing.*;
import java.io.*;

public class Video2Char {
    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Video2Char instance = new Video2Char();
        instance.start();
        instance.run();
        instance.end();
    }

    @SneakyThrows
    public void start() {
        // 加载opencv文件
        InputStream streamTmp = getClass().getResourceAsStream("/opencv_java455.dll");
        assert streamTmp != null;
        BufferedInputStream opencvStream = new BufferedInputStream(streamTmp);
        byte[] opencvData = new byte[opencvStream.available()];
        //noinspection ResultOfMethodCallIgnored
        opencvStream.read(opencvData);
        opencvStream.close();

        // 写入临时文件夹
        File tempOpencv = File.createTempFile("opencv", ".dll");
        BufferedOutputStream opencvFile = new BufferedOutputStream(new FileOutputStream(tempOpencv));
        opencvFile.write(opencvData);
        opencvFile.flush();
        opencvFile.close();
        tempOpencv.deleteOnExit();

        // 载入依赖
        System.load(tempOpencv.getAbsolutePath());
    }

    public void run() {
        Video video = new Video();
        video.appear();
    }

    @SneakyThrows
    public void end() {
    }
}
