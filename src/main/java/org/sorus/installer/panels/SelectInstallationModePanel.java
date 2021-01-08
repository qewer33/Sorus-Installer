package org.sorus.installer.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.*;

public class SelectInstallationModePanel extends BasePanel {

    JLabel label;
    JButton injectButton;
    JButton optifineButton;
    JButton forgeButton;
    JButton learnMore;

    SelectInstallationModePanel() throws IOException {
        label = new JLabel("Installation Mode");
        label.setSize(300,20);
        label.setLocation(
                190 - label.getFontMetrics(label.getFont()).stringWidth(label.getText()) / 2,
                215);
        label.setForeground(Color.WHITE);
        this.add(label);

        injectButton = new JButton();
        ImageIcon i1 = new ImageIcon(ImageIO.read(
                Objects.requireNonNull(SelectInstallationModePanel.class.getClassLoader().getResourceAsStream("mclogo.png"))));
        injectButton.setIcon(i1);
        injectButton.setToolTipText("Injection");
        injectButton.setSize(96,96);
        injectButton.setLocation(21,245);
        injectButton.setFocusable(false);
        injectButton.addActionListener(this::openInjectionPanel);
        this.add(injectButton);

        optifineButton = new JButton();
        ImageIcon i2 = new ImageIcon(ImageIO.read(
                Objects.requireNonNull(SelectInstallationModePanel.class.getClassLoader().getResourceAsStream("oflogo.png"))));
        optifineButton.setIcon(i2);
        optifineButton.setToolTipText("Optifine");
        optifineButton.setSize(96,96);
        optifineButton.setLocation(140,245);
        optifineButton.setFocusable(false);
        optifineButton.addActionListener(this::openOptifinePanel);
        this.add(optifineButton);

        forgeButton = new JButton();
        ImageIcon i3 = new ImageIcon(ImageIO.read(
                Objects.requireNonNull(SelectInstallationModePanel.class.getClassLoader().getResourceAsStream("forgelogo.png"))));
        forgeButton.setIcon(i3);
        forgeButton.setToolTipText("Forge");
        forgeButton.setSize(96,96);
        forgeButton.setLocation(259,245);
        forgeButton.setFocusable(false);
        forgeButton.addActionListener(this::openForgePanel);
        this.add(forgeButton);

        learnMore = new JButton(" Learn More");
        ImageIcon i4 = new ImageIcon(ImageIO.read(
                Objects.requireNonNull(SelectInstallationModePanel.class.getClassLoader().getResourceAsStream("info.png"))));
        learnMore.setIcon(i4);
        learnMore.setSize(160,30);
        learnMore.setLocation(-10,494);
        learnMore.setForeground(Color.WHITE);
        learnMore.setContentAreaFilled(false);
        learnMore.addActionListener(this::openInfoPanel);
        this.add(learnMore);

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

    void openInfoPanel(ActionEvent e) {
        try {
            this.displayPanel(new InstallationModeInfoPanel());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // This function overrides the panels paintComponent method and sets the background image
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(this.bgImage, 0, 0, null);
    }

}
