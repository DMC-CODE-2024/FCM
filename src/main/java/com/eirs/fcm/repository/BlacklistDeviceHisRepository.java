package com.eirs.fcm.repository;

import com.eirs.fcm.repository.entity.BlacklistDeviceHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.stream.Stream;


@Repository
public interface BlacklistDeviceHisRepository extends JpaRepository<BlacklistDeviceHis, Long> {

    @Query("select a from BlacklistDeviceHis a where a.createdOn >= :startDate and a.createdOn < :endDate and upper(a.operatorName) = :operatorName")
    public Stream<BlacklistDeviceHis> streamByOperatorNameAndCreatedOnBetween(String operatorName, LocalDateTime startDate, LocalDateTime endDate);


    @Query("select a from BlacklistDeviceHis a where a.createdOn >= :startDate and a.createdOn < :endDate and a.operatorName is NULL")
    public Stream<BlacklistDeviceHis> streamByCreatedOnBetween(LocalDateTime startDate, LocalDateTime endDate);
}
