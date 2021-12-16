package com.hieubui00it.aesencryption.ui.home;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.hieubui00it.aesencryption.util.Constants.*;

/**
 * @author hieubui00.it
 */

public class Home extends JPanel {
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
        setBounds(0, 0, WIDTH_FRAME, HEIGHT_FRAME);
        setBackground(SystemColor.control);
        setForeground(Color.WHITE);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(null);

        setupKeyField();
        setupOriginalTextField();
        setupEncryptField();
        setupDecryptField();

        setObservers();
    }

    private void setupKeyField() {
        JLabel labelKey = new JLabel("Key: ");
        labelKey.setBounds(24, 32, 46, 18);
        labelKey.setFont(new Font(FONT_TAHOMA, Font.BOLD, 16));
        add(labelKey);

        fieldKey = new JTextField();
        fieldKey.setBounds(72, 25, 314, 32);
        fieldKey.setFont(new Font(FONT_TAHOMA, Font.PLAIN, 14));
        fieldKey.setColumns(10);
        add(fieldKey);
    }

    private void setupOriginalTextField() {
        JLabel labelOriginalText = new JLabel("Original text: ");
        labelOriginalText.setBounds(24, 92, 120, 20);
        labelOriginalText.setFont(new Font(FONT_TAHOMA, Font.BOLD, 16));
        add(labelOriginalText);

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
        add(scrollPane);
    }

    private void setupEncryptField() {
        labelEncryptTime = new JLabel();
        labelEncryptTime.setBounds(400, 352, 120, 40);
        labelEncryptTime.setHorizontalAlignment(SwingConstants.CENTER);
        labelEncryptTime.setFont(new Font(FONT_TAHOMA, Font.BOLD, 13));
        add(labelEncryptTime);

        JButton btnEncrypt = new JButton("Encrypt >> ");
        btnEncrypt.setBounds(400, 392, 120, 40);
        btnEncrypt.addActionListener(event -> encrypt());
        add(btnEncrypt);

        JLabel labelEncryptedText = new JLabel("Encrypted text: ");
        labelEncryptedText.setBounds(536, 92, 140, 20);
        labelEncryptedText.setFont(new Font(FONT_TAHOMA, Font.BOLD, 16));
        add(labelEncryptedText);

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
        add(scrollPane);
    }

    public void encrypt() {
        String key = fieldKey.getText().trim();
        String plaintext = fieldOriginalText.getText();
        viewModel.encrypt(key, plaintext);
    }

    private void setupDecryptField() {
        labelDecryptTime = new JLabel();
        labelDecryptTime.setBounds(912, 352, 120, 40);
        labelDecryptTime.setHorizontalAlignment(SwingConstants.CENTER);
        labelDecryptTime.setFont(new Font(FONT_TAHOMA, Font.BOLD, 13));
        add(labelDecryptTime);

        JButton btnDecrypt = new JButton("Decrypt >>");
        btnDecrypt.setBounds(912, 392, 120, 40);
        btnDecrypt.addActionListener(event -> decrypt());
        add(btnDecrypt);

        JLabel labelDecryptedText = new JLabel("Decrypted text: ");
        labelDecryptedText.setBounds(1048, 92, 140, 20);
        labelDecryptedText.setFont(new Font(FONT_TAHOMA, Font.BOLD, 16));
        add(labelDecryptedText);

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
        fieldDecryptedText.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(fieldDecryptedText);
        scrollPane.setBounds(1048, 128, 360, 560);
        add(scrollPane);
    }

    public void decrypt() {
        String key = fieldKey.getText().trim();
        String ciphertext = fieldEncryptedText.getText();
        viewModel.decrypt(key, ciphertext);
    }

    private void setObservers() {
        viewModel.getTextEncrypted().observer(textEncrypted -> fieldEncryptedText.setText(textEncrypted));

        viewModel.getEncryptTime().observer(encryptTime -> labelEncryptTime.setText(encryptTime + " ms"));

        viewModel.getTextDecrypted().observer(textDecrypted -> fieldDecryptedText.setText(textDecrypted));

        viewModel.getDecryptTime().observer(decryptTime -> labelDecryptTime.setText(decryptTime + " ms"));

        viewModel.getErrorMessage().observer(errorMessage -> JOptionPane.showMessageDialog(this, errorMessage));
    }
}
