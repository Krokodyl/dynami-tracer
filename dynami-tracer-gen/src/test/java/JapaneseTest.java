import org.junit.Before;
import org.junit.Test;
import resources.Hex;
import services.Dictionary;
import services.vwf.Font;

public class JapaneseTest {

    Dictionary japaneseDictionary = new Dictionary();
    Dictionary latinDictionary = new Dictionary();
    Dictionary latinSmallDictionary = new Dictionary();
    
    @Before
    public void setUp() {
        japaneseDictionary.loadDictionary("dictionaries/japanese.txt");
        latinDictionary = Font.getLatinDictionary();
        latinSmallDictionary = Font.getLatinSmallDictionary();
    }
    
    @Test
    public void encodeJapanese() {
        String item = "クライ・ベイビー";
        byte[] code = japaneseDictionary.getCode(item);
        System.out.println(Hex.getHexString(code));
    }

    @Test
    public void encodeEnglish() {
        String s = "xyz~♪";
        byte[] code = latinDictionary.getCode(s);
        System.out.println(Hex.getHexString(code));
    }

    @Test
    public void decodeSmallJapanese() {
        String s = "nwR]";
        byte[] code = latinSmallDictionary.getCode(s);
        System.out.println(Hex.getHexString(code));
    }
}
