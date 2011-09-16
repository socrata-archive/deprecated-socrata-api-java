package com.socrata.util;

import java.util.regex.Pattern;

public class Strings
{
    private static Pattern UNDERSCOREIZE_STRIP_LEADING_PATTERN = Pattern
        .compile("^[^A-z]+", Pattern.CASE_INSENSITIVE);
    private static Pattern UNDERSCOREIZE_STRIP_LEADING_XML_PATTERN = Pattern
        .compile("^xml", Pattern.CASE_INSENSITIVE);
    private static Pattern UNDERSCOREIZE_REPLACE_INVALID_CHARS_PATTERN = Pattern
        .compile("[^A-z0-9]+", Pattern.CASE_INSENSITIVE);
    private static Pattern UNDERSCOREIZE_REPLACE_DUPES_PATTERN = Pattern
        .compile("_+", Pattern.CASE_INSENSITIVE);

    public static String underscoreize(String input)
    {
        String output = UNDERSCOREIZE_STRIP_LEADING_PATTERN.matcher(input).replaceAll("_");
        output = UNDERSCOREIZE_STRIP_LEADING_XML_PATTERN.matcher(output).replaceAll("_");
        output = UNDERSCOREIZE_REPLACE_INVALID_CHARS_PATTERN.matcher(output).replaceAll("_");
        output = UNDERSCOREIZE_REPLACE_DUPES_PATTERN.matcher(output).replaceAll("_");
        return output.toLowerCase();
    }
}
