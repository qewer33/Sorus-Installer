package org.sorus.installer.panels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import org.sorus.installer.launcherprofiles.Installation;
import org.sorus.installer.launcherprofiles.LauncherProfiles;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;
import java.util.Scanner;

public class CreateProfilePanel extends BasePanel {

    private static String[] versions = {"1.7.10", "1.8.9"};
    private static String[] mappings = {"","1.8.9-forge","1.7.10-forge"};

    private String minecraftPath;

    private JLabel minecraftInstallLabel;
    private JComboBox<String> minecraftInstallSelection;
    private JLabel clientVersionLabel;
    private JComboBox<String> clientVersionSelection;
    private JLabel mappingsLabel;
    private JComboBox<String> mappingsSelection;
    private JButton installButton;
    private JLabel doneLabel;
    private JLabel errorLabel;

    final JFileChooser dirSelect = new JFileChooser();

    // Constructor
    public CreateProfilePanel(String minecraftPath) {
        this.minecraftPath = minecraftPath;

        minecraftInstallLabel = new JLabel();
        minecraftInstallLabel.setForeground(Color.WHITE);
        minecraftInstallLabel.setText("Minecraft Installation");
        minecraftInstallLabel.setLocation(190 - minecraftInstallLabel.getFontMetrics(minecraftInstallLabel.getFont()).stringWidth("Minecraft Directory") / 2, 215);
        minecraftInstallLabel.setSize(new Dimension(205, 30));
        this.add(minecraftInstallLabel);

        minecraftInstallSelection = new JComboBox<>();
        File minecraft = new File(minecraftPath);
        File versionsFile = new File(minecraft, "versions");
        for(File file : Objects.requireNonNull(versionsFile.listFiles(File::isDirectory))) {
            boolean added = false;
            for(String string : versions) {
                if(file.getName().contains(string) && !added) {
                    added = true;
                    minecraftInstallSelection.addItem(file.getName());
                }
            }
        }
        minecraftInstallSelection.setSize(new Dimension(250, 30));
        minecraftInstallSelection.setLocation(65, 245);
        this.add(minecraftInstallSelection);

        clientVersionLabel = new JLabel();
        clientVersionLabel.setForeground(Color.WHITE);
        clientVersionLabel.setText("Client Version");
        clientVersionLabel.setLocation(190 - clientVersionLabel.getFontMetrics(clientVersionLabel.getFont()).stringWidth("Client Version") / 2, 275);
        clientVersionLabel.setSize(new Dimension(95, 30));
        this.add(clientVersionLabel);

        clientVersionSelection = new JComboBox<>();
        for(String version : versions) {
            clientVersionSelection.addItem(version);
        }
        clientVersionSelection.setSize(new Dimension(250, 30));
        clientVersionSelection.setLocation(65, 305);
        this.add(clientVersionSelection);

        mappingsLabel = new JLabel();
        mappingsLabel.setForeground(Color.WHITE);
        mappingsLabel.setText("Mappings");
        mappingsLabel.setLocation(190 - mappingsLabel.getFontMetrics(mappingsLabel.getFont()).stringWidth("Mappings") / 2, 335);
        mappingsLabel.setSize(new Dimension(95, 30));
        this.add(mappingsLabel);

        mappingsSelection = new JComboBox<>(mappings);
        mappingsSelection.setSize(new Dimension(250, 30));
        mappingsSelection.setLocation(65, 365);
        this.add(mappingsSelection);

        installButton = new JButton();
        installButton.setText("Install");
        installButton.setSize(new Dimension(100, 30));
        installButton.setLocation(260, 494);
        installButton.addActionListener(this::checkErrors);
        this.add(installButton);

        doneLabel = new JLabel();
        doneLabel.setForeground(Color.WHITE);
        doneLabel.setLocation(20, 494);
        doneLabel.setSize(300, 30);
        this.add(doneLabel);

        errorLabel = new JLabel();
        errorLabel.setForeground(Color.WHITE);
        errorLabel.setLocation(190 - errorLabel.getFontMetrics(errorLabel.getFont()).stringWidth(errorLabel.getText()) / 2, 435);
        errorLabel.setSize(300, 30);
        this.add(errorLabel);
    }

