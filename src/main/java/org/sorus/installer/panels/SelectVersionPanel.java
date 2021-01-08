package org.sorus.installer.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class SelectVersionPanel extends BasePanel {

    JLabel label;
    JButton injectButton;
    JButton optifineButton;
    JButton forgeButton;

    SelectVersionPanel() {
        label = new JLabel("Installation Mode");
        label.setSize(300,20);
        label.setLocation(
                190 - label.getFontMetrics(label.getFont()).stringWidth(label.getText()) / 2,
                215);
        label.setForeground(Color.WHITE);
        this.add(label);

        injectButton = new JButton("Injection");
        injectButton.setSize(130,30);
        injectButton.setLocation(125,265);
        injectButton.setFocusable(false);
        injectButton.addActionListener(this::openInjectionPanel);
        this.add(injectButton);

        optifineButton = new JButton("Optifine");
        optifineButton.setSize(130,30);
        optifineButton.setLocation(125,310);
        optifineButton.setFocusable(false);
        optifineButton.addActionListener(this::openOptifinePanel);
        this.add(optifineButton);

        forgeButton = new JButton("Forge");
        forgeButton.setSize(130,30);
        forgeButton.setLocation(125,355);
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
