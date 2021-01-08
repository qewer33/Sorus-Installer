package org.sorus.installer.panels;

import java.awt.*;

public class ForgeInstallationPanel extends BasePanel {

    ForgeInstallationPanel() {

    }

    // This function overrides the panels paintComponent method and sets the background image
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(this.bgImage, 0, 0, null);
    }

}
