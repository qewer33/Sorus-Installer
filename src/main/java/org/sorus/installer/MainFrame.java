package org.sorus.installer;

import org.sorus.installer.panels.MinecraftPathPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class MainFrame extends JFrame {

    public void create() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = 380, y = 540;
        this.setLocation((int) (dimension.getWidth() / 2 - x / 2), (int) (dimension.getHeight() / 2 - y / 2));
        this.setSize(x, y);
        this.setResizable(false);
        this.initializeComponents();
        this.setVisible(true);
        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
            @Override
            public void windowClosed(WindowEvent e) {

            }
            @Override
            public void windowIconified(WindowEvent e) {

            }
            @Override
            public void windowDeiconified(WindowEvent e) {

            }
            @Override
            public void windowActivated(WindowEvent e) {

            }
            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
    }

    private void initializeComponents() {
        CardLayout cardLayout = new CardLayout();
        this.getContentPane().setLayout(cardLayout);
        this.getContentPane().add(new MinecraftPathPanel(), "origin");
        cardLayout.show(this.getContentPane(), "origin");
    }

}
