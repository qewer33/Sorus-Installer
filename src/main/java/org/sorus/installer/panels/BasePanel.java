package org.sorus.installer.panels;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class BasePanel extends JPanel {

  protected BufferedImage bgImage;

  public BasePanel() {
    this.setLayout(null);
    try {
      bgImage =
          ImageIO.read(BasePanel.class.getClassLoader().getResourceAsStream("background.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected void displayPanel(CreateProfilePanel basePanel) {
    Container container = this.getParent();
    String name = String.valueOf(container.getComponents().length);
    container.add(basePanel, name);
    ((CardLayout) container.getLayout()).show(container, name);
  }
}
