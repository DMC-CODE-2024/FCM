package com.eirs.fcm.repository.entity;

import com.eirs.fcm.constants.ListType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "file_creation_pointer")
public class FileCreationPointer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private LocalDateTime createdTill;

}
