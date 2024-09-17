package com.eirs.fcm.repository.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sys_param")
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "tag")
    public String configKey;

    @Column(name = "value")
    public String configValue;

    @Column(name = "feature_name")
    public String module;

}
