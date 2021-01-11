package org.sorus.installer.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.*;

public class SelectInstallationModePanel extends BasePanel {

    private final String minecraftPath;
    private JLabel label;
    private JButton javaagentButton;
    private JButton launcherwrapperButton;
    private JButton otherButton;
    private JButton learnMore;

    public SelectInstallationModePanel(String minecraftPath) throws IOException {
        this.minecraftPath = minecraftPath;
        label = new JLabel("Installation Mode");
        label.setSize(300,20);
        label.setLocation(
                190 - label.getFontMetrics(label.getFont()).stringWidth(label.getText()) / 2,
                215);
        label.setForeground(Color.WHITE);
        this.add(label);

        javaagentButton = new JButton();
        ImageIcon i1 = new ImageIcon(ImageIO.read(
                Objects.requireNonNull(SelectInstallationModePanel.class.getClassLoader().getResourceAsStream("javalogo.png"))));
        javaagentButton.setIcon(i1);
        javaagentButton.setToolTipText("Java Agent");
        javaagentButton.setSize(96,96);
        javaagentButton.setLocation(21,245);
        javaagentButton.setFocusable(false);
        javaagentButton.addActionListener(this::openInjectionPanel);
        this.add(javaagentButton);

        launcherwrapperButton = new JButton();
        ImageIcon i2 = new ImageIcon(ImageIO.read(
                Objects.requireNonNull(SelectInstallationModePanel.class.getClassLoader().getResourceAsStream("mclogo.png"))));
        launcherwrapperButton.setIcon(i2);
        launcherwrapperButton.setToolTipText("Launcher Wrapper");
        launcherwrapperButton.setSize(96,96);
        launcherwrapperButton.setLocation(140,245);
        launcherwrapperButton.setFocusable(false);
        launcherwrapperButton.addActionListener(this::openLaunchWrapperPanel);
        this.add(launcherwrapperButton);

        otherButton = new JButton();
        ImageIcon i3 = new ImageIcon(ImageIO.read(
                Objects.requireNonNull(SelectInstallationModePanel.class.getClassLoader().getResourceAsStream("questionmark.png"))));
        otherButton.setIcon(i3);
        //otherButton.setToolTipText("");
        otherButton.setSize(96,96);
        otherButton.setLocation(259,245);
        otherButton.setFocusable(false);
        otherButton.addActionListener(this::openForgePanel);
        otherButton.setEnabled(false);
        this.add(otherButton);

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
        this.displayPanel(new InjectionInstallationPanel(minecraftPath));
    }

    void openLaunchWrapperPanel(ActionEvent e) {
        this.displayPanel(new LaunchWrapperInstallationPanel(minecraftPath));
    }

    void openForgePanel(ActionEvent e) {
        this.displayPanel(new ForgeInstallationPanel());
    }

    void openInfoPanel(ActionEvent e) {
        try {
            this.displayPanel(new InstallationModeInfoPanel(minecraftPath));
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
