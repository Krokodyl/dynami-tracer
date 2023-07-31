package enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Language {
    
    ENGLISH("ENG"),
    FRENCH("FRA");

    private final String code;

    Language(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    public static List<String> codes() {
        return Arrays.stream(Language.values()).map(Language::getCode).collect(Collectors.toList());
    }
    
    public static Language getLanguage(String code) {
        for (Language value : Language.values()) {
            if (value.code.equals(code)) return value;
        }
        return null;
    }
}
