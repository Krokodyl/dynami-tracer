package services.pointers;

import services.DataReader;
import services.Dictionary;
import services.Translation;
import services.vwf.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static services.Utils.h;

public class PointerTable {

    String name = "";
    
    List<PointerRange> ranges = new ArrayList<>();
    int shift;
    
    int newOffsetStart;

    Map<Integer, Translation> translationMap;
    Map<Integer, PointerEntry> pointers = new TreeMap<>();
    
    PointerTableType type;
    
    int maxLineLength = 25; // tile counts

    public PointerTable(int shift, int newOffsetStart) {
        this.shift = shift;
        this.newOffsetStart = newOffsetStart;
    }

    public int getMaxLineLength() {
        return maxLineLength;
    }

    public void setMaxLineLength(int maxLineLength) {
        this.maxLineLength = maxLineLength;
    }

    public void addRange(PointerRange range) {
        ranges.add(range);
    }

    public void loadPointers(byte[] data) {
        for (PointerRange range : ranges) {
            for (int offset=range.getOffsetStart();offset<range.getOffsetEnd();offset = offset + 2) {
                byte a = data[offset];
                byte b = data[offset+1];
                int value = getShift() + ((b & 0xFF) * 0x100) + (a & 0xFF);
                PointerEntry pointer = new PointerEntry(offset, value, getShift());
                add(pointer);
            }
        }
    }

    public Map<Integer, PointerEntry> getPointers() {
        return pointers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getShift() {
        return shift;
    }

    public void setShift(int shift) {
        this.shift = shift;
    }

    public int getNewOffsetStart() {
        return newOffsetStart;
    }

    public void setNewOffsetStart(int newOffsetStart) {
        this.newOffsetStart = newOffsetStart;
    }

    public void add(PointerEntry pointer) {
        pointers.put(pointer.getOffset(), pointer);
    }

    public PointerTableType getType() {
        return type;
    }

    public void setType(PointerTableType type) {
        this.type = type;
    }

    public void loadTranslations(Dictionary dictionary) {
        translationMap = DataReader.loadTranslations(this, dictionary, true);
    }
    
    public void loadTranslations(Dictionary dictionary, boolean replaceMissingWithAddress) {
        translationMap = DataReader.loadTranslations(this, dictionary, replaceMissingWithAddress);
    }

    public void checkTranslationsLength() {
        for (Map.Entry<Integer, Translation> e : translationMap.entrySet()) {
            Translation translation = e.getValue();
            String english = translation.getEnglish();
            if (!translation.isAddressReplacement() && english!=null && !english.isEmpty()) {
                english = english.replaceAll("\\{NL}","§");
                english = english.replaceAll("\\{WPCL}","§");
                String[] split = english.split("§");
                for (String s : split) {
                    int length = Font.getStringLength(s);
                    if (length/8>maxLineLength)
                        System.err.println(String.format("%d px\t%d tiles\t%s", length, length/8, s));
                    else System.out.println(String.format("%d px\t%d tiles\t%s", length, length/8, s));
                }


            }
        }

    }

    public void writeEnglish(byte[] data) {
        int offsetData = getNewOffsetStart();
        for (Map.Entry<Integer, PointerEntry> e : pointers.entrySet()) {
            PointerEntry p = e.getValue();
            int offset = p.getOffset();
            Translation translation = translationMap.get(p.getValue());
            byte[] bytes = translation.getData();

            // Write new pointer value
            int value = offsetData - p.getShift();
            data[offset] = (byte) ((value % 256) & 0xFF);
            data[offset + 1] = (byte) (value / 256);

            // Write new data
            if (bytes!=null) {
                //System.out.println(h(offsetData)+"\t"+translation.getEnglish());
                for (int i = 0; i < bytes.length; i++) {
                    data[offsetData++] = bytes[i];
                }
            }
        }
    }

    /*public void applyTranslation(Map<Integer, Translation> translationMap) {
        for (Map.Entry<Integer, PointerEntry> e : pointers.entrySet()) {
            PointerEntry p = e.getValue();
            int offset = p.getOffset();
            
        }
    }*/
    
}
