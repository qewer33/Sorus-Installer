package org.sorus.installer;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.sorus.installer.panels.MinecraftPathPanel;

public class MainFrame extends JFrame {

  private BufferedImage logo;

  public void create() {
    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();

    try {
      logo =
          ImageIO.read(MinecraftPathPanel.class.getClassLoader().getResourceAsStream("logo.png"));
    } catch (Exception e) {
      e.printStackTrace();
    }

    int width = 380, height = 570;
    this.setTitle("Sorus Installer");
    this.setIconImage(logo);
    this.setLocation(
        (int) (dimension.getWidth() / 2 - width / 2),
        (int) (dimension.getHeight() / 2 - height / 2));
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
