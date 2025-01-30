package com.eirs.fcm.repository.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ListDeviceData implements CsvData {

    private Long id;

    private String actualImei;

    private String imei;

    private String imsi;

    private String msisdn;

    private String operatorId;

    private String operatorName;

    private LocalDateTime createdOn;

    @Override
    public String toCsv() {
        return (actualImei == null ? "" : actualImei) + "," + (imsi == null ? "" : imsi) + "," + (msisdn == null ? "" : msisdn);
    }

}
