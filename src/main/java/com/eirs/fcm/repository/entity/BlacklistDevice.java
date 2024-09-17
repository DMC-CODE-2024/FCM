package com.eirs.fcm.repository.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "black_list")
public class BlacklistDevice implements CsvData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
