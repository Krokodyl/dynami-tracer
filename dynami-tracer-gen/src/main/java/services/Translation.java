package services;

public class Translation {
    
    int offsetData;
    byte[] data;// english data
    String japanese;
    String english;
    
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

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
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
