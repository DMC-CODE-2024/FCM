package com.eirs.fcm.repository;

import com.eirs.fcm.repository.entity.BlockedTac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.stream.Stream;


@Repository
public interface BlockTacRepository extends JpaRepository<BlockedTac, Long> {

    Stream<BlockedTac> streamAllBy();
}
