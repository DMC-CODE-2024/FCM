package com.eirs.fcm.repository.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ListDeviceDataHis implements CsvData {

    private Long id;

    private Integer operation;

    private String actualImei;

    private String imei;

    private String imsi;

    private String msisdn;

    private String operatorId;

    private String operatorName;

    private LocalDateTime createdOn;

    @Override
    public String toCsv() {
        return operation + "," + (actualImei == null ? "" : actualImei) + "," + (imsi == null ? "" : imsi) + "," + (msisdn == null ? "" : msisdn);
    }

}
