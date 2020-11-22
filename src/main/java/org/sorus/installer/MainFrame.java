package org.sorus.installer;

import org.sorus.installer.panels.MinecraftPathPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public void create() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 380, height = 570;
        this.setTitle("Sorus Installer");
        this.setLocation((int) (dimension.getWidth() / 2 - width / 2), (int) (dimension.getHeight() / 2 - height / 2));
        this.setSize(width, height);
        this.setResizable(false);
        this.initializeComponents();
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void initializeComponents() {
        CardLayout cardLayout = new CardLayout();
        this.getContentPane().setLayout(cardLayout);
        this.getContentPane().add(new MinecraftPathPanel(), "origin");
        cardLayout.show(this.getContentPane(), "origin");
    }

}
