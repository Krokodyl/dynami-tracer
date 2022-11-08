package images;

import enums.CharSide;
import enums.EditorColor;
import enums.GameColor;

import java.awt.image.BufferedImage;

/**
 * Represents an 8x8 image in the tile editor
 */
public class Sprite {

    // 32 bytes
    byte[] data;

    public Sprite(byte[] spriteData) {
        data = spriteData;
    }

    /**
     * from 0 to 7
     */
    public PixelLine2Bits getPixelLine(int line) {
        return new PixelLine2Bits(data[0+(2*line)],data[0+(2*line)+1]);
    }

    public BufferedImage getGameImage() {
        BufferedImage image = new BufferedImage(8, 8,
                BufferedImage.TYPE_INT_ARGB);
        for (int row=0;row<8;row++) {
            PixelLine2Bits pixelLine = getPixelLine(row);
            for (int col=0;col<8;col++) {
                EditorColor pixelColor = pixelLine.getPixelColor(col);
                image.setRGB(col, row, GameColor.getGameColor(pixelColor, CharSide.ONE).getColor().getRGB());
            }
        }
        /*for (int row=4;row<12;row++) {
            PixelLine2Bits pixelLine = getPixelLine(row-4);
            for (int col=0;col<8;col++) {
                EditorColor pixelColor = pixelLine.getPixelColor(col);
                image.setRGB(col, row, GameColor.getGameColor(pixelColor, CharSide.ONE).getColor().getRGB());
            }
        }*/
        return image;
    }
}
