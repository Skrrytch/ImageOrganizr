package eu.spex.iorg.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class I18n {

    public static ResourceBundle bundle;

    public static void init() {
        bundle = load(Locale.getDefault());
    }

    /**
     * English is the base bundle. For a language without its own bundle, {@link ResourceBundle#getBundle} falls back
     * to the default locale, so "--lang=en" on a German system would show German labels - the base bundle is loaded
     * directly instead. (A no-fallback {@code ResourceBundle.Control} is not supported in named modules.)
     */
    private static ResourceBundle load(Locale locale) {
        ResourceBundle found = ResourceBundle.getBundle("labels", locale);
        if (found.getLocale().getLanguage().equals(locale.getLanguage())) {
            return found;
        }
        try (InputStream in = I18n.class.getResourceAsStream("/labels.properties")) {
            return new PropertyResourceBundle(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException | NullPointerException ex) {
            Logger.error("Failed to load the base labels: " + ex.getMessage());
            return found;
        }
    }

    public static String translate(String key) {
        if (bundle == null) {
            init();
        }
        try {
            return bundle.getString(key);
        } catch (MissingResourceException ex) {
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
        } catch (MissingResourceException ex) {
            return key;
        }
    }

    public static void setLocale(String languageLocale) {
        try {
            Locale locale = Locale.of(languageLocale);
            bundle = load(locale);
        } catch (MissingResourceException ex) {
            Logger.error("Wrong language setting: Locale '" + languageLocale + "' invalid (" + ex.getMessage() + ")");
        }
    }
}
