package services;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static byte b(String s) {
        return (byte) Integer.parseInt(s,16);
    }
    
    public static int x(String s) {
        return Integer.parseInt(s,16);
    }

    public static String h(int i) {
        return padLeft(Integer.toHexString(i).toUpperCase(), '0',5);
    }

    public static String h(long i) {
        return padLeft(Long.toHexString(i).toUpperCase(), '0',5);
    }

    public static String h4(int i) {
        return padLeft(Integer.toHexString(i).toUpperCase(), '0',4);
    }

    public static String h2(int i) {
        return padLeft(Integer.toHexString(i).toUpperCase(), '0',2);
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = HEX_ARRAY[v >>> 4];
            hexChars[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        return hexStringToByteArray(s.split(" "));
    }

    public static byte[] hexStringToByteArray(String[] s) {
        byte[] bytes = new byte[s.length];
        for (int i = 0; i < s.length; i++) {
            bytes[i] = (byte) (Integer.parseInt(s[i],16) & 0xFF);
        }
        return bytes;
    }

    public static void writePointer(byte[] data, int pointerOffset, int value) {
        data[pointerOffset] = (byte) (((value) % 256) & 0xFF);
        data[pointerOffset + 1] = (byte) ((value) / 256);
        data[pointerOffset] = (byte) (((value) % 256) & 0xFF);
        data[pointerOffset + 1] = (byte) ((value) / 256);
    }
    
    public static String padRight(String s,char c,int length) {
        while (s.length()<length) {
            s+=c;
        }
        return s;
    }
    public static String padLeft(String s,char c,int length) {
        while (s.length()<length) {
            s=c+s;
        }
        return s;
    }

    public static String concat(String[] array) {
        String res = "";
        for (String s:array) res+=s+" ";
        return res.trim();
    }

    public static BufferedImage loadImage(String filename) {
        BufferedImage image = null;
        try {
            InputStream inputStream = Utils.class.getClassLoader().getResourceAsStream(filename);
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static BufferedImage loadSubImage(String filename, int x,int y, int width, int height) {
        BufferedImage image = null;
        try {
            InputStream inputStream = Utils.class.getClassLoader().getResourceAsStream(filename);
            if (inputStream==null)
                System.err.println(filename);
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image.getSubimage(x, y, width, height);
    }

    /**
     * Concatenate two images on top of each other
     */
    public static BufferedImage concatImages(BufferedImage img1,
                                             BufferedImage img2) {
        //int offset = 2;
        int width = img1.getWidth();
        int height = img1.getHeight() + img2.getHeight();
        BufferedImage newImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();
        Color oldColor = g2.getColor();
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        g2.setColor(oldColor);
        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, 0, img1.getHeight());
        g2.dispose();
        return newImage;
    }

    /**
     * Concatenate two images next to each other
     */
    public static BufferedImage concatImagesSide(BufferedImage img1,
                                             BufferedImage img2) {
        //int offset = 2;
        int width = img1.getWidth() + img2.getWidth();
        int height = img1.getHeight();
        BufferedImage newImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();
        Color oldColor = g2.getColor();
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        g2.setColor(oldColor);
        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, img1.getWidth(), 0);
        g2.dispose();
        return newImage;
    }

    public static BufferedImage createRedImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D    graphics = image.createGraphics();

        graphics.setColor(Color.red);
        graphics.fillRect (0,0,image.getWidth(),image.getHeight());
        graphics.dispose();
        return image;
    }

    public static List<String> splitInPairs(String s) {
        List<String> res = new ArrayList<>();
        int k = 0;
        String segment = "";
        while (!s.isEmpty()) {
            char c = s.charAt(0);
            if (c !='{') {
                segment += c;
            } else {
                if (!segment.isEmpty()) {
                    segment += " ";
                    res.add(segment);
                    segment = "";
                }
                while (s.charAt(0)!='}') {
                    segment += s.charAt(0);
                    s = s.substring(1);
                }
                segment += s.charAt(0);
            }
            if (segment.length()>=2) {
                res.add(segment);
                segment = "";
            }
            if (!s.isEmpty()) s = s.substring(1);
        }
        return res;
    }

    public static String getColorAsHex(int rgb) {
        int blue = rgb & 0xff;
        int green = (rgb & 0xff00) >> 8;
        int red = (rgb & 0xff0000) >> 16;
        return toHexString(red,2)+toHexString(green,2)+toHexString(blue,2);
    }

    public static String toHexString(int value, int padding) {
        return String.format("%0"+padding+"x",value).toUpperCase();
    }

    public static String toHexString(byte value) {
        return toHexString(((int)value) & 0xFF, 2);
    }

    public static byte[] codeBytes(String code) {
        byte[] data = new byte[2];
        int a = Integer.parseInt(code.substring(0, 2), 16);
        int b = Integer.parseInt(code.substring(2, 4), 16);
        data[0] = (byte) a;
        data[1] = (byte) b;
        return data;
    }
    
    public static long checksum(byte[] data) {
        long sum = 0;
        int offset = 0;
        int test = 0;
        for (byte b : data) {
            if (offset>=0xFFB0 && offset<=(0xFFDB)) {
                test += b & 0xFF;
            }
            if (offset>=0xFFB0 && offset<(0xFFB0+0x30)) {
                
            }
            else {
                sum += b & 0xFF;
            }
            offset++;
        }
        sum &= 0xFFFF;
        System.out.println("test="+h(test));
        return sum;
    }

    public static int lastIndexOf(byte[] array, byte[] target) {
        if (target.length == 0) {
            return array.length - 1;
        } else if (target.length > array.length) {
            return -1;
        }

        int lastIndexOf = -1;
        boolean differentValue;

        for (int i = 0; i <= array.length - target.length; i++) {
            differentValue = false;
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    differentValue = true;
                    break;
                }
            }
            if (!differentValue) {
                lastIndexOf = i;
            }
        }

        return lastIndexOf;
    }

    public static void mergeImages(String folder, int columns, String output) {
        try {
            List<File> files = Files.list(Paths.get(folder)).map(Path::toFile).collect(Collectors.toList());
            BufferedImage image = ImageIO.read(files.get(0));
            int rows = files.size() / columns;
            if (files.size()%columns>0) rows++;
            BufferedImage out = new BufferedImage(columns*image.getWidth(), rows*image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            int x = 0;
            int y = 0;
            for (File file : files) {
                if (x==out.getWidth()) {
                    x = 0;
                    y = y + image.getHeight();
                }
                BufferedImage read = ImageIO.read(file);
                addImage(out, read, 1, x, y);
                x = x + image.getWidth();
            }
            ImageIO.write(out, "png", new File(output));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * prints the contents of buff2 on buff1 with the given opaque value.
     */
    public static void addImage(BufferedImage buff1, BufferedImage buff2,
                                float opaque, int x, int y) {
        Graphics2D g2d = buff1.createGraphics();
        g2d.setComposite(
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opaque));
        g2d.drawImage(buff2, x, y, null);
        g2d.dispose();
    }
}
