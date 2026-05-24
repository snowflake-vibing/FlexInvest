package doanjava;

import View.LoginForm;

import java.awt.Font;
import javax.swing.UIManager;

public class DoAnJava {
    public static void main(String[] args) {
        try {
            javax.swing.UIManager.put("TitlePane.useWindowDecorations", true);
            javax.swing.UIManager.put("Button.arc", 999);
            javax.swing.UIManager.put("Component.arc", 999);
            javax.swing.UIManager.put("ProgressBar.arc", 999);
            javax.swing.UIManager.put("TextComponent.arc", 999);
            
            java.awt.Color accentColor = new java.awt.Color(245, 158, 11);
            javax.swing.UIManager.put("Component.focusColor", accentColor);
            javax.swing.UIManager.put("Component.focusedBorderColor", accentColor);
            javax.swing.UIManager.put("Button.default.background", accentColor);
            javax.swing.UIManager.put("TabbedPane.selectedBackground", new java.awt.Color(238, 242, 255));
            javax.swing.UIManager.put("TabbedPane.underlineColor", accentColor);
            
            com.formdev.flatlaf.FlatLightLaf.setup();
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
        } catch (Exception ignored) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored2) {}
        }
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginForm();
        });
    }
}
