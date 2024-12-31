package com.example.pbft.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.pbft.Models.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>{

}
