package eu.comsode.unifiedviews.plugins.marc;

public enum Encoding {
    UTF8("UTF-8"), UTF16("UTF-16"), ISO_8859_1("ISO-8859-1"), WINDOWS1250("windows-1250");

    private String charset;

    Encoding(String charset) {
        this.charset = charset;
    }

    public String getCharset() {
        return charset;
    }
}
