package services;

import junit.framework.TestCase;

import java.util.Map;
import java.util.TreeMap;

public class DictionaryTest extends TestCase {
    
    Map<String, Integer> reverseMap = new TreeMap<>();
    
    public void testGetCode() {
        reverseMap.put("~", 12);
        String value = "a~b";
        if (reverseMap.containsKey(value)) {
            System.out.println("yes");
        }
    }

    public void testApplyPresets() {
        String translation = "ABC the blank is the blank.";
        String value = "{3F}";
        String key = "the";
        translation = translation.replaceAll(key, value);
        System.out.println(translation);
    }
}