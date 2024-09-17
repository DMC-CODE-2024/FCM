package com.eirs.fcm.repository;

import com.eirs.fcm.repository.entity.BlockedTacHis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.stream.Stream;


@Repository
public interface BlockTacHisRepository extends JpaRepository<BlockedTacHis, Long> {

    @Query("select a from BlockedTacHis a where a.createdOn >= :startDate and a.createdOn < :endDate")
    public Stream<BlockedTacHis> streamByCreatedOnBetween(LocalDateTime startDate, LocalDateTime endDate);
}
