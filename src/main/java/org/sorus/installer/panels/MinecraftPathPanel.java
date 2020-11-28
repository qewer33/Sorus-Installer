package org.sorus.installer.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.sorus.installer.OS;

public class MinecraftPathPanel extends BasePanel {

  private final JTextField jTextField;

  final JFileChooser dirSelect = new JFileChooser();

  public MinecraftPathPanel() {

    try {
      Icon folderIcon =
          new ImageIcon(
              ImageIO.read(
                  MinecraftPathPanel.class.getClassLoader().getResourceAsStream("folder.png")));
      JButton fileChooserButton = new JButton();
      fileChooserButton.setIcon(folderIcon);
      fileChooserButton.setLocation(265, 245);
      fileChooserButton.setSize(50, 30);
      fileChooserButton.addActionListener(this::openFileChooser);
      this.add(fileChooserButton);
    } catch (IOException e) {
      e.printStackTrace();
    }

    JLabel jLabel = new JLabel();
    jLabel.setForeground(Color.WHITE);
    jLabel.setText("Minecraft Directory");
    jLabel.setLocation(
        190 - jLabel.getFontMetrics(jLabel.getFont()).stringWidth("Minecraft Directory") / 2, 215);
    jLabel.setSize(new Dimension(150, 30));
    this.add(jLabel);

    jTextField = new JTextField();
    jTextField.setText(this.getMinecraftPath());
    jTextField.setSize(new Dimension(200, 30));
    jTextField.setLocation(60, 245);
    jTextField.setCaretPosition(jTextField.getText().length());
    this.add(jTextField);

    JButton jButton = new JButton();
    jButton.setText("Next");
    jButton.setSize(new Dimension(100, 30));
    jButton.setLocation(260, 494);
    jButton.addActionListener(this::onNextButtonPress);
    this.add(jButton);
  }

  private void onNextButtonPress(ActionEvent e) {
    this.displayPanel(new CreateProfilePanel(jTextField.getText()));
  }

  private String getMinecraftPath() {
    switch (OS.getOS()) {
      case WINDOWS:
        return System.getProperty("user.home").replace("\\", "/") + "/AppData/Roaming/.minecraft";
      default:
        return "";
    }
  }

  private void openFileChooser(ActionEvent e) {
    dirSelect.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int returnVal = dirSelect.showOpenDialog(null);

    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = dirSelect.getSelectedFile();
      jTextField.setText(file.getAbsolutePath());
    } else {
      jTextField.setText(this.getMinecraftPath());
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawImage(this.bgImage, 0, 0, null);
  }
}
