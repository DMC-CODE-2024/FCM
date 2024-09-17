package com.eirs.fcm.repository.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "blocked_tac_list")
public class BlockedTac implements CsvData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tac;

    private LocalDateTime createdOn;

    @Override
    public String toCsv() {
        return tac;
    }
}
