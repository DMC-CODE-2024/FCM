package com.eirs.fcm.repository;

import com.eirs.fcm.repository.entity.ExceptionDeviceHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.stream.Stream;


@Repository
public interface ExceptionDeviceHisRepository extends JpaRepository<ExceptionDeviceHis, Long> {


    @Query("select a from ExceptionDeviceHis a where a.createdOn >= :startDate and a.createdOn < :endDate and upper(a.operatorName) = :operatorName")
    public Stream<ExceptionDeviceHis> streamByOperatorNameAndCreatedOnBetween(String operatorName, LocalDateTime startDate, LocalDateTime endDate);


    @Query("select a from ExceptionDeviceHis a where a.createdOn >= :startDate and a.createdOn < :endDate and a.operatorName is NULL")
    public Stream<ExceptionDeviceHis> streamByCreatedOnBetween(LocalDateTime startDate, LocalDateTime endDate);
}
