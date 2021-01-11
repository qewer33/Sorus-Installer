package org.sorus.installer;

import java.util.List;
import java.util.Map;

public class JJson {

    public List < JLibrary > libraries;
    public String mainClass;
    public String minecraftArguments;

    private static class JLibrary {
        public JDownloads downloads;
        public String name;
        public Map <String, String> natives;
    }

    private static class JDownloads {
        public JArtifact artifact;
        public Map <String, JClassifier> classifiers;

        private static class JArtifact {
            public String path;
            public String sha1;
            public int size;
            public String url;
        }
    }

    public static class JClassifier {
        public String path;
        public String sha1;
        public int size;
        public String url;
    }

}