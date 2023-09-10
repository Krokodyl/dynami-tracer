import org.junit.Before;
import org.junit.Test;
import resources.Hex;
import resources.ResIO;
import services.Dictionary;
import services.vwf.Font;

import static resources.Hex.h2;

public class JapaneseTest {

    Dictionary japaneseDictionary = new Dictionary();
    Dictionary japaneseSmallDictionary = new Dictionary();
    Dictionary latinDictionary = new Dictionary();
    Dictionary latinSmallDictionary = new Dictionary();
    
    @Before
    public void setUp() {
        japaneseDictionary.loadDictionary("dictionaries/japanese.txt");
        japaneseSmallDictionary.loadDictionary("dictionaries/small-japanese.txt");
        latinDictionary = Font.getLatinDictionary();
        latinSmallDictionary = Font.getLatinSmallDictionary();
    }

    @Test
    public void encodeJapanese() {
        String item = "タロキチ";
        byte[] code = japaneseDictionary.getCode(item);
        System.out.println(Hex.getHexString(code));
    }

    @Test
    public void encodeSmallJapanese() {
        String item = "トレジャー";
        byte[] code = japaneseSmallDictionary.getCode(item);
        System.out.println(Hex.getHexString(code));
    }

    @Test
    public void encodeEnglish() {
        String s = "QPxp";
        byte[] code = latinDictionary.getCode(s);
        System.out.println(Hex.getHexString(code));
    }

    @Test
    public void decodeSmallJapanese() {
        String s = "nwR]";
        byte[] code = latinSmallDictionary.getCode(s);
        System.out.println(Hex.getHexString(code));
    }

    @Test
    public void decodeJapanese() {
        byte[] data = Hex.parseHex("0F 27 02 01 AB AD A9 AF B1 F8 0C 01 02 AA F8 D8 F8 00 CD 08 01 01 DE F8 5A 00 00 F6 02 03 00 5E B4 AE DA 00 FD 01 04 03 C6 AC DA 66 00 64 00 05 04 80 F8 69 F8 00 00 00 06 05 56 D0 00 00 00 08 E2 20 A9 00 48 20 46 2F 20 1F 30 C9 00 F0 0A 68 1A C9 08 90 EF A9 FF 80 01 68 E0 0B 00 00 56 D0");
        String text = japaneseDictionary.getValue(data);
        System.out.println(text);
    }

    @Test
    public void decodeJapanese2() {
        ResIO io = ResIO.getTextResource("gen/quiz.txt");
        while (io.hasNext()) {
            String s = io.next().toString();
            
            if (s.indexOf("C8 0C")>0) {
                String id;
                if (s.indexOf("16 C2")<0)
                    id = s.substring(s.indexOf("18 C2") + 5, s.indexOf("C8 0C"));
                else id = s.substring(s.indexOf("16 C2") + 5, s.indexOf("C8 0C"));

                int a = s.indexOf("0C 28");
                int b = s.indexOf("0A", a);
                if (b<0) b = s.indexOf("33", a);
                if (a > 0 && b > 0) {
                    String code = s.substring(a + 6, b).trim();
                    System.out.println(String.format("%s\t\t%s\t%s", id, code, Font.stripStringSpecialCode(japaneseDictionary.getValue(Hex.parseHex(code)))));
                }
            }
        }
        /*
        for (String s:new String[]{
                "BC 64 E6 C8",
                "92 4C A6 AE DA 52 98 D0",
                "C6 9E B2 46",
                "98 F8 42 AE",
                "BC 66 D8 F8 B6",
                "66 DA 4A C8",
                "4C DA 4A D2 CC 96 E0",
                "B5 8F",
                "90 DE"
        }) {
            byte[] data = Hex.parseHex(s);
            String text = japaneseDictionary.getValue(data);
            System.out.println(text);
        }*/
        
    }

    
}
