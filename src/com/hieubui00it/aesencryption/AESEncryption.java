package com.hieubui00it.aesencryption;

import com.hieubui00it.aesencryption.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * @author hieubui00.it
 */

public class AESEncryption {

    // Launch the application
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                new MainFrame().setVisible(true);
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
