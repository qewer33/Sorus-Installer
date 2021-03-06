package org.sorus.installer.panels;

import com.cedarsoftware.util.io.JsonWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.sorus.installer.launcherprofiles.Installation;
import org.sorus.installer.launcherprofiles.LauncherProfiles;
import org.sorus.installer.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class LaunchWrapperInstallationPanel extends BasePanel {

    private static final String[] versions = {"1.7.10", "1.8.9"};
    private static final String[] mappings = {"", "1.7.10-forge", "1.8.9-forge"};

    private final String minecraftPath;

    private final JLabel minecraftInstallLabel;
    private final JComboBox<String> minecraftInstallSelection;
    private final JLabel clientVersionLabel;
    private final JComboBox<String> clientVersionSelection;
    private final JLabel mappingsLabel;
    private final JComboBox<String> mappingsSelection;
    private final JButton installButton;
    private final JLabel doneLabel;
    private final JLabel errorLabel;

    private JLabel loadingIcon;
    private ImageIcon loadingGif;
    private ImageIcon doneImage;

    public LaunchWrapperInstallationPanel(String minecraftPath) {
        this.minecraftPath = minecraftPath;

        minecraftInstallLabel = new JLabel();
        minecraftInstallLabel.setForeground(Color.WHITE);
        minecraftInstallLabel.setText("Minecraft Installation");
        minecraftInstallLabel.setLocation(
                190
                        - minecraftInstallLabel
                        .getFontMetrics(minecraftInstallLabel.getFont())
                        .stringWidth("Minecraft Directory")
                        / 2,
                215);
        minecraftInstallLabel.setSize(new Dimension(205, 30));
        this.add(minecraftInstallLabel);

        minecraftInstallSelection = new JComboBox<>();
        File minecraft = new File(minecraftPath);
        File versionsFile = new File(minecraft, "versions");
        for (File file : Objects.requireNonNull(versionsFile.listFiles(File::isDirectory))) {
            boolean added = false;
            for (String string : versions) {
                if (file.getName().contains(string) && !added) {
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
        clientVersionLabel.setLocation(
                190
                        - clientVersionLabel
                        .getFontMetrics(clientVersionLabel.getFont())
                        .stringWidth("Client Version")
                        / 2,
                275);
        clientVersionLabel.setSize(new Dimension(95, 30));
        this.add(clientVersionLabel);

        clientVersionSelection = new JComboBox<>();
        for (String version : versions) {
            clientVersionSelection.addItem(version);
        }
        clientVersionSelection.setSize(new Dimension(250, 30));
        clientVersionSelection.setLocation(65, 305);
        this.add(clientVersionSelection);

        mappingsLabel = new JLabel();
        mappingsLabel.setForeground(Color.WHITE);
        mappingsLabel.setText("Mappings");
        mappingsLabel.setLocation(
                190 - mappingsLabel.getFontMetrics(mappingsLabel.getFont()).stringWidth("Mappings") / 2,
                335);
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
        errorLabel.setLocation(
                190 - errorLabel.getFontMetrics(errorLabel.getFont()).stringWidth(errorLabel.getText()) / 2,
                435);
        errorLabel.setSize(300, 30);
        this.add(errorLabel);

        try {
            URL url = getClass().getResource("/loading.gif");
            loadingGif = new ImageIcon(url);
            doneImage = new ImageIcon(getClass().getResource("/checkmark.png"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // This function checks if there are any java exceptions thrown or there are any problems with the
    // selections.
    // If there is a java exception thrown, it calls showErrorDialog().
    private void checkErrors(ActionEvent e) {
        try {
            if (checkVersionsMatching()) {
                if (!checkForgeMappings()) {
                    install();
                } else {
                    setErrorLabelText("Mappings and MC version don't match");
                }
            } else {
                setErrorLabelText("Sorus and MC versions don't match");
                try {
                    if (checkVersionsMatching()) {
                        if (!checkForgeMappings()) {
                            install();
                        } else {
                            setErrorLabelText("Mappings and MC version don't match");
                        }
                    } else {
                        setErrorLabelText("Sorus and MC versions don't match");
                    }
                } catch (Exception ex) {
                    setErrorLabelText(ex.getClass().getSimpleName());
                    showErrorDialog(ex);
                }
            }
        } catch (Exception ex) {
            setErrorLabelText(ex.getClass().getSimpleName());
            showErrorDialog(ex);
        }
    }

    private void install() {
        showInstallingIcon();
        String selectedInstall = (String) minecraftInstallSelection.getSelectedItem();
        String version = (String) clientVersionSelection.getSelectedItem();
        String selectedMappings = (String) mappingsSelection.getSelectedItem();
        String mappings = selectedMappings.isEmpty() ? "" : ";mappings=" + selectedMappings;
        File inFolder = new File(new File(minecraftPath), "versions/" + selectedInstall);
        File in = new File(inFolder, selectedInstall + ".json");
        File outFolder = new File(new File(minecraftPath), "versions/Sorus-" + selectedInstall);
        outFolder.mkdir();
        File out = new File(outFolder, "Sorus-" + selectedInstall + ".json");
        try {
            String json = FileUtils.readFileToString(in, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);
            jsonObject.put("mainClass", "org.sorus.launchwrapper.SorusLaunch");
            String minecraftArgs = jsonObject.getString("minecraftArguments");
            jsonObject.put("minecraftArguments", minecraftArgs + " --sorusArgs version=" + version + mappings);
            String id = jsonObject.getString("id");
            jsonObject.put("id", "Sorus-" + id);
            JSONArray libraries = jsonObject.getJSONArray("libraries");
            libraries.put(this.getLibraryObject("org.sorus:Sorus:LaunchWrapper"));
            libraries.put(this.getLibraryObject("org.sorus:Sorus:Core"));
            libraries.put(this.getLibraryObject("org.sorus:Sorus:" + version));
            libraries.put(this.getLibraryObject("net.minecraft:launchwrapper:1.12"));
            FileOutputStream output = new FileOutputStream(out);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            output.write(jsonObject.toString().getBytes());
            output.flush();
            output.close();
            if(new File(inFolder, selectedInstall + ".jar").exists()) {
                Files.copy(Paths.get(new File(inFolder, selectedInstall + ".jar").toURI()), Paths.get(new File(outFolder, "Sorus-" + selectedInstall + ".jar").toURI()), StandardCopyOption.REPLACE_EXISTING);
            }

            File launcherProfile = new File(minecraftPath, "launcher_profiles.json");
            Scanner scanner = new Scanner(launcherProfile);
            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine());
            }
            LauncherProfiles launcherProfiles =
                    gson.fromJson(stringBuilder.toString(), LauncherProfiles.class);
            Installation installation = new Installation();
            String memory = System.getProperty("sun.arch.data.model").equals("64") ? "2" : "1";
            installation.created = "1970-01-01T00:00:00.000Z";
            installation.lastUsed = "1970-01-01T00:00:00.000Z";
            installation.lastVersionId = "Sorus-" + selectedInstall;
            installation.name = "Sorus - " + version;
            installation.type = "custom";
            installation.icon =
                    "data:image/png;base64,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4Qddcsklys3NtY4CmLC6mdVsaRjuc9hhh+mrr75Sdna2dRTXWLlypa644gp99tln1lGAuDr33HP12muvBf7Bpvz8fHXu3Flz5syxjgL8JOEdkZVSmAqFQnrkkUcopD9yHEfjxo1T69atKaTwvaZNm+r5558PfCGNRCIaMmQIhRQwYH3zLsNF47zzzrN+psA1cnNznUGDBjnhcNh8vzAY8R41atRwlixZYn3amYtGo87w4cPN9weDcYCRUBaX7xP+j4Q7paWl6auvvlLjxo2to5hbvXq1evTooc8//9w6ChB36enpmjBhgi688ELrKOamTp2qrl278oo3uFVCe2KiL99TSPGzW2+9lUIqaebMmWrXrh2FFIEQDod19913U0glLV26VH379qWQws0S2tsSvVJKKYUkqV69elq+fHmg7yV1HEejRo3S0KFDtXfvXus4QEJcddVVGjlypJKSkqyjmNq6das6dOigZcuWWUcBDiZhXTGRK6UUUvzsvvvuC3QhLS4u1h133KHBgwdTSBEYp556qh5++OHAF9KioiL169ePQgqv8GV/s75Zl+GS0bJlS6e4uNj6+QIzubm5zlVXXeWEQiHzfcFgJGocccQRztq1a61PP3PRaNS58cYbzfcHg1HGkRB80QkJFQqFdP/99ys5OZiH3p49e9S3b1+9+eab1lGAhKlatapeeOEFNWjQwDqKueeee06PPvqodQzAlRLVDBLWsuFunTp1UufOna1jmNi5c6d69uypd9991zoKkDApKSl65JFH1LZtW+so5j788EMNGTJEjsOUCM9xlIB7S4O5XAUTSUlJGj58uMLh4H2zYefOnerevbtmzJhhHQVImFAopNtvv119+/a1jmJu9erV6t27t/bt22cdBXCtRLQD/iSEJOmCCy7QCSecYB0j4SikCKpu3brpL3/5i0Ihqy9au0Nubq4uv/xyff/999ZRgIqIe59LxC8FpRRKSkrSZ599phNPPNE6SkLt3r1b3bt355I9AueEE07QjBkzVL16desopkpKStSrVy+98sor1lGAWIhrbwzedVSY6Nq1a+BWSX96qIlCiqCpV6+eJk6cGPhC6jiO7r33Xk2aNMk6CuAJ8V4pZZUUgVwlLSoq0rXXXqsxY8ZYRwESKisrS2+88YbOOOMM6yjmJk6cqL59+6q4uNg6ChBLceuOrJQi7s4666xArZJGo1Hdeeedeu6556yjAAmVnJys4cOHU0glzZs3T4MGDaKQAmUQz1LKKikUCoX0xz/+MTBP3DuOoyeeeEIPP/wwr31BoIRCIV133XW69tprraOY27hxo3r27Kk9e/ZYRwHiIW6TWzwv3zMjQ+3atdOHH34YmJflT5s2Td26dePToQiczp0766233lJaWpp1FFN5eXnq2rWrPvjgA+soQDzFpT8GY/kKZq677rrAFNLly5dr4MCBFFIETtOmTfXiiy8GvpAWFxfrpptuopAC5RSvUsoqKdSoUSNdfPHF1jESYteuXerfvz/vIUTg1KxZUxMmTFCtWrWso5gbMWIEDzciKOLS81gpRdxcffXVyszMtI4RdyUlJbr11ls1d+5c6yhAQqWlpWnUqFE67rjjrKOYmzJliu68805Fo1HrKIBnxeueUlZKAy47O1vLly9XvXr1rKPE3ZgxY3T11VcrEolYRwESJhQK6f7779dtt91mHcXckiVLdNZZZ2nTpk3WUYBEinmHjMdKKYUUuvjii1W3bl3rGHG3ZMkS3XLLLRRSBE6fPn108803W8cwt3nzZvXt25dCiiCKed/j8j1iLhwO69prr/X9967z8/M1aNAg7dixwzoKkFDt2rXT448/rqSkJOsopgoKCjRw4EAtWrTIOgrgC7EupaySQi1btlSrVq2sY8SV4zh64IEH9Nlnn1lHARKqQYMGeumll5SdnW0dxZTjOLrjjjv09ttvW0cBLMW097FSipjr06ePUlNTrWPE1bx58zR8+HDrGEBCZWZmasKECapfv751FHOjR4/WiBEj+EgGEEOxvr7K2RlwlStX1tdff61DDjnEOkrcFBQU6NRTT9UXX3xhHQVImHA4rHHjxqlXr17WUcx9+OGH6tq1q3Jzc62jAG4Qsy4Zy5VSCil0xhln+P4Bp3//+98UUgTOsGHD1LNnT+sY5lavXq1+/fpRSIH/F7P+F8uVUkopNHnyZF1yySXWMeJmxYoVOumkk7R7927rKEDCXHzxxZowYYLvb8s5mF27dunss8/WvHnzrKMAbhOTPsk9pYiZOnXq6PTTT7eOETfRaFR/+tOfKKQIlOOPP17PPvts4AtpUVGRBg8eTCEF4ihWpZRVUuj8889X1apVrWPEzdtvv6033njDOgaQMLVq1dIrr7zi6/O6NH5628aECROsowBuFZMeyEopYqZHjx7WEeKmsLBQf/7zn/mEIAIjIyNDL7zwgho3bmwdxdzEiRN17733cv4DcUYpRUw0aNBAbdq0sY4RN2PGjNFXX31lHQNIiHA4rIceekhnnnmmdRRzX3zxhQYPHqyioiLrKIDvxaKUcuke6ty5s29fpp2bm6t77rnHOgaQMNdff72uvvpq6xjmNmzYoN69e/PVNqB0KtwHWSlFTFxwwQXWEeLm6aef5rvWCIwzzzxTw4cPD/wnRHNzc3XFFVfom2++sY4CBEYsHuFnpTTgcnJytGzZMtWoUcM6Sszl5eWpSZMmlFIEwlFHHaUPPvjA9+8aPphIJKIbbrhBTz75pHUUwGsq1CsrulJKIYXatWvny0IqSU899RSFFIFQvXp1vfTSS4EvpJI0YsQIPf3009YxAC+qUC/k8j0q7Nxzz7WOEBd79+7Vgw8+aB0DiLvU1FQ988wzatmypXUUc2+//TZv2gCMUEpRIWlpaercubN1jLh49tlntXnzZusYQNzdc889uvDCC61jmFu2bJkGDBig/Px86yhAIFXk2j+X7qHjjz9e8+bNU0pKinWUmCopKVGzZs307bffWkcB4uqKK67Q6NGjfXcOl9WOHTt02mmnaenSpdZRAD8oV79kpRQV0qFDB19OZm+99RaFFL7Xtm1bPfbYY748h8uiqKhI/fv3p5ACxiilqJAzzjjDOkJcjBgxwjoCEFeHHnqoJkyY4Nv3C5dWNBrVnXfeqSlTplhHAQKPy/cot+zsbC1fvlz16tWzjhJTy5cvV/PmzeU4HOLwp0qVKmn69Ok6+eSTraOYGzNmjAYOHMiDTUBsJfTyPbM11Lx5c9WuXds6RsyNGjWKQgrfSk5O1mOPPUYhlTRnzhwNGTKEQgrEXrkmUS7fo9xOPvlk3331pbCwUC+99JJ1DCAuQqGQhg4dqiuuuMI6irm1a9fq8ssv1759+6yjAPgRpRTl1r59e+sIMffuu+/ysnz41kUXXaS///3vCoVi8TE/79qzZ4969uypjRs3WkcBsJ/ylFKua0JpaWm+fNH2uHHjuHQPXzr++OM1atQopaamWkcxVVxcrCFDhuizzz6zjgL4XZknU1ZKUS4NGzb03QNOO3fu1DvvvGMdA4i5unXr6sUXX/Tt54BLy3EcPfDAAxo3bpx1FAC/gVKKcjn66KOVnp5uHSOmpk6dqry8POsYQExVqlRJY8aMUfPmza2jmHvttdf0t7/9zToGgAOglKJcWrVqZR0h5t544w3rCEBMJSUl6R//+IfOPvts6yjmvvzyS1199dUqLi62jgLgAMpaSrnZDpKkE044wTpCTO3du1fvvfeedQwgZkKhkK699loNHjzYOoq5jRs3qmfPntqxY4d1FCBoytQbWSlFmWVmZqpZs2bWMWJq1qxZ2rVrl3UMIGbOOussPfDAA757bVtZFRQUqF+/flq5cqV1FAAHQSlFmdWpU0c1a9a0jhFT7777Lk/dwzeaNm2q0aNHKysryzqKqWg0qj/+8Y+aOXOmdRQApZBchv8sMzYkSUcccYQyMzOtY8SM4ziaPn26dYxAycjIUEZGhtLS0jz3zsxIJOLqP2AqVaqkF198UYceeqh1FHOPPfaYnn76aesYQNA5KuVnR8tSSgFJUosWLawjxNS6dev07bffWsfwrVAopLp166p169Y69dRTdcwxx6hhw4aqVq2aKlWqpHDYWxdsioqKXF1Kk5KSAr9CKknTp0/XsGHDXL2vAPwSpRRl1rRpU+sIMfXpp5+qqKjIOobvVKlSRV26dFGfPn3Url07Va5c2TpSTKSlpVlHwEGsWLFCvXv3Vn5+vnUUAGVAKUWZHXXUUdYRYmrWrFnWEXylRo0auvrqq3XdddfpsMMOs46DgNm9e7cuu+wybdu2zToKgDIqbSnl+gck/XeVyG9FY/bs2dYRfCE1NVW9evXS3/72N9WvX986DgKoqKhI/fr101dffWUdBcAvleq+UlZKUSY1atRQ9erVrWPEzLZt27ifNAYOP/xwjRgxQueee67nHlyCP0SjUd111118BAPwMEopyiQnJ8c39wZK0tKlS1VQUGAdw9POOeccPfvss6pbt651FATYCy+8oAcffNA6BoAK8NZjrzB36KGHeu5p6d+zaNEiRaNR6xieFA6Hdc0112jy5MkUUphat26dhg0bpkgkYh0FQAWUpl1wPyl+1qBBA+sIMbVw4ULrCJ4UCoV08803a8SIEb56Zy28qX79+vr000/19NNP66yzzvLV1RzARw7aJ/2z5IWE8NtDTjwQUT79+vXTvffeq5SUFOsogMLhsBo0aKBBgwZp2rRpWrJkiZ588kmdfPLJvMIL8JDSPJHASil+NnbsWPXt29c6RkwUFxerbt262r59u3UU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t44SVzVr1lT16tX19ttv89lCF0hPT9fzzz+vTp06WUcxN3XqVF133XUqKiqyjuJphYWFmjJlio455hgdddRR1nESKhQKqXnz5ho1ahRvbEBCmTdjhv/GkUce6ezbt8/6j/24Ky4udnr27Gm+vYM+QqGQc9999wVyRet/LVq0yKlbt675PvHTqFKlijN16lTrXZtw0WjUueKKK8y3PyNQwzwAw6fjzTfftP5NTYgtW7Y4TZs2Nd/eQR79+/d3ioqKrA8Fc5s2beJ1PnEatWrVcmbPnm29ixPu66+/dtLS0sy3PyMYIywgTp544olAXPbJycnR888/r8qVK1tHCaTTTjtNDz/8cOCftC8oKNC1116rxYsXW0fxpS1btqhbt25asGCBdZSEatq0qbp3724dAwFi3owZ/hxJSUnO4sWLrf/QT5jRo0c7ycnJ5ts9SKNRo0Z8QtRxnEgk4txyyy28WzIB46ijjnJWr15tvcsTaunSpfy2MRIyeNAJceM4jgoKCtS1a1eFQiHrOHF33HHHac+ePfrss8+sowRClSpVNHnyZB177LHWUcw988wz+stf/qJoNGodxfe2b9+uefPm6eKLL1ZGRoZ1nISoUaOG5s+frxUrVlhHQQCYN2OGf0d2drazfv166z/0E2bv3r1Oly5dzLe730dycrIzbtw4693tCh999JGTnZ1tvk+CNi677DInPz/fevcnzCeffMIrxhhxH6yUIq6KiooUDod11llnWUdJiJSUFJ155pl67733tGnTJus4vhQKhXT77bfrD3/4g8LhYN8Wv2rVKnXt2lXbtm2zjhI4y5YtkyR17NgxEFeC6tWrp5kzZ+q7776zjgKfM2/GDH+PatWqOVu2bLH+Qz+hvv32W6dRo0bm296P49JLLw3UCtWB7Ny50znppJPM90eQR0pKivPyyy9bHwoJM3nyZPNtzvD3CPYyAxJi586deuaZZ6xjJNQRRxyhCRMmqF69etZRfKVly5Z66qmnlJ6ebh3FVFFRkYYMGaK5c+daRwm04uJiXX/99YF548E555yjJk2aWMeAj1FKkRCPPPKIdu7caR0jodq0aaMJEyaoevXq1lF8oV69enrxxRdVs2ZN6yimotGo7rvvPo0fP946CiRt27ZNAwYM0O7du62jxF1mZqYGDBhgHQM+xj2lSIi8vDxVrVrV958e/V8NGjRQ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l+HpIbkgBIPxmyM9Pd157733rH9rPW3VqlXOPffc4zRq1CjhE0Z2drazYMEC603gCo8++qiTlJRkfk4x3DGGDRtmfUjGXG5urtOwYUPzbcvw7NBP19IcAS5Vv359ffLJJzrssMOso3haYWGh3nvvPY0dO1YzZszQzp074/r/Lzk5WRMnTtQll1xi/U83N336dF100UXKz8+3jgKXOPTQQ7VmzRolJydbR4mpiy++2Jf3zCIhQtxTCtf77rvvNHDgQCb0CkpLS1OXLl00ceJEffPNNxozZoy6dOmiatWqxfxez1AopLvuuotCKmnFihXq168fxy9+YePGjfrwww+tY8TcySefbB0BHvbTkxB3WwcBfs+aNWuUn5+vM888M/APy8RCZmamjj/+ePXq1UsDBgzQiSeeqEqVKmnnzp3at2+fotFohf77e/Toof/85z8Kh4P9d+/27dt1wQUXaNWqVdZR4EIpKSm68MILrWPElOM4vn0XK+LuHi7fwzOSk5M1atQo9e/f3zqKbxUWFmrx4sWaNWuWPvzwQ33xxRfavn17mT5r2qZNG73//vvKysqy/ueYKiwsVK9evfTqq69aR4FL1ahRQ+vXr/fV53Y3bdqkpk2bavfu3dZR4D0hSik8pXLlypoyZYpOO+006yiBkJ+fr2+++UYLFizQvHnztHDhQq1atUp79uxRQUHBr/7z9erV02effab69etbRzcVjUZ155136v777+eLTTigUCikqVOn6pxzzrGOEjMlJSVq06aNFixYYB0F3kMphfc0bNhQM2bMUOPGja2jBFJubq42bNig1atXa8WKFVq1apXWrl2rjRs3asSIETr11FOtI5p77rnndPXVV6ukpMQ6Clzu2muv1ZNPPmkdI6b69++v559/3joGvIdSCm866aST9Pbbb6tGjRrWUYBf2LVrl/7yl79oz5491lF+IT8//6D3CpeUlGjHjh3atGmTduzYoV27dikSiVhH97VDDjlEa9eu9dVT+I8++qhuvPFG6xjwHkopvOvCCy/U+PHjfXU/FuAG0WhU27dv15o1azR37ly98847mjt3rrZv324dzXeSkpI0d+5ctWrVyjpKzHz00Ufq2LGjdQx4D6+Egne9+eabuummm/gUKRBj4XBYOTk5atOmjW644Qa9/fbbWrp0qcaMGaO2bdv6alXPWiQS0YwZM6xj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";
            installation.javaArgs =
                    "-Xmx"
                            + memory
                            + "G -Xms"
                            + memory
                            + "G";
            launcherProfiles.profiles.put(Util.generateRandomString(8), installation);
            FileWriter fileWriter = new FileWriter(launcherProfile);
            fileWriter.write(gson.toJson(launcherProfiles));
            fileWriter.close();
            new Thread(
                    () -> {
                        setErrorLabelText("");
                        this.doneLabel.setText("Installing...");
                        try {
                            this.downloadIfDifferent(
                                    new URL(
                                            "https://github.com/SorusClient/Sorus-Resources/raw/master/client/environments/LaunchWrapper.jar"),
                                    new File(minecraftPath, "libraries/org/sorus/Sorus/LaunchWrapper/Sorus-LaunchWrapper.jar"));
                            this.downloadIfDifferent(
                                    new URL(
                                            "https://github.com/SorusClient/Sorus-Resources/raw/master/client/versions/"
                                                    + version
                                                    + ".jar"),
                                    new File(minecraftPath, "libraries/org/sorus/Sorus/" + version + "/Sorus-" + version + ".jar"));
                            this.downloadIfDifferent(
                                    new URL(
                                            "https://github.com/SorusClient/Sorus-Resources/raw/master/client/Core.jar"),
                                    new File(minecraftPath, "libraries/org/sorus/Sorus/Core/Sorus-Core.jar"));
                        } catch (IOException ex) {
                            setErrorLabelText(ex.getClass().getSimpleName());
                            showErrorDialog(ex);
                        }
                        if (!((String) mappingsSelection.getSelectedItem()).isEmpty()) {
                            try {
                                this.downloadIfDifferent(
                                        new URL(
                                                "https://github.com/SorusClient/Sorus-Resources/raw/master/mappings/"
                                                        + mappingsSelection.getSelectedItem()
                                                        + ".txt"),
                                        new File(
                                                minecraftPath
                                                        + "/sorus/mappings/"
                                                        + mappingsSelection.getSelectedItem()
                                                        + ".txt"));
                            } catch (IOException ex) {
                                setErrorLabelText(ex.getClass().getSimpleName());
                                showErrorDialog(ex);
                            }
                        }
                        this.loadingIcon.setIcon(doneImage);
                        this.doneLabel.setText("Done.");
                        this.installButton.setEnabled(false);
                    })
                    .start();
        } catch(IOException | JSONException ioException) {
            ioException.printStackTrace();
        }
    }

    // This function installs the client.
    // If there are any java exceptions thrown in the process, it calls showErrorDialog().
    private void installOld() {
        File minecraft = new File(minecraftPath);
        File launcherProfile = new File(minecraft, "launcher_profiles.json");
        try {

            showInstallingIcon();

            Scanner scanner = new Scanner(launcherProfile);
            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine());
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            LauncherProfiles launcherProfiles =
                    gson.fromJson(stringBuilder.toString(), LauncherProfiles.class);
            Installation installation = new Installation();
            String memory = System.getProperty("sun.arch.data.model").equals("64") ? "2" : "1";
            String version = (String) clientVersionSelection.getSelectedItem();
            installation.created = "1970-01-01T00:00:00.000Z";
            installation.lastUsed = "1970-01-01T00:00:00.000Z";
            installation.lastVersionId = (String) minecraftInstallSelection.getSelectedItem();
            installation.name = "Sorus - " + clientVersionSelection.getSelectedItem();
            installation.type = "custom";
            installation.icon =
                    "data:image/png;base64,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4Qddcsklys3NtY4CmLC6mdVsaRjuc9hhh+mrr75Sdna2dRTXWLlypa644gp99tln1lGAuDr33HP12muvBf7Bpvz8fHXu3Flz5syxjgL8JOEdkZVSmAqFQnrkkUcopD9yHEfjxo1T69atKaTwvaZNm+r5558PfCGNRCIaMmQIhRQwYH3zLsNF47zzzrN+psA1cnNznUGDBjnhcNh8vzAY8R41atRwlixZYn3amYtGo87w4cPN9weDcYCRUBaX7xP+j4Q7paWl6auvvlLjxo2to5hbvXq1evTooc8//9w6ChB36enpmjBhgi688ELrKOamTp2qrl278oo3uFVCe2KiL99TSPGzW2+9lUIqaebMmWrXrh2FFIEQDod19913U0glLV26VH379qWQws0S2tsSvVJKKYUkqV69elq+fHmg7yV1HEejRo3S0KFDtXfvXus4QEJcddVVGjlypJKSkqyjmNq6das6dOigZcuWWUcBDiZhXTGRK6UUUvzsvvvuC3QhLS4u1h133KHBgwdTSBEYp556qh5++OHAF9KioiL169ePQgqv8GV/s75Zl+GS0bJlS6e4uNj6+QIzubm5zlVXXeWEQiHzfcFgJGocccQRztq1a61PP3PRaNS58cYbzfcHg1HGkRB80QkJFQqFdP/99ys5OZiH3p49e9S3b1+9+eab1lGAhKlatapeeOEFNWjQwDqKueeee06PPvqodQzAlRLVDBLWsuFunTp1UufOna1jmNi5c6d69uypd9991zoKkDApKSl65JFH1LZtW+so5j788EMNGTJEjsOUCM9xlIB7S4O5XAUTSUlJGj58uMLh4H2zYefOnerevbtmzJhhHQVImFAopNtvv119+/a1jmJu9erV6t27t/bt22cdBXCtRLQD/iSEJOmCCy7QCSecYB0j4SikCKpu3brpL3/5i0Ihqy9au0Nubq4uv/xyff/999ZRgIqIe59LxC8FpRRKSkrSZ599phNPPNE6SkLt3r1b3bt355I9AueEE07QjBkzVL16desopkpKStSrVy+98sor1lGAWIhrbwzedVSY6Nq1a+BWSX96qIlCiqCpV6+eJk6cGPhC6jiO7r33Xk2aNMk6CuAJ8V4pZZUUgVwlLSoq0rXXXqsxY8ZYRwESKisrS2+88YbOOOMM6yjmJk6cqL59+6q4uNg6ChBLceuOrJQi7s4666xArZJGo1Hdeeedeu6556yjAAmVnJys4cOHU0glzZs3T4MGDaKQAmUQz1LKKikUCoX0xz/+MTBP3DuOoyeeeEIPP/wwr31BoIRCIV133XW69tprraOY27hxo3r27Kk9e/ZYRwHiIW6TWzwv3zMjQ+3atdOHH34YmJflT5s2Td26dePToQiczp0766233lJaWpp1FFN5eXnq2rWrPvjgA+soQDzFpT8GY/kKZq677rrAFNLly5dr4MCBFFIETtOmTfXiiy8GvpAWFxfrpptuopAC5RSvUsoqKdSoUSNdfPHF1jESYteuXerfvz/vIUTg1KxZUxMmTFCtWrWso5gbMWIEDzciKOLS81gpRdxcffXVyszMtI4RdyUlJbr11ls1d+5c6yhAQqWlpWnUqFE67rjjrKOYmzJliu68805Fo1HrKIBnxeueUlZKAy47O1vLly9XvXr1rKPE3ZgxY3T11VcrEolYRwESJhQK6f7779dtt91mHcXckiVLdNZZZ2nTpk3WUYBEinmHjMdKKYUUuvjii1W3bl3rGHG3ZMkS3XLLLRRSBE6fPn108803W8cwt3nzZvXt25dCiiCKed/j8j1iLhwO69prr/X9967z8/M1aNAg7dixwzoKkFDt2rXT448/rqSkJOsopgoKCjRw4EAtWrTIOgrgC7EupaySQi1btlSrVq2sY8SV4zh64IEH9Nlnn1lHARKqQYMGeumll5SdnW0dxZTjOLrjjjv09ttvW0cBLMW097FSipjr06ePUlNTrWPE1bx58zR8+HDrGEBCZWZmasKECapfv751FHOjR4/WiBEj+EgGEEOxvr7K2RlwlStX1tdff61DDjnEOkrcFBQU6NRTT9UXX3xhHQVImHA4rHHjxqlXr17WUcx9+OGH6tq1q3Jzc62jAG4Qsy4Zy5VSCil0xhln+P4Bp3//+98UUgTOsGHD1LNnT+sY5lavXq1+/fpRSIH/F7P+F8uVUkopNHnyZF1yySXWMeJmxYoVOumkk7R7927rKEDCXHzxxZowYYLvb8s5mF27dunss8/WvHnzrKMAbhOTPsk9pYiZOnXq6PTTT7eOETfRaFR/+tOfKKQIlOOPP17PPvts4AtpUVGRBg8eTCEF4ihWpZRVUuj8889X1apVrWPEzdtvv6033njDOgaQMLVq1dIrr7zi6/O6NH5628aECROsowBuFZMeyEopYqZHjx7WEeKmsLBQf/7zn/mEIAIjIyNDL7zwgho3bmwdxdzEiRN17733cv4DcUYpRUw0aNBAbdq0sY4RN2PGjNFXX31lHQNIiHA4rIceekhnnnmmdRRzX3zxhQYPHqyioiLrKIDvxaKUcuke6ty5s29fpp2bm6t77rnHOgaQMNdff72uvvpq6xjmNmzYoN69e/PVNqB0KtwHWSlFTFxwwQXWEeLm6aef5rvWCIwzzzxTw4cPD/wnRHNzc3XFFVfom2++sY4CBEYsHuFnpTTgcnJytGzZMtWoUcM6Sszl5eWpSZMmlFIEwlFHHaUPPvjA9+8aPphIJKIbbrhBTz75pHUUwGsq1CsrulJKIYXatWvny0IqSU899RSFFIFQvXp1vfTSS4EvpJI0YsQIPf3009YxAC+qUC/k8j0q7Nxzz7WOEBd79+7Vgw8+aB0DiLvU1FQ988wzatmypXUUc2+//TZv2gCMUEpRIWlpaercubN1jLh49tlntXnzZusYQNzdc889uvDCC61jmFu2bJkGDBig/Px86yhAIFXk2j+X7qHjjz9e8+bNU0pKinWUmCopKVGzZs307bffWkcB4uqKK67Q6NGjfXcOl9WOHTt02mmnaenSpdZRAD8oV79kpRQV0qFDB19OZm+99RaFFL7Xtm1bPfbYY748h8uiqKhI/fv3p5ACxiilqJAzzjjDOkJcjBgxwjoCEFeHHnqoJkyY4Nv3C5dWNBrVnXfeqSlTplhHAQKPy/cot+zsbC1fvlz16tWzjhJTy5cvV/PmzeU4HOLwp0qVKmn69Ok6+eSTraOYGzNmjAYOHMiDTUBsJfTyPbM11Lx5c9WuXds6RsyNGjWKQgrfSk5O1mOPPUYhlTRnzhwNGTKEQgrEXrkmUS7fo9xOPvlk3331pbCwUC+99JJ1DCAuQqGQhg4dqiuuuMI6irm1a9fq8ssv1759+6yjAPgRpRTl1r59e+sIMffuu+/ysnz41kUXXaS///3vCoVi8TE/79qzZ4969uypjRs3WkcBsJ/ylFKua0JpaWm+fNH2uHHjuHQPXzr++OM1atQopaamWkcxVVxcrCFDhuizzz6zjgL4XZknU1ZKUS4NGzb03QNOO3fu1DvvvGMdA4i5unXr6sUXX/Tt54BLy3EcPfDAAxo3bpx1FAC/gVKKcjn66KOVnp5uHSOmpk6dqry8POsYQExVqlRJY8aMUfPmza2jmHvttdf0t7/9zToGgAOglKJcWrVqZR0h5t544w3rCEBMJSUl6R//+IfOPvts6yjmvvzyS1199dUqLi62jgLgAMpaSrnZDpKkE044wTpCTO3du1fvvfeedQwgZkKhkK699loNHjzYOoq5jRs3qmfPntqxY4d1FCBoytQbWSlFmWVmZqpZs2bWMWJq1qxZ2rVrl3UMIGbOOussPfDAA757bVtZFRQUqF+/flq5cqV1FAAHQSlFmdWpU0c1a9a0jhFT7777Lk/dwzeaNm2q0aNHKysryzqKqWg0qj/+8Y+aOXOmdRQApZBchv8sMzYkSUcccYQyMzOtY8SM4ziaPn26dYxAycjIUEZGhtLS0jz3zsxIJOLqP2AqVaqkF198UYceeqh1FHOPPfaYnn76aesYQNA5KuVnR8tSSgFJUosWLawjxNS6dev07bffWsfwrVAopLp166p169Y69dRTdcwxx6hhw4aqVq2aKlWqpHDYWxdsioqKXF1Kk5KSAr9CKknTp0/XsGHDXL2vAPwSpRRl1rRpU+sIMfXpp5+qqKjIOobvVKlSRV26dFGfPn3Url07Va5c2TpSTKSlpVlHwEGsWLFCvXv3Vn5+vnUUAGVAKUWZHXXUUdYRYmrWrFnWEXylRo0auvrqq3XdddfpsMMOs46DgNm9e7cuu+wybdu2zToKgDIqbSnl+gck/XeVyG9FY/bs2dYRfCE1NVW9evXS3/72N9WvX986DgKoqKhI/fr101dffWUdBcAvleq+UlZKUSY1atRQ9erVrWPEzLZt27ifNAYOP/xwjRgxQueee67nHlyCP0SjUd111118BAPwMEopyiQnJ8c39wZK0tKlS1VQUGAdw9POOeccPfvss6pbt651FATYCy+8oAcffNA6BoAK8NZjrzB36KGHeu5p6d+zaNEiRaNR6xieFA6Hdc0112jy5MkUUphat26dhg0bpkgkYh0FQAWUpl1wPyl+1qBBA+sIMbVw4ULrCJ4UCoV08803a8SIEb56Zy28qX79+vr000/19NNP66yzzvLV1RzARw7aJ/2z5IWE8NtDTjwQUT79+vXTvffeq5SUFOsogMLhsBo0aKBBgwZp2rRpWrJkiZ588kmdfPLJvMIL8JDSPJHASil+NnbsWPXt29c6RkwUFxerbt262r59u3UUT+nUqZPefPNNVapUyToK8LsikYiWL1+uF198UZMmTdLKlSutIwFB97u9k5VSlImf7h1cv3698vLyrGN4Sr169TR69GgKKTwhKSlJLVq00D//+U/Nnz9fb775pi644AK+eAW4FKUUpRYKhVSrVi3rGDGzYcMGFRcXW8fwjHA4rIceekiHH364dRSgzCpVqqQLLrhAr7/+uhYuXKhhw4b57nYkwOsOVkq5dI+fpaWlKTs72zpGzKxdu5Yn78vgvPPO0yWXXGIdA6iQcDisxo0b67777tPChQv17LPPqlWrVr56qwjgYr/bKzkLUWqZmZnKyMiwjhEz69ats47gGRkZGfr73//Og03wlWrVqunKK6/U7NmzNWXKFJ155pkc44AhSilKLSMjw1eldOPGjdYRPOOiiy7Sscceax0DiIu0tDSdd955mjp1qj766CNdeOGFSk7m2zJAolFKUWrp6em+er3KDz/8YB3BE5KTk3XjjTdyeRO+l5ycrJNPPlmTJ0/WrFmz1LVrV8opkEC/N8twPyl+IT09Xenp6dYxYmbr1q3WETyhTZs2OuGEE6xjAAmTlJSktm3b6tVXX6WcArF3wH7J0gdKzU8POUnStm3brCN4Qrdu3bjPDoG0fzmdOXOm2rdvr1CoNK/3BlAelFKUmt8+J7lr1y7rCK6XnJys8847zzoGYCopKUmnnXaa3n//fb388stq3ry5dSTAlyilKDU/PeS0b98+3lFaCkceeSTvcgR+lJKSossuu0zz5s3Tv/71L9WoUcM6EuArlFKUmp/uJy0oKOAdpaXQsmVLX+13IBaysrJ0yy23aMmSJRowYAD3mwIxQilFqfnp6evCwkJKaSkcd9xx1hEA16pbt65GjRqljz76SK1atbKOA3jegVoGT97jV/x0+b6oqIhSWgoNGza0jgC4WigU0imnnKI5c+boX//6l6pUqWIdCfCC3+yZ/ln6Qtz5aaU0EolYR/CEQw45xDoC4Ampqam65ZZbNG/ePJ199tk8pQ+Ug39aBoCYq1y5snUEwFOOPPJIvf322xo5cqSqVq1qHQfwFEopgAPy0+o4kChJSUkaOHCgFi5cqM6dO1vHATyDGQellp+fbx0hZjIyMihcAOKqQYMGeuedd/TYY4/56p58IF5+a1bmISf8Jj89GJSUlGQdwRP4wABQMUlJSRo8eLA+//xz3mYB/NKv+iZLRSi1goIC6wgxk5mZSTEthS1btlhHAHyhRYsWmjNnjoYMGcJVGuAAODNQakVFRdYRYiY9PZ2JoRTWrFljHQHwjczMTI0YMULjx4/na1DAb2BWRqnl5eVZR4iZ5ORkVapUyTqG6y1ZssQ6AuA73bt31+zZs9WyZUvrKICrUEpRanv37rWOEFPVq1e3juB6X375pa9WyAG3OOqoo/TRRx+pb9++XLUBfsSZgFLLz89XSUmJdYyYycnJsY7geitWrNDGjRutYwC+lJ2dreeff14PPvig0tPTreMA5v63lPLkPQ4oPz/fVw871alTxzqC6xUWFmrmzJnWMQDfCoVC+uMf/6jXX3+dP5QRRL/onayUotQKCgp8VUrr1atnHcETJk6cyGdZgTg7++yz9fHHH6t58+bWUQAzlFKUWn5+vq9eoF+/fn3rCJ7wySef6Ouvv7aOAfhe06ZN9cEHH+iMM86wjgKYoJSi1Pbu3eurh50aNmxoHcETCgoKNGrUKDkOd/cA8VarVi29/vrruuKKKxQKhazjAAlFKUWpRaNRbd261TpGzDRo0EApKSnWMTxh7NixWr16tXUMIBAqVaqkkSNHatiwYXzkA4FCKUWZbN682TpCzNSvX19paWnWMTxh9+7duuuuu7i3FEiQtLQ0/f3vf9ff//53paamWscBEoJSijL54YcfrCPETLVq1VS3bl3rGJ7x8ssv8yQ+kEBJSUkaNmyYRo4cqYyMDOs4QNztX0q5YQwH9d1331lHiKkWLVpYR/CMkpISDRkyxFer5YDbhUIhXXHFFRo5cqQyMzOt4wDx8HP/ZKUUZeK3UnrMMcdYR/CUlStXavDgwSosLLSOAgRGKBRS79699dxzz/F5ZPgapRRlsm7dOusIMXXcccdZR/CcV199VXfffTf3lwIJFAqF1K1bN40ZM4ZiCt+ilKJMNm3apH379lnHiJljjjmGhwjKyHEcDR8+XMOHD6eYAgl22WWXUUzhW5RSlMnOnTu1e/du6xgx07hxYz7tVw7RaFR33nmn7r77bpWUlFjHAQLlp2KalZVlHQWIKUopymT37t2+egI/HA6rdevW1jE8KRqN6p///KeuueYa7dmzxzoOECiXXXaZnnrqKR5+gq9QSlEmjuPo22+/tY4RU+3bt7eO4FnRaFTPPvuszjrrLC1cuNA6DhAovXv31hNPPMH7luEblFKU2bJly6wjxFT79u35akoFzZ07V6eeeqruueceVk2BBPnpdVF/+9vflJycbB0HqLCfSinvKEWpLV261DpCTLVs2VLVqlWzjuF5eXl5uvvuu3XiiSdq5MiRlFMgAUKhkG6++WbdfvvtCoVC1nGA8nIk6aflobut08A7UlJSNHDgQIXD/lhoT0pK0ueff66vv/7aOoov7NixQ2+99ZZeeuklbdu2TdWrV1fNmjV9c7wAbhMOh9W+fXt999133EYDL7vnpz+rWClFqdWsWVNff/21r55aHz16tK6++mrrGL6Umpqqpk2bqlOnTmrfvr2aNWumOnXqqFq1ahRVIIb27t2rSy65RNOnT7eOApRHiFKKMguFQvr888/VqlUr6ygxs379ejVp0kRFRUXWUXwvNTVVlStXVlZWlqpWraqUlJRS/d9lZGSYXZ5MTU3V8OHD1bJlS5P//24yb948ffXVV6pdu/bPf1zk5OQoOzvbOhokbd68WWeeeaaWLFliHQUoK0opymfkyJG+Wll0HEetW7fWl19+aR0FLhMOh39+J2vQ79lbuXKlOnTo8PNr4ZKSkpSenq6srCzl5OSocePGatKkiZo3b64WLVqoUaNGvrqi4hXLly/XGWecoe+//946ClAWIR7XQ7nMnz/fOkJMhUIhXXrppZRS/Mpll12mP//5z4EvpHv27FHPnj1/8Z7iSCSivXv3au/evdqyZcsvHoJMTk5Wdna2GjRooDZt2vw8jjzySF5hFGdNmzbVc889p4suushXX+CD/7FSinI58cQT9dlnn/nqVUrLly/X0Ucfzacz8bPWrVtr2rRpqlGjhnUUUyUlJerRo4cmT55cof+e1NRU1a5dW6eccopOP/10dejQQU2aNOHe4jhwHEePPfaY/vjHP/KbBq/g8j3Kp3r16lq2bJlq1aplHSWmTjjhBC1YsMA6Blygbt26+vjjj9W4cWPrKObuuusu3XvvvXKc2E4VGRkZaty4sbp06aIuXbqoTZs2Sk1Ntf7n+kYkEtF1112nUaNGWUcBSuPny1EOg1HW8fHHHzt+8+CDD5pvV4b9yMjIcN577z3rw9EVxo8f76SkpMR9mycnJztHHHGEM3ToUOeTTz5xSkpKrP/pvrB7926nbdu25ucUg1GK8TPrIAwPjgceeMD69zbmNmzY4KSmpppvW4bdCIfDzlNPPWV9KLrC3LlzncqVKyd8HyQnJzvHHHOM88ADDzhr16613gyet3z5cqdWrVrm5xaDcZDxM+sgDA+O888/34lGo9a/tzF33nnnmW9bht34wx/+4EQiEevD0NwPP/zgNGjQwHx/ZGVlOZdeeqkzY8YMp6ioyHqzeNbkyZMTsuLNYFRgKOmn/wUoq3379mnAgAFKT0+3jhJTlSpV0oQJE6xjwMCZZ56pMWPGlPrdqX61b98+XXjhhVq8eLF1FBUXF2vZsmUaP368Xn31VaWmpqpJkyY8wV9GTZs21a5du/TZZ59ZRwEO5O6QKKUop3A4rLlz5+rEE0+0jhJTJSUlatSokTZs2GAdBQl01FFHafbs2YF/0j4ajWrQoEF65plnrKP8plAopEMOOUR/+MMfdNVVVwV+f5XFvn371KlTJ82bN886CvCbeA8Hyi0ajerDDz+0jhFzycnJGjRokHUMJFC1atU0adIkCo6khx9+WGPGjLGOcUCO42jDhg267bbb1KJFC917773avn27dSxPyMzM1JgxY1SlShXrKMABWd9DwPDwOOuss3x5X+nWrVudtLQ08+3LiP9ITU113njjDetDzhWmTJniyeO+Vq1azr///W8nLy/PehN6wlNPPeWEw2Hz/cZg/MYwD8Dw8KhZs6azbds269/YuLjmmmvMty8jviMUCjkPPvig9aHmCkuXLnVq1qxpvk8qMho2bOiMHz+e10kdRCQScS644ALz/cVg/MYwD8Dw+Jg2bZr1b2xcLF26lNUEn4++ffv6cqW/rLZs2eIcffTR5vsjFiMUCjknn3yy88UXX1hvVldbt26dU6NGDfP9xWDsP5Ik3S2gAmrWrKlzzjnHOkbM5eTkaMmSJVq2bJl1FMTBySefrFdeeSXwT9rn5+erZ8+emjNnjnWUmNmwYYPGjBmjzZs3q127dr57Q0gsVKlSRTk5OZoyZUrMv9QFVIR5M2Z4ezRv3tzJz8+3/sM/LhYuXMhqqQ9Hw4YNnY0bN1ofXq4wdOhQJxQKme+TeI06deo4kydPZkX8N0QiEef8888330cMxn7DPADD4yMcDjvz58+3/n2Ni2g06lx66aXm25gRu5GZmenMnTvX+tByhaefftpJTk423yfxHqFQyOnVq5ezefNm603uOuvXr3eqVatmvo8YDEkOr4RChUWjUU2bNs06RlyEQiHdeeedSkpKso6CGAiFQnr22WfVpk0b6yjmPvjgAw0dOlQlJSXWUeLOcRyNHz9eLVu21LRp07hcvZ/69evrnnvuUSgUso4CSHJBM2Z4f7Ru3dq3nwCMRqNO//79zbcxo+Ljz3/+M5dxHcf59ttvnUMPPdR8f1iMpKQk56abbnJyc3Otd4NrlJSUOG3btjXfNwwGX3RCTCQnJ2vx4sVq1qyZdZS4WLNmjY455hjt3bvXOgrK6ZJLLtGECRMC/2DTzp07dfbZZ+vzzz+3jmKqTZs2GjdunI488kjrKK7wxRdf6JRTTlFxcbF1FAQYT98jJqLRqOrVq6dTTz3VOkpcVKtWTfn5+fr444+to6AcWrZsqVdffVVZWVnWUUwVFxdrwIABmj59unUUcxs3btTEiRPVpEkTHXXUUYG/fF2vXj1t2rQp8H+swJ75ci3DH+PEE090CgsLra9Exc2uXbucBg0amG9nRtlGTk6Os2rVKuvDx1w0GnXuvvtuXz9pX56RnJzs3H333b7+7Sqtbdu2ObVq1TLfJ4zgDlZKETNbtmzRRRddpLp161pHiYv09HTVqVNHr776qnUUlFJ6erpef/11nXDCCdZRzE2cOFFDhw5VJBKxjuIq0WhUH330kVauXKnOnTsH+p2mmZmZys7O1ttvv20dBQFm3owZ/hm333679R/7cVVSUuKce+655tuZcfARDoedxx9/3PqQcYV58+Y5VatWNd8nbh+nnHKKs3btWuvdZaqwsNA59thjzfcFI7DDPADDR+Pwww938vLyrH9X42rFihVM8B4YgwcPdiKRiPXhYu67775zGjdubL4/vDKaNWvmLFq0yHq3mXrrrbe4zYNhMrh8j5jauXOn2rdvryZNmlhHiZsaNWooNTWVh0Vc7Oyzz9azzz4b+Cftc3Nz1b17d82fP986imds27ZNb7zxhtq1a6dDDz3UOo6JJk2a6NNPP9WqVausoyCAzJsxw1/j8ssv9/27IAsKCpyOHTuab2vGr8dRRx3lbNq0yfoQMVdUVORcc8015vvDqyMnJ8d59913rXejmblz5zpJSUnm+4ERuGEegOGzUblyZee7776z/k2Nu+XLlzvVq1c3396M/x85OTnOl19+aX1omItGo85DDz3khMNh833i5VGlShVnypQpvv8j+0DHUK9evcz3ASNYg8v3iLnCwkLVqlVL7du3t44SVzVr1lT16tX19ttv89lCF0hPT9fzzz+vTp06WUcxN3XqVF133XUqKiqyjuJphYWFmjJlio455hgdddRR1nESKhQKqXnz5ho1ahRvbEBCmTdjhv/GkUce6ezbt8/6j/24Ky4udnr27Gm+vYM+QqGQc9999wVyRet/LVq0yKlbt675PvHTqFKlijN16lTrXZtw0WjUueKKK8y3PyNQwzwAw6fjzTfftP5NTYgtW7Y4TZs2Nd/eQR79+/d3ioqKrA8Fc5s2beJ1PnEatWrVcmbPnm29ixPu66+/dtLS0sy3PyMYIywgTp544olAXPbJycnR888/r8qVK1tHCaTTTjtNDz/8cOCftC8oKNC1116rxYsXW0fxpS1btqhbt25asGCBdZSEatq0qbp3724dAwFi3owZ/hxJSUnO4sWLrf/QT5jRo0c7ycnJ5ts9SKNRo0Z8QtRxnEgk4txyyy28WzIB46ijjnJWr15tvcsTaunSpfy2MRIyeNAJceM4jgoKCtS1a1eFQiHrOHF33HHHac+ePfrss8+sowRClSpVNHnyZB177LHWUcw988wz+stf/qJoNGodxfe2b9+uefPm6eKLL1ZGRoZ1nISoUaOG5s+frxUrVlhHQQCYN2OGf0d2drazfv166z/0E2bv3r1Oly5dzLe730dycrIzbtw4693tCh999JGTnZ1tvk+CNi677DInPz/fevcnzCeffMIrxhhxH6yUIq6KiooUDod11llnWUdJiJSUFJ155pl67733tGnTJus4vhQKhXT77bfrD3/4g8LhYN8Wv2rVKnXt2lXbtm2zjhI4y5YtkyR17NgxEFeC6tWrp5kzZ+q7776zjgKfM2/GDH+PatWqOVu2bLH+Qz+hvv32W6dRo0bm296P49JLLw3UCtWB7Ny50znppJPM90eQR0pKivPyyy9bHwoJM3nyZPNtzvD3CPYyAxJi586deuaZZ6xjJNQRRxyhCRMmqF69etZRfKVly5Z66qmnlJ6ebh3FVFFRkYYMGaK5c+daRwm04uJiXX/99YF548E555yjJk2aWMeAj1FKkRCPPPKIdu7caR0jodq0aaMJEyaoevXq1lF8oV69enrxxRdVs2ZN6yimotGo7rvvPo0fP946CiRt27ZNAwYM0O7du62jxF1mZqYGDBhgHQM+xj2lSIi8vDxVrVrV958e/V8NGjRQ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l+HpIbkgBIPxmyM9Pd157733rH9rPW3VqlXOPffc4zRq1CjhE0Z2drazYMEC603gCo8++qiTlJRkfk4x3DGGDRtmfUjGXG5urtOwYUPzbcvw7NBP19IcAS5Vv359ffLJJzrssMOso3haYWGh3nvvPY0dO1YzZszQzp074/r/Lzk5WRMnTtQll1xi/U83N336dF100UXKz8+3jgKXOPTQQ7VmzRolJydbR4mpiy++2Jf3zCIhQtxTCtf77rvvNHDgQCb0CkpLS1OXLl00ceJEffPNNxozZoy6dOmiatWqxfxez1AopLvuuotCKmnFihXq168fxy9+YePGjfrwww+tY8TcySefbB0BHvbTkxB3WwcBfs+aNWuUn5+vM888M/APy8RCZmamjj/+ePXq1UsDBgzQiSeeqEqVKmnnzp3at2+fotFohf77e/Toof/85z8Kh4P9d+/27dt1wQUXaNWqVdZR4EIpKSm68MILrWPElOM4vn0XK+LuHi7fwzOSk5M1atQo9e/f3zqKbxUWFmrx4sWaNWuWPvzwQ33xxRfavn17mT5r2qZNG73//vvKysqy/ueYKiwsVK9evfTqq69aR4FL1ahRQ+vXr/fV53Y3bdqkpk2bavfu3dZR4D0hSik8pXLlypoyZYpOO+006yiBkJ+fr2+++UYLFizQvHnztHDhQq1atUp79uxRQUHBr/7z9erV02effab69etbRzcVjUZ155136v777+eLTTigUCikqVOn6pxzzrGOEjMlJSVq06aNFixYYB0F3kMphfc0bNhQM2bMUOPGja2jBFJubq42bNig1atXa8WKFVq1apXWrl2rjRs3asSIETr11FOtI5p77rnndPXVV6ukpMQ6Clzu2muv1ZNPPmkdI6b69++v559/3joGvIdSCm866aST9Pbbb6tGjRrWUYBf2LVrl/7yl79oz5491lF+IT8//6D3CpeUlGjHjh3atGmTduzYoV27dikSiVhH97VDDjlEa9eu9dVT+I8++qhuvPFG6xjwHkopvOvCCy/U+PHjfXU/FuAG0WhU27dv15o1azR37ly98847mjt3rrZv324dzXeSkpI0d+5ctWrVyjpKzHz00Ufq2LGjdQx4D6+Egne9+eabuummm/gUKRBj4XBYOTk5atOmjW644Qa9/fbbWrp0qcaMGaO2bdv6alXPWiQS0YwZM6xj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";
            String selectedMappings = (String) mappingsSelection.getSelectedItem();
            String mappings = selectedMappings.isEmpty() ? "" : ";mappings=" + selectedMappings;
            installation.javaArgs =
                    "-Xmx"
                            + memory
                            + "G -Xms"
                            + memory
                            + "G "
                            + "-javaagent:sorus/client/"
                            + version
                            + ".jar=version="
                            + version
                            + mappings;
            launcherProfiles.profiles.put("Sorus", installation);
            FileWriter fileWriter = new FileWriter(launcherProfile);
            fileWriter.write(gson.toJson(launcherProfiles));
            fileWriter.close();
            new Thread(
                    () -> {
                        setErrorLabelText("");
                        this.doneLabel.setText("Installing...");
                        try {
                            File temp = new File(minecraftPath + "/sorus/client/temp/");
                            boolean environmentDifferent = this.downloadIfDifferent(
                                    new URL(
                                            "https://github.com/SorusClient/Sorus-Resources/raw/master/client/environments/Agent-Injection.jar"),
                                    new File(temp, "Agent-Injection.jar"));
                            boolean versionDifferent = this.downloadIfDifferent(
                                    new URL(
                                            "https://github.com/SorusClient/Sorus-Resources/raw/master/client/versions/"
                                                    + version
                                                    + ".jar"),
                                    new File(temp, version + ".jar"));
                            boolean coreDifferent = this.downloadIfDifferent(
                                    new URL(
                                            "https://github.com/SorusClient/Sorus-Resources/raw/master/client/Core.jar"),
                                    new File(temp, "Core.jar"));
                            if(environmentDifferent || versionDifferent || coreDifferent) {
                                JarOutputStream output = new JarOutputStream(new FileOutputStream(minecraftPath + "/sorus/client/" + version + ".jar"));
                                this.combineJars(output, new JarFile(new File(temp, "Agent-Injection.jar")), new JarFile(new File(temp, version + ".jar")), new JarFile(new File(temp, "Core.jar")));
                            }
                        } catch (IOException ex) {
                            setErrorLabelText(ex.getClass().getSimpleName());
                            showErrorDialog(ex);
                        }
                        if (!((String) mappingsSelection.getSelectedItem()).isEmpty()) {
                            try {
                                this.downloadIfDifferent(
                                        new URL(
                                                "https://github.com/SorusClient/Sorus-Resources/raw/master/mappings/"
                                                        + mappingsSelection.getSelectedItem()
                                                        + ".txt"),
                                        new File(
                                                minecraftPath
                                                        + "/sorus/mappings/"
                                                        + mappingsSelection.getSelectedItem()
                                                        + ".txt"));
                            } catch (IOException ex) {
                                setErrorLabelText(ex.getClass().getSimpleName());
                                showErrorDialog(ex);
                            }
                        }
                        this.loadingIcon.setIcon(doneImage);
                        this.doneLabel.setText("Done.");
                        this.installButton.setEnabled(false);
                    })
                    .start();
        } catch (IOException ex) {
            setErrorLabelText(ex.getClass().getSimpleName());
            showErrorDialog(ex);
        }
    }

    private boolean downloadIfDifferent(URL url, File file) throws IOException {
        InputStream inputStream = url.openStream();
        boolean needToDownload;
        file.getParentFile().mkdirs();
        try {
            InputStream inputStream1 = new FileInputStream(file);
            needToDownload = !IOUtils.contentEquals(inputStream, inputStream1);
            inputStream1.close();
        } catch (FileNotFoundException e) {
            needToDownload = true;
        }
        if (needToDownload) {
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            return true;
        }
        return false;
    }

    private void combineJars(JarOutputStream output, JarFile... jars) {
        List<String> added = new ArrayList<>();
        try {
            for(JarFile jar : jars) {
                Enumeration<JarEntry> entries = jar.entries();
                while(entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if(added.contains(entry.getName())) {
                        continue;
                    }
                    InputStream inputStream = jar.getInputStream(entry);
                    output.putNextEntry(new JarEntry(entry.getName()));
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    output.flush();
                    output.closeEntry();
                    added.add(entry.getName());
                }
            }
            output.flush();
            output.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void showInstallingIcon() {
        minecraftInstallSelection.setVisible(false);
        clientVersionSelection.setVisible(false);
        installButton.setVisible(false);
        mappingsSelection.setVisible(false);
        mappingsLabel.setVisible(false);
        minecraftInstallLabel.setVisible(false);
        clientVersionLabel.setVisible(false);

        loadingIcon = new JLabel();
        loadingIcon.setIcon(loadingGif);
        loadingIcon.setLocation(90, 200);
        loadingIcon.setSize(200, 200);
        this.add(loadingIcon);
    }

    // This function takes an exception as a parameter and opens a new window that displays the stack
    // trace of the exception in a text area.
    private void showErrorDialog(Exception e) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        JDialog errorDialog = new JDialog();
        errorDialog.setTitle(e.getClass().getSimpleName());
        errorDialog.setSize(400, 400);
        errorDialog.setLocation(
                (int) (screenSize.getWidth() / 2 - 400 / 2), (int) (screenSize.getHeight() / 2 - 400 / 2));

        JTextArea errorArea = new JTextArea();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        errorArea.setText(sw.toString());
        errorArea.setLocation(0, 0);
        errorArea.setSize(400, 400);
        errorArea.setEditable(false);

        JScrollPane errorScrollPane =
                new JScrollPane(
                        errorArea,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        errorDialog.add(errorScrollPane);
        errorDialog.setVisible(true);
    }

    // This boolean checks if Sorus and Minecraft versions match
    private boolean checkVersionsMatching() {
        return minecraftInstallSelection
                .getSelectedItem()
                .toString()
                .contains(clientVersionSelection.getSelectedItem().toString());
    }

    // This boolean checks if mappings are selected while installing the client on forge
    private boolean checkForgeMappings() {
        String lowercase = ((String) minecraftInstallSelection.getSelectedItem()).toLowerCase();
        String mappings = (String) mappingsSelection.getSelectedItem();
        return lowercase.contains("forge") && mappings.equals("")
                || !lowercase.contains("forge") && !mappings.equals("");
    }

    // This function sets the text of the error label and centers it
    private void setErrorLabelText(String s) {
        errorLabel.setText(s);
        errorLabel.setLocation(
                190 - errorLabel.getFontMetrics(errorLabel.getFont()).stringWidth(errorLabel.getText()) / 2,
                435);
    }

    // This function overrides the panels paintComponent method and sets the background image
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(this.bgImage, 0, 0, null);
    }

    private JSONObject getLibraryObject(String name) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        return jsonObject;
    }

}
