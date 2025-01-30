package com.eirs.fcm.repository.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TacData implements CsvData {

    private Long id;

    private String tac;

    private LocalDateTime createdOn;

    @Override
    public String toCsv() {
        return tac;
    }
}
