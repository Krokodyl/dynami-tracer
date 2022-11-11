package entities;

public class Patch {
    
    int offset;
    byte[] data;

    public Patch(int offset, byte[] data) {
        this.offset = offset;
        this.data = data;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
    
    public void applyPatch(byte[] gameData) {
        int i = getOffset();
        for (byte b : getData()) {
            gameData[i++] = b;
        }
    }
}
