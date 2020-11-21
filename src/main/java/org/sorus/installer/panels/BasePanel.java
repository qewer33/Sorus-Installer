package org.sorus.installer.panels;

import javax.swing.*;
import java.awt.*;

public class BasePanel extends JPanel {

    public BasePanel() {
        this.setLayout(null);
    }

    protected void displayPanel(BasePanel basePanel) {
        Container container = this.getParent();
        String name = String.valueOf(container.getComponents().length);
        container.add(basePanel, name);
        ((CardLayout) container.getLayout()).show(container, name);
    }

}
