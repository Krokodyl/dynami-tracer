package services.pointers;

import enums.Language;
import org.apache.commons.lang.StringUtils;
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
    int newOffsetShift;

    Map<Integer, Translation> translationMap;
    Map<Integer, PointerEntry> pointers = new TreeMap<>();
    
    PointerTableType type;
    
    int maxLineLength = 25; // tile counts

    public PointerTable(int shift, int newOffsetStart, int newOffsetShift) {
        this.shift = shift;
        this.newOffsetStart = newOffsetStart;
        this.newOffsetShift = newOffsetShift;
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
                if (range.getNewOffsetStart()>0) {
                    pointer.setNewOffset(range.getNewOffsetStart()+(offset- range.getOffsetStart()));
                }
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
    
    public void printTranslations(int start, int end, Language language, int cult) {
        for (Map.Entry<Integer, Translation> e : translationMap.entrySet()) {
            Translation translation = e.getValue();
            int offsetData = translation.getOffsetData();
            if (offsetData>=start && offsetData<=end) {
                String text = translation.getTranslation(language);
                if (offsetData>=cult) {
                    int i = text.indexOf("{NL}");
                    text = text.substring(i);
                }
                System.out.println(Font.stripStringSpecialCode(text));
            }
        }
    }

    public void checkTranslationsLength(Language targetLanguage, Dictionary latin) {
        if (translationMap==null) return;
        for (Map.Entry<Integer, Translation> e : translationMap.entrySet()) {
            Translation translation = e.getValue();
            String value = translation.getTranslation(targetLanguage);
            if (!translation.isAddressReplacement() && value!=null && !value.isEmpty()) {
                for (String specialCode : Font.SPECIAL_CODES) {
                    value = value.replaceAll(specialCode.replaceAll("\\{", "\\\\{"),"§");
                }
                value = latin.reversePresets(value);
                String[] split = value.split("§");
                for (String s : split) {
                    int length = Font.getStringLength(s);
                    if (Math.ceil(length/8.0)>maxLineLength) {
                        //System.err.println(String.format("%d px\t%d tiles\t%s", length, (int)Math.ceil(length/8.0), s));
                        System.err.println("Suggestion:");
                        String s2 = Font.stripStringSpecialCode(s);
                        String[] strings = Font.splitString(s2, maxLineLength * 8);
                        for (String s1 : strings) {
                            //System.err.println(s1);
                        }
                        String collect = Arrays.stream(strings).collect(Collectors.joining("{NL}"));
                        System.err.println(collect+"{EL}");
                    }
                        
                    //else System.out.println(String.format("%d px\t%d tiles\t%s", length, length/8, s));
                }


            }
        }

    }

    public void writeBlanks(byte[] data) {
        for (Map.Entry<Integer, PointerEntry> e : this.pointers.entrySet()) {
            PointerEntry value = e.getValue();
            int offsetData = value.getValue();
            byte[] dataValue = null;
            if (getType() == PointerTableType.SIZE_PREFIX) {
                int length = data[offsetData];
                dataValue = DataReader.readByteCount(data, offsetData, length);
            } else dataValue = DataReader.readUntilEndOfLine(data, offsetData);
            int length = dataValue.length;
            for (int i = offsetData;i<offsetData+length;i++) {
                data[i] = (byte) 0xBB;
            }
        }
        for (PointerRange range : ranges) {
            for (int offset=range.getOffsetStart();offset<range.getOffsetEnd();offset = offset + 2) {
                data[offset] = (byte) 0xAA;
                data[offset+1] = (byte) 0xAA;
            }
        }
    }

    public void writeEnglish(byte[] data) {
        int newLength = 0;
        int offsetData = getNewOffsetStart();
        for (Map.Entry<Integer, PointerEntry> e : pointers.entrySet()) {
            PointerEntry p = e.getValue();
            
            //int offset = p.getOffset();
            int newOffset = p.getNewOffset();
            Translation translation = translationMap.get(p.getValue());
            byte[] bytes = translation.getData();
            newLength += bytes.length;
            
            // Write new pointer value
            int value = offsetData - getNewShift();
            data[newOffset] = (byte) ((value % 256) & 0xFF);
            data[newOffset + 1] = (byte) (value / 256);

            // Write new data
            if (bytes!=null) {
                System.out.println(h(offsetData)+"\t"+translation.getTranslation(Language.ENGLISH));
                for (int i = 0; i < bytes.length; i++) {
                    data[offsetData++] = bytes[i];
                }
            }
        }
        //System.out.println(getName()+" new length = "+h(newLength));
    }

    public int getOldLength(byte[] bytes) {
        int length = 0;
        for (Map.Entry<Integer, PointerEntry> e : pointers.entrySet()) {
            PointerEntry value = e.getValue();
            int offsetData = value.getValue();
            byte[] dataValue = null;
            if (getType() == PointerTableType.SIZE_PREFIX) {
                //int length = data[offsetData];
                dataValue = DataReader.readByteCount(bytes, offsetData, length);
            } else dataValue = DataReader.readUntilEndOfLine(bytes, offsetData);
            length += dataValue.length;
        }
        //System.out.println(getName()+" length = "+h(length));
        return length;
    }
    
    public int getNewLength() {
        int newLength = 0;
        for (Map.Entry<Integer, PointerEntry> e : pointers.entrySet()) {
            PointerEntry p = e.getValue();
            Translation translation = translationMap.get(p.getValue());
            byte[] bytes = translation.getData();
            newLength += bytes.length;
        }
        //System.out.println(getName()+" new length = "+h(newLength));
        return newLength;
    }

    private int getNewShift() {
        return newOffsetShift;
    }

    public void updateFrequencies(Map<String, Integer> frequencies, Language l) {
        for (Map.Entry<Integer, PointerEntry> e : pointers.entrySet()) {
            PointerEntry p = e.getValue();
            Translation translation = translationMap.get(p.getValue());
            String s = translation.getTranslation(l);
            s = Font.stripStringSpecialCode(s);
            String[] split = s.split(" ");
            for (String word : split) {
                Integer integer = frequencies.get(word);
                if (integer==null) frequencies.put(word, 1);
                else frequencies.put(word, integer+1);
            }

        }
    }

    public void updateCommonFrequencies(Map<String, Integer> frequencies, Language l) {
        for (Map.Entry<Integer, PointerEntry> e : pointers.entrySet()) {
            PointerEntry p = e.getValue();
            Translation translation = translationMap.get(p.getValue());
            String s = translation.getTranslation(l);
            s = Font.stripStringSpecialCode(s);
            //String[] split = s.split(" ");
            /*for (String word : split) {
                Integer integer = frequencies.get(word);
                if (integer==null) frequencies.put(word, 1);
                else frequencies.put(word, integer+1);
            }*/
            for (Map.Entry<String, Integer> f : frequencies.entrySet()) {
                String key = f.getKey();
                int i = StringUtils.countMatches(s, key);
                frequencies.put(key, frequencies.get(key)+i);
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
