package images;

import enums.EditorColor;
import services.Utils;

public class PixelLine2Bits {

    // 4 bytes
    byte[] data;

    static int[] pixelFlags = new int[8];
    static {
        pixelFlags[0] = Integer.parseInt("80", 16);
        pixelFlags[1] = Integer.parseInt("40", 16);
        pixelFlags[2] = Integer.parseInt("20", 16);
        pixelFlags[3] = Integer.parseInt("10", 16);
        pixelFlags[4] = Integer.parseInt("8", 16);
        pixelFlags[5] = Integer.parseInt("4", 16);
        pixelFlags[6] = Integer.parseInt("2", 16);
        pixelFlags[7] = Integer.parseInt("1", 16);
    }
    
    PixelLine2Bits(byte a, byte b) {
        data = new byte[2];
        data[0] = a;
        data[1] = b;
    }

    public byte getByte(int i) {
        return data[i];
    }

    // from 0 to 7
    public EditorColor getPixelColor(int pixel) {
        int flag = pixelFlags[pixel];
        String s = "" + getBit(data[0], flag) + getBit(data[1], flag);
        int fourBitsValue = Integer.parseInt(s, 2);
        return EditorColor.getEditorColor(fourBitsValue);
    }

    public void setPixelColor(int pixel, EditorColor color) {
        int flag = pixelFlags[pixel];
        int fourBitsValue = color.getFourBitsValue();
        String binaryString = Integer.toBinaryString(fourBitsValue);
        binaryString = Utils.padLeft(binaryString,'0',4);
        for (int i=0;i<4;i++) {
            int val = Integer.parseInt("" + binaryString.charAt(i)) * flag;
            data[i] = (byte)(data[i] + val);
        }
    }

    private int getBit(byte b, int flag) {
        if ((b & flag)==flag) return 1;
        else return 0;
    }
}
