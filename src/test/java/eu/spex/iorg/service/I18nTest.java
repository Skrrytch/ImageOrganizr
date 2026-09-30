package eu.spex.iorg.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class I18nTest {

    private final Locale defaultLocale = Locale.getDefault();

    @AfterEach
    void restoreDefaultLocale() {
        Locale.setDefault(defaultLocale);
        I18n.init();
    }

    @Test
    void englishIsShownOnAGermanSystem() {
        Locale.setDefault(Locale.GERMANY);
        I18n.setLocale("en");
        assertEquals("Rename", I18n.translate("button.rename"));
    }

    @Test
    void germanIsShownOnRequest() {
        Locale.setDefault(Locale.US);
        I18n.setLocale("de");
        assertEquals("Umbenennen", I18n.translate("button.rename"));
    }

    @Test
    void everyLanguageHasTheSameKeys() throws IOException {
        Set<String> english = keys("/labels.properties");
        Set<String> german = keys("/labels_de.properties");
        assertEquals(new TreeSet<>(english), new TreeSet<>(german));
    }

    private static Set<String> keys(String resource) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = I18nTest.class.getResourceAsStream(resource)) {
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return properties.stringPropertyNames();
    }

    @Test
    void unknownLanguageFallsBackToEnglish() {
        Locale.setDefault(Locale.FRANCE);
        I18n.init();
        assertEquals("Rename", I18n.translate("button.rename"));
    }
}
