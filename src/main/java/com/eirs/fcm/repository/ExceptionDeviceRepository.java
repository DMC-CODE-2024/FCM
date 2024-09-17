package com.eirs.fcm.repository;

import com.eirs.fcm.repository.entity.ExceptionDevice;
import com.eirs.fcm.repository.entity.ExceptionDeviceHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;


@Repository
public interface ExceptionDeviceRepository extends JpaRepository<ExceptionDevice, Long> {

    public Stream<ExceptionDevice> streamByOperatorNameIgnoreCase(String operatorName);

    @Query("SELECT R from ExceptionDevice R where R.operatorName is null")
    public List<ExceptionDevice> findByOperatorNameIsNull();
}
