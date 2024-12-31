package com.example.pbft.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Account {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long accountId;
    private String name;
    private int balance;
    
    public Account() {
		super();
	}

    public Account(Long accountId, String name, int balance) {
		super();
		this.accountId = accountId;
		this.name = name;
		this.balance = balance;
	}

	public String getName() {
        return name;
    }

    public int getBalance() {
        return balance;
    }

	public void setBalance(int balance) {
        this.balance = balance;
    }

	@Override
	public String toString() {
		return "Account [accountId=" + accountId + ", name=" + name + ", balance=" + balance + "]";
	}
    
}
