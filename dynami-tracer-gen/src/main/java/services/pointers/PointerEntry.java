package services.pointers;

public class PointerEntry {
    
    int offset;
    int value;
    int shift;
    byte[] data;

    int newOffset;
    int newValue;
    byte[] newData;

    public PointerEntry(int offset, int value, int shift) {
        this.offset = offset;
        this.value = value;
        this.shift = shift;
    }

    public PointerEntry(int offset, int value, int shift, int newOffset) {
        this.offset = offset;
        this.value = value;
        this.shift = shift;
        this.newOffset = newOffset;
    }

    public int getNewOffset() {
        return newOffset;
    }

    public void setNewOffset(int newOffset) {
        this.newOffset = newOffset;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getShift() {
        return shift;
    }

    public void setShift(int shift) {
        this.shift = shift;
    }

    public int getNewValue() {
        return newValue;
    }

    public void setNewValue(int newValue) {
        this.newValue = newValue;
    }
}
