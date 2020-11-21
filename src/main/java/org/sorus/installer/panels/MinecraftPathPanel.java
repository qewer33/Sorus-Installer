package org.sorus.installer.panels;

import org.sorus.installer.OS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MinecraftPathPanel extends BasePanel {

    private final JTextField jTextField;

    public MinecraftPathPanel() {
        JLabel jLabel = new JLabel();
        jLabel.setText("Minecraft Directory");
        jLabel.setLocation(135, 175);
        jLabel.setSize(new Dimension(115, 30));
        this.add(jLabel);
        jTextField = new JTextField();
        jTextField.setText(this.getMinecraftPath());
        jTextField.setSize(new Dimension(250, 30));
        jTextField.setLocation(65, 205);
        jTextField.setCaretPosition(jTextField.getText().length());
        this.add(jTextField);
        JButton jButton = new JButton();
        jButton.setText("Next");
        jButton.setSize(new Dimension(100, 30));
        jButton.setLocation(140, 245);
        jButton.addActionListener(this::onNextButtonPress);
        this.add(jButton);
    }

    private void onNextButtonPress(ActionEvent e) {
        this.displayPanel(new CreateProfilePanel(jTextField.getText()));
    }

    private String getMinecraftPath() {
        switch(OS.getOS()) {
            case WINDOWS:
                return System.getProperty("user.home").replace("\\", "/") + "/AppData/Roaming/.minecraft";
            default:
                return "";
        }
    }

}
