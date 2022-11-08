package services.pointers;

import java.util.Map;
import java.util.TreeMap;

public class PointerTable {

    String name = "";
    
    int offsetStart;
    int offsetEnd;
    int shift;
    
    int newOffsetStart;
    
    Map<Integer, PointerEntry> pointers = new TreeMap<>();

    public PointerTable(int offsetStart, int offsetEnd, int shift, int newOffsetStart) {
        this.offsetStart = offsetStart;
        this.offsetEnd = offsetEnd;
        this.shift = shift;
        this.newOffsetStart = newOffsetStart;
    }

    public void loadPointers(byte[] data) {
        for (int offset=getOffsetStart();offset<getOffsetEnd();offset = offset + 2) {
            byte a = data[offset];
            byte b = data[offset+1];
            int value = getShift() + ((b & 0xFF) * 0x100) + (a & 0xFF);
            PointerEntry pointer = new PointerEntry(offset, value, getShift());
            add(pointer);
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

    public int getOffsetStart() {
        return offsetStart;
    }

    public void setOffsetStart(int offsetStart) {
        this.offsetStart = offsetStart;
    }

    public int getOffsetEnd() {
        return offsetEnd;
    }

    public void setOffsetEnd(int offsetEnd) {
        this.offsetEnd = offsetEnd;
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
    
    /*public void applyTranslation(Map<Integer, Translation> translationMap) {
        for (Map.Entry<Integer, PointerEntry> e : pointers.entrySet()) {
            PointerEntry p = e.getValue();
            int offset = p.getOffset();
            
        }
    }*/
    
}
