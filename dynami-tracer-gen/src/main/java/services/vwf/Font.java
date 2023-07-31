package services.vwf;

import entities.Patch;
import old.ImageParser;
import old.Palette2bpp;
import palette.ColorGraphics;
import resources.Bytes;
import resources.ResIO;
import services.DataWriter;
import services.Dictionary;
import services.Utils;
import tile.ColorDepth;
import tile.Tile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static services.Utils.x;

public class Font {

    final static String FONT_CHARACTERS = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]ˆ_`abcdefghijklmnopqrstuvwxyz{|}~α♪…āūēō♥";
    final static String FONT_SMALL_CHARACTERS = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]ˆ_`abcdefghijklmnopqrstuvwxyz{|}~β…γ";
    
    // Greek    α	β	γ	δ	ε	ζ	η	θ	ι	κ	λ	μ	ν	ξ	ο	π	ρ	ς	σ	τ	υ	φ	χ	ψ	ω
    
    static Map<Character, Integer> characterLengthMap = new HashMap<>();
    
    public final static List<String> SPECIAL_CODES = new ArrayList<>(List.of(
            "{EL}", // end of line
            "{NL}", // new line
            "{NLTB}", // new line + tab
            "{WPNL}", // wait for player input + new line
            "{WPNLTB}", // wait for player input + new line + tab
            "{CL}", // clear dialog box
            "{CLTB}", // clear dialog box + tab
            "{WPCL}", // wait for player input + clear text
            "{WPCLTB}", // wait for player input + clear text + tab
            "{0D}",
            "{03}",
            "{04}",
            "{13}","{14}","{15}","{16}","{17}"
            ));
    
    public static Dictionary getLatinDictionary() {
        Dictionary dictionary = new Dictionary();
        dictionary.loadDictionary("dictionaries/latin.txt");
        char[] charArray = FONT_CHARACTERS.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            int code = i + 0x40;
            dictionary.addEntry(code, c+"");
            //System.out.println(String.format("Latin %s\t%s", c+"", h2(code)));
        }
        return dictionary;
    }

    public static Dictionary getLatinSmallDictionary() {
        Dictionary dictionary = new Dictionary();
        dictionary.loadDictionary("dictionaries/latin.txt");
        char[] charArray = FONT_SMALL_CHARACTERS.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            int code = i + 0x40;
            dictionary.addEntry(code, c+"");
            //System.out.println(String.format("Latin Small %s\t%s", c+"", h2(code)));
        }
        return dictionary;
    }
    
    public static List<Patch> getFontPatches(){

        List<Patch> patches = new ArrayList<>();

        Patch p = new Patch(x("34000"), ResIO.getBinaryResource("data/font/font.data").getBytes());
        patches.add(p);

        /**
         * Change so that {0D} maps to the new 0
         */
        p = new Patch(0x30418, Utils.hexStringToByteArray("69 50"));
        patches.add(p);

        /**
         * Init/Reset Variables
         */
        byte[] bytes = Utils.hexStringToByteArray("22 00 0A C3");
        p = new Patch(x("300D9"), bytes);
        patches.add(p);
        p = new Patch(x("30A00"), ResIO.getBinaryResource("data/asm/init.bin").getBytes());
        patches.add(p);

        /**
         * Main Routine
         */
        bytes = Utils.hexStringToByteArray("22 A0 0A C3 60");
        p = new Patch(x("305F1"), bytes);
        patches.add(p);


        p = new Patch(x("30AA0"), ResIO.getBinaryResource("data/asm/main.bin").getBytes());
        patches.add(p);

        p = new Patch(x("30B00"), ResIO.getBinaryResource("data/asm/override.bin").getBytes());
        patches.add(p);

        p = new Patch(x("30BD0"), ResIO.getBinaryResource("data/asm/overlap.bin").getBytes());
        patches.add(p);

        p = new Patch(x("30C80"), ResIO.getBinaryResource("data/asm/overflow.bin").getBytes());
        patches.add(p);

        p = new Patch(x("30D00"), ResIO.getBinaryResource("data/asm/update-shift.bin").getBytes());
        patches.add(p);

        p = new Patch(x("30B80"), ResIO.getBinaryResource("data/asm/write-top-tile.bin").getBytes());
        patches.add(p);
        p = new Patch(x("30B90"), ResIO.getBinaryResource("data/asm/write-bot-tile.bin").getBytes());
        patches.add(p);
        p = new Patch(x("30BA0"), ResIO.getBinaryResource("data/asm/load-top-tile.bin").getBytes());
        patches.add(p);
        p = new Patch(x("30BB0"), ResIO.getBinaryResource("data/asm/load-bot-tile.bin").getBytes());
        patches.add(p);
        
        return patches;
    }
    
    public static int getCharacterLength(char c) {
        Integer integer = characterLengthMap.get(c);
        return integer!=null ? integer : 0;
    }
    
    public static int getStringLength(String s) {
        int length = 0;
        for (char c : s.toCharArray()) {
            length += Font.getCharacterLength(c);
            length++;
        }
        return length;
    }

    public static String stripStringSpecialCode(String s) {
        String res = "";
        boolean skip = false;
        for (char c : s.toCharArray()) {
            if (c == '{' || skip) {
                skip = true;
            } else if (c == '}'){
                skip = false;
                res += " ";
            } else {
                res += c;
            }
        }
        return res;
    }
    
    public static String[] splitString(String s, int lineLength) {
        List<String> list = new ArrayList<>();
        String[] split = s.split(" ");
        int spaceLength = getCharacterLength(' ');
        int segmentLength = 0;
        String segment = "";
        for (String s1 : split) {
            int wordLength = getStringLength(s1);
            if (segment.isEmpty()) {
                // first word of the segment doesn't get prefix with a space
                segment += s1;
                segmentLength += wordLength;
            }
            else {
                if (segmentLength+spaceLength+wordLength<lineLength) {
                    segment += " " + s1;
                    segmentLength += spaceLength+wordLength;
                } else {
                    list.add(segment);
                    segment = s1;
                    segmentLength = wordLength;
                }
            }
        }
        if (!segment.isEmpty())
            list.add(segment);
        return list.toArray(new String[0]);
    }

    public static void generateVWFFontData() {
        String file = "/images/vwf/font.png";
        BufferedImage image = null;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        //List<byte[]> tiles = new ArrayList<>();

        ColorGraphics colorGraphics = new ColorGraphics();
        colorGraphics.loadFromImageFile("images/vwf/palette.png", 1);

        char c = ' ';

        try {
            image = ImageIO.read(Objects.requireNonNull(Font.class.getResource(file)));
            int tile = 0;
            while (tile<image.getWidth()) {
                BufferedImage top = image.getSubimage(tile, 0, 8, 8);
                BufferedImage bot = image.getSubimage(tile, 8, 8, 8);

                int width = 0;
                // calculate tile width
                for (int x=0;x<top.getWidth();x++) {
                    for (int y=0;y<top.getHeight();y++) {
                        int rgb = top.getRGB(x, y);
                        if (rgb!=0 && (x+1)>width) width = x+1;
                    }
                }
                for (int x=0;x<bot.getWidth();x++) {
                    for (int y=0;y<bot.getHeight();y++) {
                        int rgb = bot.getRGB(x, y);
                        if (rgb!=0 && (x+1)>width) width = x+1;
                    }
                }
                if (width==8) width--;
                if (tile==0) width = 5;
                //System.out.println(String.format("'%s'\twidth=%d (%d)", c+"", width, tile));
                characterLengthMap.put(c, width);
                c = (char)(((int)c)+1);
                Tile topTile = new Tile();
                topTile.loadFromImage(top, colorGraphics, ColorDepth._2BPP);
                byte[] bytes = topTile.getBytes();
                bytes[0] = (byte) (width & 0xFF);
                byteArrayOutputStream.writeBytes(bytes);

                Tile botTile = new Tile();
                botTile.loadFromImage(bot, colorGraphics, ColorDepth._2BPP);
                bytes = botTile.getBytes();
                byteArrayOutputStream.writeBytes(bytes);

                tile += 8;
            }

            DataWriter.saveData("D:\\git\\dynami-tracer\\dynami-tracer-gen\\src\\main\\resources\\data\\font\\font.data", byteArrayOutputStream.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeSmallFontData(byte[] data) {
        Palette2bpp palette2bpp = new Palette2bpp("images/vwf/palette-small.png");
        byte[] bytes = ImageParser.loadImage2bpp(
                "images/vwf/small-font.png",
                palette2bpp
        );

        Bytes.writeBytes(bytes, data, 0x33000);
        
        
        for (int i = 0; i< FONT_SMALL_CHARACTERS.toCharArray().length; i++) {
            data[0x2F362+i] = (byte) ((0x80)+i);
        }
        
    }
    
}
