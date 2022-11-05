import resources.ResIO;

public class DynamiTracer {
    
    public static void main(String[]args) {
        //generateJapaneseTable();
    }
    
    public static void generateJapaneseTable() {
        String inputCodes = "tmp/name-input.txt";
        String inputValues = "tmp/name-input-values.txt";
        ResIO resInputCodes = ResIO.getTextResource(inputCodes);
        ResIO resInputValues = ResIO.getTextResource(inputValues);
        while (resInputCodes.hasNext() && resInputValues.hasNext()) {
            String[] codes = resInputCodes.next().toString().split(" ");
            char[] values = resInputValues.next().toString().toCharArray();
            for (int i = 0; i < codes.length; i++) {
                String code = codes[i];
                String value = "" + values[i];
                System.out.println(code+"="+value);
            }
        }
    }
}
