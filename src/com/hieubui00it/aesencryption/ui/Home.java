package com.hieubui00it.aesencryption.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author hieubui00.it
 */

public class Home extends JFrame {
    public static String FONT_TAHOMA = "Tahoma";

    private JPanel panelContent;
    private JLabel labelEncryptTime;
    private JLabel labelDecryptTime;
    private JTextField fieldKey;
    private JTextArea fieldOriginalText;
    private JTextArea fieldEncryptedText;
    private JTextArea fieldDecryptedText;

    private final HomeViewModel viewModel = new HomeViewModel();

    public Home() {
        initComponents();
    }

    private void initComponents() {
        setTitle("AES Encryption");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(0, 0, 1448, 760);
        setLocationRelativeTo(null);
        setResizable(false);

        setupContentPanel();
        setupKeyField();
        setupOriginalTextField();
        setupEncryptField();
        setupDecryptField();
    }

    private void setupContentPanel() {
        panelContent = new JPanel();
        panelContent.setBackground(SystemColor.control);
        panelContent.setForeground(Color.WHITE);
        panelContent.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(panelContent);
        panelContent.setLayout(null);
    }

    private void setupKeyField() {
        JLabel labelKey = new JLabel("Key: ");
        labelKey.setBounds(24, 32, 46, 18);
        labelKey.setFont(new Font(FONT_TAHOMA, Font.BOLD, 16));
        panelContent.add(labelKey);

        fieldKey = new JTextField();
        fieldKey.setBounds(72, 25, 314, 32);
        fieldKey.setFont(new Font(FONT_TAHOMA, Font.PLAIN, 14));
        panelContent.add(fieldKey);
        fieldKey.setColumns(10);
    }

    private void setupOriginalTextField() {
        JLabel labelOriginalText = new JLabel("Original text: ");
        labelOriginalText.setBounds(24, 92, 120, 20);
        labelOriginalText.setFont(new Font(FONT_TAHOMA, Font.BOLD, 16));
        panelContent.add(labelOriginalText);

        fieldOriginalText = new JTextArea();
        fieldOriginalText.setBounds(24, 128, 360, 560);
        fieldOriginalText.setLineWrap(true);
        fieldOriginalText.setWrapStyleWord(true);
        fieldOriginalText.setFont(new Font(FONT_TAHOMA, Font.PLAIN, 14));
        fieldOriginalText.setBorder(
            BorderFactory.createCompoundBorder(
                fieldOriginalText.getBorder(),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)
            )
        );

        JScrollPane scrollPane = new JScrollPane(fieldOriginalText);
        scrollPane.setBounds(24, 128, 360, 560);
        panelContent.add(scrollPane);
    }

    private void setupEncryptField() {
        labelEncryptTime = new JLabel();
        labelEncryptTime.setBounds(400, 352, 120, 40);
        labelEncryptTime.setHorizontalAlignment(SwingConstants.CENTER);
        labelEncryptTime.setFont(new Font(FONT_TAHOMA, Font.BOLD, 13));
        panelContent.add(labelEncryptTime);

        JButton btnEncrypt = new JButton("Encrypt >> ");
        btnEncrypt.setBounds(400, 392, 120, 40);
        btnEncrypt.addActionListener(event -> encrypt());
        panelContent.add(btnEncrypt);

        JLabel labelEncryptedText = new JLabel("Encrypted text: ");
        labelEncryptedText.setBounds(536, 92, 140, 20);
        labelEncryptedText.setFont(new Font(FONT_TAHOMA, Font.BOLD, 16));
        panelContent.add(labelEncryptedText);

        fieldEncryptedText = new JTextArea();
        fieldEncryptedText.setBounds(536, 128, 360, 560);
        fieldEncryptedText.setLineWrap(true);
        fieldEncryptedText.setWrapStyleWord(true);
        fieldEncryptedText.setFont(new Font(FONT_TAHOMA, Font.PLAIN, 14));
        fieldEncryptedText.setBorder(
            BorderFactory.createCompoundBorder(
                fieldEncryptedText.getBorder(),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)
            )
        );

        JScrollPane scrollPane = new JScrollPane(fieldEncryptedText);
        scrollPane.setBounds(536, 128, 360, 560);
        panelContent.add(scrollPane);
    }

    private void setupDecryptField() {
        labelDecryptTime = new JLabel();
        labelDecryptTime.setBounds(912, 352, 120, 40);
        labelDecryptTime.setHorizontalAlignment(SwingConstants.CENTER);
        labelDecryptTime.setFont(new Font(FONT_TAHOMA, Font.BOLD, 13));
        panelContent.add(labelDecryptTime);

        JButton btnDecrypt = new JButton("Decrypt >>");
        btnDecrypt.setBounds(912, 392, 120, 40);
        btnDecrypt.addActionListener(event -> decrypt());
        panelContent.add(btnDecrypt);

        JLabel labelDecryptedText = new JLabel("Decrypted text: ");
        labelDecryptedText.setBounds(1048, 92, 140, 20);
        labelDecryptedText.setFont(new Font(FONT_TAHOMA, Font.BOLD, 16));
        panelContent.add(labelDecryptedText);

        fieldDecryptedText = new JTextArea();
        fieldDecryptedText.setBounds(1048, 128, 360, 560);
        fieldDecryptedText.setLineWrap(true);
        fieldDecryptedText.setWrapStyleWord(true);
        fieldDecryptedText.setFont(new Font(FONT_TAHOMA, Font.PLAIN, 14));
        fieldDecryptedText.setBorder(
            BorderFactory.createCompoundBorder(
                fieldDecryptedText.getBorder(),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)
            )
        );
        fieldDecryptedText.setFocusable(false);

        JScrollPane scrollPane = new JScrollPane(fieldDecryptedText);
        scrollPane.setBounds(1048, 128, 360, 560);
        panelContent.add(scrollPane);
    }

    public void encrypt() {
        String key = fieldKey.getText().trim();
        String plaintext = fieldOriginalText.getText();

        long startTime = System.nanoTime();
        String encryptedText = viewModel.encrypt(key, plaintext);
        long encryptTime = System.nanoTime() - startTime;

        labelEncryptTime.setText(encryptTime + " ns");
        fieldEncryptedText.setText(encryptedText);
    }

    public void decrypt() {
        String key = fieldKey.getText().trim();
        String ciphertext = fieldEncryptedText.getText();

        long startTime = System.nanoTime();
        String decryptedText = viewModel.decrypt(key, ciphertext);
        long decryptTime = System.nanoTime() - startTime;

        if (decryptedText == null) {
            JOptionPane.showMessageDialog(rootPane, "Invalid key or ciphertext");
        }

        labelDecryptTime.setText(decryptTime + " ns");
        fieldDecryptedText.setText(decryptedText);
    }
}
