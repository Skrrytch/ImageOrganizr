package eu.spex.iorg.service;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {

    public static ResourceBundle bundle;

    public static void init() {
        Locale locale = Locale.getDefault();
        bundle = ResourceBundle.getBundle("labels", locale);
    }

    public static String translate(String key) {
        if (bundle == null) {
            init();
        }
        try {
            return bundle.getString(key);
        } catch (Exception ex) {
            return key;
        }
    }

    public static String translate(String key, Object... params) {
        if (bundle == null) {
            init();
        }
        try {
            String translated = bundle.getString(key);
            return MessageFormat.format(translated, params);
        } catch (Exception ex) {
            return key;
        }
    }

    public static void setLocale(String languageLocale) {
        try {
            Locale locale = new Locale(languageLocale);
            bundle = ResourceBundle.getBundle("labels", locale);
        } catch (Exception ex) {
            Logger.error("Wrong language setting: Locale '" + languageLocale + "' invalid (" + ex.getMessage() + ")");
        }
    }
}
