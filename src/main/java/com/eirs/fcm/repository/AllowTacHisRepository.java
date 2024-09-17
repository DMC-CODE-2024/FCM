package com.eirs.fcm.repository;

import com.eirs.fcm.repository.entity.AllowedTacHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.stream.Stream;


@Repository
public interface AllowTacHisRepository extends JpaRepository<AllowedTacHis, Long> {

    @Query("select a from AllowedTacHis a where a.createdOn >= :startDate and a.createdOn < :endDate")
    public Stream<AllowedTacHis> streamByCreatedOnBetween(LocalDateTime startDate, LocalDateTime endDate);
}
