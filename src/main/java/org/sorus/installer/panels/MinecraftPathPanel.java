package org.sorus.installer.panels;

import org.sorus.installer.OS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class MinecraftPathPanel extends BasePanel {

    private final JButton jButton;
    private final JTextField jTextField;

    public MinecraftPathPanel() {
        JLabel jLabel = new JLabel();
        jLabel.setText("Minecraft Directory");
        jLabel.setLocation(135, 175);
        jLabel.setSize(new Dimension(115, 30));
        this.add(jLabel);
        jTextField = new JTextField();
        jTextField.setLocation(100, 215);
        jTextField.setSize(new Dimension(200, 30));
        jTextField.setText(this.getMinecraftPath());
        this.add(jTextField);
        jButton = new JButton();
        jButton.setLocation(10, 105);
        this.add(jButton);
        jButton.setText("...");
        jButton.setSize(new Dimension(30, 30));
        jButton.setLocation(65, 215);
        jButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setSelectedFile(new File(jTextField.getText()));
            int option = fileChooser.showOpenDialog(MinecraftPathPanel.this);
            if(option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                jTextField.setText(file.getAbsolutePath());
            }
        });
        this.add(jButton);
        JButton jButton = new JButton();
        jButton.setLocation(135, 255);
        jButton.setSize(100, 30);
        jButton.setText("Select");
        jButton.addActionListener(this::onSelectButtonPress);
        this.add(jButton);
    }

    private void onSelectButtonPress(ActionEvent e) {
        this.displayPanel(new CreateProfilePanel(jTextField.getText()));
    }

    private String getMinecraftPath() {
        switch(OS.getOS()) {
            case WINDOWS:
                return System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Roaming" + File.separator + ".minecraft";
            default:
                return "";
        }
    }

}
