package com.eirs.fcm.repository;

import com.eirs.fcm.repository.entity.ListFileManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ListFileManagementRepository extends JpaRepository<ListFileManagement, Long> {

    List<ListFileManagement> findByCopyStatus(Integer copyStatus);

}
