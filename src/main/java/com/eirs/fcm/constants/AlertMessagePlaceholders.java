package com.eirs.fcm.constants;

public enum AlertMessagePlaceholders {
    FILE_TYPE("<param_filetype>"),
    URL("<param_url>"), LIST("<param_list>"), OPERATOR("<param_operator>"),
    EXCEPTION("param_exception"),

    CONFIG_KEY("param_key"), CONFIG_VALUE("param_value"),

    FEATURE_NAME("param_feature"), LANGUAGE("param_language"), SMS("param_sms");
    String placeholder;

    AlertMessagePlaceholders(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getPlaceholder() {
        return this.placeholder;
    }
}
