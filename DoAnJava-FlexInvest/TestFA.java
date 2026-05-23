import java.awt.*;
import java.io.File;
public class TestFA {
    public static void main(String[] args) throws Exception {
        Font fa = Font.createFont(Font.TRUETYPE_FONT, new File("src/Resources/fa-solid-900.ttf")).deriveFont(20f);
        System.out.println("Font loaded: " + fa.getName());
    }
}
