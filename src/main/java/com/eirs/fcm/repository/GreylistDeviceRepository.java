package com.eirs.fcm.repository;

import com.eirs.fcm.repository.entity.GreylistDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;


@Repository
public interface GreylistDeviceRepository extends JpaRepository<GreylistDevice, Long> {

    public Stream<GreylistDevice> streamByOperatorNameIgnoreCase(String operatorName);

    @Query("SELECT R from GreylistDevice R where R.operatorName is null")
    public List<GreylistDevice> findByOperatorNameIsNull();
}
