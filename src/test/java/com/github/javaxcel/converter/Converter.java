package com.github.javaxcel.converter;

import java.util.StringTokenizer;

public final class Converter {

    private Converter() {
    }

    public static String capitalize(String str) {
        if (str == null) return null;

        StringBuilder sb = new StringBuilder();
        for (StringTokenizer tokenizer = new StringTokenizer(str, "-"); tokenizer.hasMoreTokens(); ) {
            String token = tokenizer.nextToken();
            sb.append(String.valueOf(token.charAt(0)).toUpperCase())
                    .append(token.substring(1))
                    .append('-');
        }

        return sb.substring(0, sb.length() - 1);
    }

}