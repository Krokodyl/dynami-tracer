package services.pointers;

public class PointerRange {
    
    int offsetStart;
    int offsetEnd;

    public PointerRange(int offsetStart, int offsetEnd) {
        this.offsetStart = offsetStart;
        this.offsetEnd = offsetEnd;
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
}
