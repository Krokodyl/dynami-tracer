package services.pointers;

import enums.Language;
import services.DataReader;
import services.Dictionary;
import services.Translation;
import services.vwf.Font;

import java.util.*;
import java.util.stream.Collectors;

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

    public void checkTranslationsLength(Language targetLanguage) {
        for (Map.Entry<Integer, Translation> e : translationMap.entrySet()) {
            Translation translation = e.getValue();
            String value = translation.getTranslation(targetLanguage);
            if (!translation.isAddressReplacement() && value!=null && !value.isEmpty()) {
                for (String specialCode : Font.SPECIAL_CODES) {
                    value = value.replaceAll(specialCode.replaceAll("\\{", "\\\\{"),"§");
                }
                String[] split = value.split("§");
                for (String s : split) {
                    int length = Font.getStringLength(s);
                    if (Math.ceil(length/8.0)>maxLineLength) {
                        System.err.println(String.format("%d px\t%d tiles\t%s", length, (int)Math.ceil(length/8.0), s));
                        System.err.println("Suggestion:");
                        String[] strings = Font.splitString(Font.stripStringSpecialCode(s), maxLineLength * 8);
                        for (String s1 : strings) {
                            System.err.println(s1);
                        }
                        String collect = Arrays.stream(strings).collect(Collectors.joining("{NL}"));
                        System.err.println(collect+"{EL}");
                    }
                        
                    //else System.out.println(String.format("%d px\t%d tiles\t%s", length, length/8, s));
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
