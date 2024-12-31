package com.example.pbft.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.pbft.Models.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>{
	
	Account findByName(String name);

	@Modifying
	@Transactional
	@Query("UPDATE Account a SET a.balance = ?1")
	void updateBalances(int balance);

}
