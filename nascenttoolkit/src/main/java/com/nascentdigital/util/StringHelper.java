package com.nascentdigital.util;


import android.content.Context;
import android.text.Html;
import android.text.SpannedString;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class StringHelper {
    public static final String Space = " ";

    //region public methods

    /***
     * Replaces all occurrences of a string sequence with another string sequence
     *
     * @param value       String that will be operated on
     * @param oldSequence Sequence that will be replaced by newSequence
     * @param newSequence Sequence that will replace oldSequence
     * @return New string value with all occurrences of oldSequence replaced by newSequence
     */
    public static String replace(String value, String oldSequence, String newSequence) {
        if (isNullOrEmpty(value))
            return value;

        while (value.contains(oldSequence)) {
            value = value.replace(oldSequence, newSequence);
        }

        return value;
    }

    /**
     * Converts and formats a double value into a string number representation.
     *
     * @param input  double value to be converted and formatted
     * @param locale locale to use when formatting double value
     * @return string representation of input
     */
    public static String formatDoubleAsNumber(double input, Locale locale) {
        NumberFormat nf = NumberFormat.getNumberInstance(locale);
        return nf.format(input);
    }

    /**
     * Rounds a string representation of a number to 0 decimal points
     *
     * @param value string value (must be in numerical format)
     * @return string representation rounded to 0 decimal points
     */
    @Deprecated
    public static String roundNoDecimal(String value) {
        return roundNoDecimal(value, RoundingMode.HALF_UP);
    }

    /**
     * Rounds a string representation of a number to 0 decimal points
     *
     * @param value string value (must be in numerical format)
     * @return string representation rounded to 0 decimal points
     */
    public static String roundNoDecimal(String value, RoundingMode mode) {
        value = value.replace(",", ".");
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(0, mode);
        return bd.toString();
    }

    /**
     * Rounds a string representation of a number to 1 decimal point
     *
     * @param value string value (must be in numerical format)
     * @return string representation rounded to 1 decimal point
     */
    @Deprecated
    public static String roundOneDecimal(String value) {
        return roundOneDecimal(value, RoundingMode.HALF_UP);
    }

    /**
     * Rounds a string representation of a number to 1 decimal point
     *
     * @param value string value (must be in numerical format)
     * @return string representation rounded to 1 decimal point
     */
    public static String roundOneDecimal(String value, RoundingMode mode) {
        value = value.replace(",", ".");
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(1, mode);
        return bd.toString();
    }

    /**
     * Rounds a string representation of a number to 2 decimal points
     *
     * @param value string value (must be in numerical format)
     * @return string representation rounded to 2 decimal points
     */
    @Deprecated
    public static double roundTwoDecimal(double value) {
        return roundTwoDecimal(value, RoundingMode.HALF_UP);
    }

    /**
     * Rounds a string representation of a number to 2 decimal points
     *
     * @param value string value (must be in numerical format)
     * @return string representation rounded to 2 decimal points
     */
    public static double roundTwoDecimal(double value, RoundingMode mode) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, mode);
        return bd.doubleValue();
    }

    /**
     * Formats a 10 digit phone number to 555-555-5555
     *
     * @param input 10 digit string (ie. 5555555555)
     * @return a formatted 10 digit phone number or the original string if the input is not 10 digits.
     */
    public static String formatPhoneNumber(String input) {
        if (input == null || input.length() != 10) {
            return input;
        }
        return input.substring(0, 3) + "-" + input.substring(3, 6) + "-" + input.substring(6, 10);
    }

    /***
     * Converts a string to title case (ie. Title Case)
     *
     * @param input string to be converted (eg, tiTle caSe)
     * @return string formatted in title case (eg, Title Case)
     */
    public static String toTitleCase(String input) {
        input = input.toLowerCase();
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }

            titleCase.append(c);
        }

        return titleCase.toString();
    }

    /**
     * Simple String transformation by XOR-ing all characters by value. Used to obfuscate and deobfuscate
     * string values.
     */
    static String stringTransform(String s, int i) {
        char[] chars = s.toCharArray();
        for (int j = 0; j < chars.length; j++)
            chars[j] = (char) (chars[j] ^ i);
        return String.valueOf(chars);
    }

    /***
     * @param string
     * @return true if string is null or empty ("")
     */
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.equals("");
    }

    /**
     * @param string
     * @return true if string is null or whitespace (" ")
     */
    public static boolean isNullOrWhitespace(String string) {
        return string == null || "".equals(string.trim());
    }

    /**
     * Detects if string is null, and returns string or default value.
     *
     * @param string
     * @param defaultString
     * @return defaultString if string is null, or string if string is not null
     */
    public static String isNull(String string, String defaultString) {
        if (string == null) {
            return defaultString;
        }
        return string;
    }

    /**
     * Appends a list of strings together into one string, separated by the supplied delimiter.
     *
     * @param strings
     * @param delimiter
     * @return
     */
    public static String appendStrings(List<String> strings, String delimiter) {
        if (strings == null || strings.size() == 0) {
            return "";
        }
        if (delimiter == null) {
            delimiter = "";
        }

        String result = "";
        for (String str : strings) {
            result += str + delimiter;
        }

        return result.substring(0, result.length() - delimiter.length());
    }

    /**
     * Capitalizes the first letter of value
     *
     * @param value
     * @return
     */
    public static String capitalize(String value) {
        if (value.length() == 0) return value;
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    /**
     * Set formatted text with placeholders while keeping the formatting
     *
     * @param id
     * @param args
     * @return
     */
    public static CharSequence getFormattedTextWithPlaceholders(int id, Context context, Object...
            args) {
        for (int i = 0; i < args.length; ++i)
            args[i] = args[i] instanceof String ? TextUtils.htmlEncode((String) args[i]) : args[i];
        return Html.fromHtml(String.format(Html.toHtml(new SpannedString(context.getText(id))),
                args));
    }

    /**
     * Convert a map to string of key value pairs. Used for debugging
     */
    public static String mapToString(HashMap<String, Object> map) {
        StringBuilder data = new StringBuilder();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            data.append(e.getKey() + " : " + e.getValue().toString() + "\n");
        }

        return data.toString();
    }
    //endregion
}
