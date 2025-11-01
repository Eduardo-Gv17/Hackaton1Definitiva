package org.example.hackaton1_.repository;

import org.example.hackaton1_.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface SalesRepository extends JpaRepository<Sale, String> {


    @Query("SELECT s FROM Sale s WHERE s.soldAt BETWEEN :from AND :to")
    List<Sale> findByDateRange(LocalDateTime from, LocalDateTime to);


    @Query("SELECT s FROM Sale s WHERE s.branch = :branch AND s.soldAt BETWEEN :from AND :to")
    List<Sale> findByBranchAndDateRange(String branch, LocalDateTime from, LocalDateTime to);
}
