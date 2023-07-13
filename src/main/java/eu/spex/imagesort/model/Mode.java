package eu.spex.imagesort.model;

import java.util.Arrays;
import java.util.Optional;

public enum Mode {

    SIMPLE_KNOCKOUT("simple-knockout", true),

    FULL_KNOCKOUT("full-knockout", true),

    ORDER("order", true),

    RATE("rate", false),

    CATEGORIZE("categorize", false);

    private final String parameter;

    private final boolean compareMode;

    Mode(String parameter, boolean compareMode) {
        this.parameter = parameter;
        this.compareMode = compareMode;
    }

    public String getParameter() {
        return parameter;
    }

    public boolean isCompareMode() {
        return compareMode;
    }

    public static Mode byParameter(String parameter) {
        Optional<Mode> foundMode = Arrays.stream(Mode.values()).filter(mode -> mode.getParameter().equalsIgnoreCase(parameter)).findFirst();
        return foundMode.orElse(null);
    }
}

