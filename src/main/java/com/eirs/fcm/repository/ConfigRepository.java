package com.eirs.fcm.repository;

import com.eirs.fcm.repository.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigRepository extends JpaRepository<SystemConfig, Long> {

    public Optional<SystemConfig> findByConfigKey(String configKey);
}
