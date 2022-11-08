package entities;

public class Constants {

    public static int COUNT_UPPERCASE = 26;
    public static int OFFSET_UPPERCASE_MAINSPRITES = Integer.parseInt("34600",16);
    public static int OFFSET_UPPERCASE_SIDESPRITES = Integer.parseInt("35B00",16);

    public static int COUNT_LOWERCASE = 66;
    public static int OFFSET_LOWERCASE_MAINSPRITES = Integer.parseInt("34600",16) + Integer.parseInt("900",16);
    public static int OFFSET_LOWERCASE_SIDESPRITES = Integer.parseInt("35B00",16) + Integer.parseInt("480",16);

    public static int COUNT_DIGITS = 11;
    public static int OFFSET_DIGITS_MAINSPRITES = OFFSET_UPPERCASE_MAINSPRITES + Integer.parseInt("4B0",16);

    public static int COUNT_SMALL_LATIN = 80;
    public static int OFFSET_SMALL_LATIN = Integer.parseInt("33A70",16);

    public static String TRANSLATION_FILE_POINTER = "POINTER";
    public static String TRANSLATION_FILE_DATA = "DATA";
    public static String TRANSLATION_FILE_JPN = "JPN";
    public static String TRANSLATION_FILE_ENG = "ENG";
}
