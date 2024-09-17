package com.eirs.fcm.repository;

import com.eirs.fcm.constants.ListType;
import com.eirs.fcm.repository.entity.FileCreationPointer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface FileCreationPointerRepository extends JpaRepository<FileCreationPointer, Long> {

    Optional<FileCreationPointer> findByType(String type);
}
