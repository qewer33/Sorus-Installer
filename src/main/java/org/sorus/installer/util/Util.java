package org.sorus.installer.util;

import java.util.Random;

public class Util {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";

    public static String generateRandomString(int length) {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < length; i++) {
            builder.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return builder.toString();
    }

}
