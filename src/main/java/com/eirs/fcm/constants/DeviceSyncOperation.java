package com.eirs.fcm.constants;

public enum DeviceSyncOperation {
    DELETE, ADD;

    public static DeviceSyncOperation get(Integer index) {
        return DeviceSyncOperation.values()[index];
    }

    public static String getForMacra(Integer index) {
        DeviceSyncOperation deviceSyncOperation = DeviceSyncOperation.values()[index];
        return deviceSyncOperation == DELETE ? "R" : "I";
    }

}
