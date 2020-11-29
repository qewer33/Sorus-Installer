package org.sorus.installer.panels;

import org.sorus.installer.OS;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class MinecraftPathPanel extends BasePanel {

    private JButton fileChooserButton;
    private JTextField mcdirField;
    private JLabel mcdirLabel;
    private JButton nextButton;
    private JLabel errorLabel;

    final JFileChooser dirSelect = new JFileChooser();

    // Constructor
    public MinecraftPathPanel() {

        try {
            Icon folderIcon = new ImageIcon(ImageIO.read(MinecraftPathPanel.class.getClassLoader().getResourceAsStream("folder.png")));
            fileChooserButton = new JButton();
            fileChooserButton.setIcon(folderIcon);
            fileChooserButton.setLocation(265, 245);
            fileChooserButton.setSize(50, 30);
            fileChooserButton.setFocusable(false);
            fileChooserButton.addActionListener(this::openFileChooser);
            this.add(fileChooserButton);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mcdirLabel = new JLabel();
        mcdirLabel.setForeground(Color.WHITE);
        mcdirLabel.setText("Minecraft Directory");
        mcdirLabel.setLocation(190 - mcdirLabel.getFontMetrics(mcdirLabel.getFont()).stringWidth("Minecraft Directory") / 2, 215);
        mcdirLabel.setSize(150, 30);
        this.add(mcdirLabel);

        mcdirField = new JTextField();
        mcdirField.setText(this.getMinecraftPath());
        mcdirField.setSize(200, 30);
        mcdirField.setLocation(60, 245);
        mcdirField.setCaretPosition(mcdirField.getText().length());
        this.add(mcdirField);

        nextButton = new JButton();
        nextButton.setText("Next");
        nextButton.setSize(100, 30);
        nextButton.setLocation(260, 494);
        nextButton.setFocusable(false);
        nextButton.addActionListener(this::checkErrors);
        this.add(nextButton);

        errorLabel = new JLabel();
        errorLabel.setForeground(Color.WHITE);
        errorLabel.setLocation(190 - errorLabel.getFontMetrics(errorLabel.getFont()).stringWidth(errorLabel.getText()) / 2, 435);
        errorLabel.setSize(300, 30);
        this.add(errorLabel);
    }

    // This string gets the .minecraft path
    private String getMinecraftPath() {
        switch (OS.getOS()) {
            case WINDOWS:
                return System.getProperty("user.home").replace("\\", "/") + "/AppData/Roaming/.minecraft";
            case MAC:
                return "/Users/" + System.getProperty("user.name") + "/Library/Application Support/minecraft";
            default:
                return "";
        }
    }

    // This function opens the file chooser
    private void openFileChooser(ActionEvent e) {
        dirSelect.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = dirSelect.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = dirSelect.getSelectedFile();
            mcdirField.setText(file.getAbsolutePath());
        } else {
            mcdirField.setText(this.getMinecraftPath());
        }
    }

    // This function checks if there are any java exceptions thrown or there are any problems with the selections.
    // If there is a java exception thrown, it calls showErrorDialog().
    private void checkErrors(ActionEvent e) {
        try {
            if (!checkMcDirError()) {
                this.displayPanel(new CreateProfilePanel(mcdirField.getText()));
            } else {
                errorLabel.setText("Please enter a valid Minecraft directory");
                errorLabel.setLocation(190 - errorLabel.getFontMetrics(errorLabel.getFont()).stringWidth(errorLabel.getText()) / 2, 435);
            }
        } catch (Exception ex) {
            errorLabel.setText(ex.getClass().getSimpleName());
            errorLabel.setLocation(190 - errorLabel.getFontMetrics(errorLabel.getFont()).stringWidth(errorLabel.getText()) / 2, 435);
            showErrorDialog(ex);
        }
    }

    // This function takes an exception as a parameter and opens a new window that displays the stack trace of the exception in a text area.
    private void showErrorDialog(Exception e) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        JDialog errorDialog = new JDialog();
        errorDialog.setTitle(e.getClass().getSimpleName());
        errorDialog.setSize(400, 400);
        errorDialog.setLocation((int) (screenSize.getWidth() / 2 - 400 / 2), (int) (screenSize.getHeight() / 2 - 400 / 2));

        JTextArea errorArea = new JTextArea();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        errorArea.setText(sw.toString());
        errorArea.setLocation(0, 0);
        errorArea.setSize(400, 400);
        errorArea.setEditable(false);

        JScrollPane errorScrollPane = new JScrollPane(errorArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        errorDialog.add(errorScrollPane);
        errorDialog.setVisible(true);
    }

    // This boolean checks if the path entered is a valid Minecraft directory
    // (all it does really is to check if the last 10 letters of the textfield text equals "minecraft").
    private boolean checkMcDirError() {
        if (mcdirField.getText().length() > 10 && mcdirField.getText().substring(mcdirField.getText().length() - 9).equals("minecraft") ||
                mcdirField.getText().length() > 11 && mcdirField.getText().substring(mcdirField.getText().length() - 10).equals("minecraft/") ||
                mcdirField.getText().length() < 11) {
            return false;
        } else return true;
    }

    // This function overrides the panels paintComponent method and sets the background image
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(this.bgImage, 0, 0, null);
    }

}
