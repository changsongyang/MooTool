package com.luoboduner.moo.tool.util.translator;

public class TranslatorFactory {
    public enum TranslatorType {
        GOOGLE,
        BING,
        MICROSOFT
    }

    public static Translator getTranslator(TranslatorType type) {
        switch (type) {
            case GOOGLE:
                return new GoogleTranslatorUtil();
            case BING:
                return new BingTranslatorUtil();
            case MICROSOFT:
//                return new MicrosoftTranslatorUtil();
            default:
                throw new IllegalArgumentException("Unknown translator type: " + type);
        }
    }
}
