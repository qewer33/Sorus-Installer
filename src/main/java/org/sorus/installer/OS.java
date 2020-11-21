package org.sorus.installer;

public class OS {

    public static OSType getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("win")) {
            return OSType.WINDOWS;
        } else if(os.contains("mac")) {
            return OSType.MAC;
        } else if(os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return OSType.LINUX;
        } else {
            return OSType.OTHER;
        }
    }

    public enum OSType {
        WINDOWS,
        MAC,
        LINUX,
        OTHER
    }

}
