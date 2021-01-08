package org.sorus.installer.panels;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Objects;

public class InstallationModeInfoPanel extends BasePanel {

    JButton back;

    InstallationModeInfoPanel() throws IOException {
        back = new JButton(" Back");
        ImageIcon i4 = new ImageIcon(ImageIO.read(
                Objects.requireNonNull(SelectInstallationModePanel.class.getClassLoader().getResourceAsStream("back.png"))));
        back.setIcon(i4);
        back.setSize(160,30);
        back.setLocation(-30,494);
        back.setForeground(Color.WHITE);
        back.setContentAreaFilled(false);
        back.addActionListener(this::goBack);
        this.add(back);
    }

    void goBack(ActionEvent e) {
        try {
            this.displayPanel(new SelectInstallationModePanel());
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