    // This function checks if there are any java exceptions thrown or there are any problems with the selections.
    // If there is a java exception thrown, it calls showErrorDialog().
    private void checkErrors(ActionEvent e) {
        try {
            if (checkVersionsMatching()) {
                if (checkForgeMappings()) {
                    install();
                } else {
                    errorLabel.setText("Mappings and MC version don't match");
                    errorLabel.setLocation(190 - errorLabel.getFontMetrics(errorLabel.getFont()).stringWidth(errorLabel.getText()) / 2, 435);
                }
            } else {
                errorLabel.setText("Sorus and MC versions don't match");
                errorLabel.setLocation(190 - errorLabel.getFontMetrics(errorLabel.getFont()).stringWidth(errorLabel.getText()) / 2, 435);
            }
        } catch(Exception ex) {
            showErrorDialog(ex);
        }
    }

    // This function installs the client.
    // If there are any java exceptions thrown in the process, it calls showErrorDialog().
    private void install() {
        File minecraft = new File(minecraftPath);
        File launcherProfile = new File(minecraft, "launcher_profiles.json");
        try {
            Scanner scanner = new Scanner(launcherProfile);
            StringBuilder stringBuilder = new StringBuilder();
            while(scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine());
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            LauncherProfiles launcherProfiles = gson.fromJson(stringBuilder.toString(), LauncherProfiles.class);
            Installation installation = new Installation();
            String memory = System.getProperty("sun.arch.data.model").equals("64") ? "2" : "1";
            String version = (String) clientVersionSelection.getSelectedItem();
            installation.created = "1970-01-01T00:00:00.000Z";
            installation.lastUsed = "1970-01-01T00:00:00.000Z";
            installation.lastVersionId = (String) minecraftInstallSelection.getSelectedItem();
            installation.name = "Sorus - " + clientVersionSelection.getSelectedItem();
            installation.type = "custom";
            // I was going to make it so installation has the sorus logo but I have no idea what I set it since
            // it is a string and not an image. Do I just set it as the path to the image?
            // installation.icon =
            installation.javaArgs = "-Xmx" + memory + "G -Xms" + memory + "G " + "-javaagent:Sorus/client/" + version + ".jar=version=" + version;
            launcherProfiles.profiles.put("Sorus", installation);
            FileWriter fileWriter = new FileWriter(launcherProfile);
            fileWriter.write(gson.toJson(launcherProfiles));
            fileWriter.close();
            new Thread(() -> {
                errorLabel.setText("");
                this.doneLabel.setText("Installing...");
                try {
                    this.downloadIfDifferent(new URL("https://github.com/SorusClient/Sorus-Resources/raw/master/client/" + version + ".jar"), new File(minecraftPath + "/sorus/client/" + version + ".jar"));
                } catch(IOException ex) {
                    errorLabel.setText(ex.getClass().getSimpleName());
                    showErrorDialog(ex);
                }
                this.doneLabel.setText("Done.");
                this.installButton.setEnabled(false);
            }).start();
        } catch(IOException ex) {
            errorLabel.setText(ex.getClass().getSimpleName());
            showErrorDialog(ex);
        }
    }

    private void downloadIfDifferent(URL url, File file) throws IOException {
        InputStream inputStream = url.openStream();
        boolean needToDownload;
        file.getParentFile().mkdirs();
        try {
            InputStream inputStream1 = new FileInputStream(file);
            needToDownload = !IOUtils.contentEquals(inputStream, inputStream1);
            inputStream1.close();
        } catch(FileNotFoundException e) {
            needToDownload = true;
        }
        if(needToDownload) {
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
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

    // This boolean checks if Sorus and Minecraft versions match
    private boolean checkVersionsMatching() {
        if (minecraftInstallSelection.getSelectedItem().toString().indexOf(clientVersionSelection.getSelectedItem().toString()) == -1) {
            return false;
        } else return true;
    }

    // This boolean checks if mappings are selected while installing the client on forge
    private boolean checkForgeMappings() {
        if (minecraftInstallSelection.getSelectedItem().toString().indexOf("forge") != -1 && mappingsSelection.getSelectedItem().toString().equals("") ||
                minecraftInstallSelection.getSelectedItem().toString().indexOf("forge") != -1 && minecraftInstallSelection.getSelectedItem().toString().indexOf("1.8.9") != -1 && mappingsSelection.getSelectedItem().toString().equals("1.7.10-forge") ||
                minecraftInstallSelection.getSelectedItem().toString().indexOf("forge") != -1 && minecraftInstallSelection.getSelectedItem().toString().indexOf("1.7.10") != -1 && mappingsSelection.getSelectedItem().toString().equals("1.8.9-forge") ||
                minecraftInstallSelection.getSelectedItem().toString().indexOf("forge") == -1 && ! mappingsSelection.getSelectedItem().toString().equals("")) {
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
