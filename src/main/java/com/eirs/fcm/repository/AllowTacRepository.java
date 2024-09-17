package com.eirs.fcm.repository;

import com.eirs.fcm.repository.entity.AllowedTac;
import com.eirs.fcm.repository.entity.AllowedTacHis;
import com.eirs.fcm.repository.entity.BlockedTac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.stream.Stream;


@Repository
public interface AllowTacRepository extends JpaRepository<AllowedTac, Long> {

    Stream<AllowedTac> streamAllBy();
}
