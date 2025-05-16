package com.pbl3.supermarket.repository;

import com.pbl3.supermarket.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, String> {
    @Query(value = "SELECT * FROM receipt WHERE MONTH(bill_date) = :month AND YEAR(bill_date) = :year", nativeQuery = true)
    List<Receipt> findByMonth(@Param("month") int month, @Param("year") int year);
    @Query(value = "SELECT * FROM receipt WHERE DAY(bill_date) = :day AND MONTH(bill_date) = :month AND YEAR(bill_date) = :year", nativeQuery = true)
    List<Receipt> findByDay(@Param("day") int day, @Param("month") int month, @Param("year") int year);
}
