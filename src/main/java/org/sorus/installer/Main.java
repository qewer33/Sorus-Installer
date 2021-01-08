package org.sorus.installer;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class Main {

  public static void main(String[] args) {
    FlatLightLaf.install();
    MainFrame mainFrame = new MainFrame();
    mainFrame.create();
  }
}
