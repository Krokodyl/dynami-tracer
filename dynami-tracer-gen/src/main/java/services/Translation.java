package services;

import enums.Language;

import java.util.HashMap;
import java.util.Map;

public class Translation {
    
    int offsetData;
    byte[] data;// english data
    String japanese;
    Map<Language, String> translations = new HashMap<>();
    //String english;
    
    boolean addressReplacement = false;

    public int getOffsetData() {
        return offsetData;
    }

    public void setOffsetData(int offsetData) {
        this.offsetData = offsetData;
    }

    public String getJapanese() {
        return japanese;
    }

    public void setJapanese(String japanese) {
        this.japanese = japanese;
    }
    
    public void setTranslation(Language l, String value) {
        translations.put(l, value);
    }
    
    public String getTranslation(Language l) {
        return translations.get(l);
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isAddressReplacement() {
        return addressReplacement;
    }

    public void setAddressReplacement(boolean addressReplacement) {
        this.addressReplacement = addressReplacement;
    }
}
