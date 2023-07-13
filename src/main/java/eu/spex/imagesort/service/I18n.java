package eu.spex.imagesort.service;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {

    public static ResourceBundle bundle;

    static {
        Locale currentLocale = new Locale("de");

        // Lade das Ressourcenbündel für die aktuelle Sprache
        bundle = ResourceBundle.getBundle("labels", currentLocale);
    }

    public static String translate(String key) {
        try {
            return bundle.getString(key);
        } catch(Exception ex) {
            return key;
        }
    }
}
