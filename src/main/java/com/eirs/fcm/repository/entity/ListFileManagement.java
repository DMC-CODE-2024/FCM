package com.eirs.fcm.repository.entity;

import com.eirs.fcm.constants.CopyStatus;
import com.eirs.fcm.constants.FileType;
import com.eirs.fcm.constants.ListType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "list_file_mgmt")
public class ListFileManagement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdOn;

    private LocalDateTime modifiedOn;

    private String fileName;

    private String filePath;

    private String sourceServer;

    private String destinationPath;

    private String destinationServer;

    @Enumerated(EnumType.STRING)
    private ListType listType;

    private String operatorName;

    private Integer fileType;

    private Integer fileState;

    private Long recordCount;

    private Integer copyStatus;
}
