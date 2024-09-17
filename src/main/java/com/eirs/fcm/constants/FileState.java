package com.eirs.fcm.constants;

public enum FileState {
    INIT(0), COMPLETED(1);
    Integer index;

    FileState(Integer index) {
        this.index = index;
    }

    public Integer getIndex() {
        return this.index;
    }
}
