package com.hieubui00it.aesencryption.ui;

import com.hieubui00it.aesencryption.ui.home.Home;

import javax.swing.*;

import static com.hieubui00it.aesencryption.util.Constants.HEIGHT_FRAME;
import static com.hieubui00it.aesencryption.util.Constants.WIDTH_FRAME;

/**
 * @author hieubui00.it
 */

public class MainFrame extends JFrame {

    public MainFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("AES Encryption");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(0, 0, WIDTH_FRAME, HEIGHT_FRAME);
        setLocationRelativeTo(null);
        setResizable(false);

        setContentPane(new Home());
    }
}
