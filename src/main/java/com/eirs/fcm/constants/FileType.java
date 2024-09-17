package com.eirs.fcm.constants;

public enum FileType {
    DAILY_FULL(0, "daily"), DAILY_INCREMENTAL(1, "daily"), WEEKLY_FULL(2, "weekly"), WEEKLY_INCREMENTAL(3, "weekly");
    Integer index;
    private String value;

    FileType(Integer index, String value) {
        this.index = index;
        this.value = value;
    }

    public Integer getIndex() {
        return this.index;
    }

    public String getValue() {
        return value;
    }

    public static FileType getByIndex(Integer index) {
        for (FileType fileType : FileType.values()) {
            if (fileType.getIndex() == index) {
                return fileType;
            }
        }
        return null;
    }
}
