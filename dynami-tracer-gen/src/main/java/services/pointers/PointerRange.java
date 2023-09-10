package services.pointers;

public class PointerRange {
    
    int offsetStart;
    int offsetEnd;
    int newOffsetStart;

    public PointerRange(int offsetStart, int offsetEnd) {
        this.offsetStart = offsetStart;
        this.offsetEnd = offsetEnd;
        this.newOffsetStart = offsetStart;
    }

    public PointerRange(int offsetStart, int offsetEnd, int newOffsetStart) {
        this.offsetStart = offsetStart;
        this.offsetEnd = offsetEnd;
        this.newOffsetStart = newOffsetStart;
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

    public int getNewOffsetStart() {
        return newOffsetStart;
    }

    public void setNewOffsetStart(int newOffsetStart) {
        this.newOffsetStart = newOffsetStart;
    }
}
