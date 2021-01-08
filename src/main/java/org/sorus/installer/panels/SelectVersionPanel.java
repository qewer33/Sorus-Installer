package org.sorus.installer.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class SelectVersionPanel extends BasePanel {

    JLabel label;
    JButton injectButton;
    JButton optifineButton;
    JButton forgeButton;
    JButton learnMore;

    SelectVersionPanel() throws IOException {
        label = new JLabel("Installation Mode");
        label.setSize(300,20);
        label.setLocation(
                190 - label.getFontMetrics(label.getFont()).stringWidth(label.getText()) / 2,
                215);
        label.setForeground(Color.WHITE);
        this.add(label);

        injectButton = new JButton();
        ImageIcon i1 = new ImageIcon(ImageIO.read(
                SelectVersionPanel.class.getClassLoader().getResourceAsStream("mclogo.png")));
        injectButton.setIcon(i1);
        injectButton.setToolTipText("Injection");
        injectButton.setSize(96,96);
        injectButton.setLocation(21,255);
        injectButton.setFocusable(false);
        injectButton.addActionListener(this::openInjectionPanel);
        this.add(injectButton);

        optifineButton = new JButton();
        ImageIcon i2 = new ImageIcon(ImageIO.read(
                SelectVersionPanel.class.getClassLoader().getResourceAsStream("oflogo.png")));
        optifineButton.setIcon(i2);
        optifineButton.setToolTipText("Optifine");
        optifineButton.setSize(96,96);
        optifineButton.setLocation(140,255);
        optifineButton.setFocusable(false);
        optifineButton.addActionListener(this::openOptifinePanel);
        this.add(optifineButton);

        forgeButton = new JButton();
        ImageIcon i3 = new ImageIcon(ImageIO.read(
                SelectVersionPanel.class.getClassLoader().getResourceAsStream("forgelogo.png")));
        forgeButton.setIcon(i3);
        forgeButton.setToolTipText("Forge");
        forgeButton.setSize(96,96);
        forgeButton.setLocation(259,255);
        forgeButton.setFocusable(false);
        forgeButton.addActionListener(this::openForgePanel);
        this.add(forgeButton);
    }

    void openInjectionPanel(ActionEvent e) {
        this.displayPanel(new InjectionInstallationPanel());
    }

    void openOptifinePanel(ActionEvent e) {
        this.displayPanel(new OptifineInstallationPanel());
    }

    void openForgePanel(ActionEvent e) {
        this.displayPanel(new ForgeInstallationPanel());
    }

    // This function overrides the panels paintComponent method and sets the background image
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(this.bgImage, 0, 0, null);
    }

}
