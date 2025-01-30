package com.eirs.fcm.repository.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TacDataHis implements CsvData {

    private Long id;

    private String tac;

    private Integer operation;

    private LocalDateTime createdOn;

    @Override
    public String toCsv() {
        return operation + "," + tac;
    }
}
