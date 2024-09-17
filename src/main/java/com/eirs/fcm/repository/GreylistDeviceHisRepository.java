package com.eirs.fcm.repository;

import com.eirs.fcm.repository.entity.GreylistDeviceHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.stream.Stream;


@Repository
public interface GreylistDeviceHisRepository extends JpaRepository<GreylistDeviceHis, Long> {


    @Query("select a from GreylistDeviceHis a where a.createdOn >= :startDate and a.createdOn < :endDate and upper(a.operatorName) = :operatorName")
    public Stream<GreylistDeviceHis> streamByOperatorNameAndCreatedOnBetween(String operatorName, LocalDateTime startDate, LocalDateTime endDate);


    @Query("select a from GreylistDeviceHis a where a.createdOn >= :startDate and a.createdOn < :endDate and a.operatorName is NULL")
    public Stream<GreylistDeviceHis> streamByCreatedOnBetween(LocalDateTime startDate, LocalDateTime endDate);
}
