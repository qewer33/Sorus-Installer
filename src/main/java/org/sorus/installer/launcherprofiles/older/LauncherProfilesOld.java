package org.sorus.installer.launcherprofiles.older;

import java.util.HashMap;
import org.sorus.installer.launcherprofiles.newer.LauncherSettingsNew;

public class LauncherProfilesOld {

  public HashMap<String, UserOld> authenticationDatabase;
  public String clientToken;
  public LauncherVersionOld launcherVersion;
  public HashMap<String, InstallationOld> profiles;
  public String selectedUser;
  public LauncherSettingsNew settings;
}
