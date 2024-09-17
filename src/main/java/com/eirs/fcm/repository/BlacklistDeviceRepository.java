package com.eirs.fcm.repository;

import com.eirs.fcm.repository.entity.BlacklistDevice;
import com.eirs.fcm.repository.entity.BlacklistDeviceHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;


@Repository
public interface BlacklistDeviceRepository extends JpaRepository<BlacklistDevice, Long> {

    public Stream<BlacklistDevice> streamByOperatorNameIgnoreCase(String operatorName);

    @Query("SELECT R from BlacklistDevice R where R.operatorName is null")
    public List<BlacklistDevice> findByOperatorNameIsNull();
}
