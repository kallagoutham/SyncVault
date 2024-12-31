package com.example.pbft.Models;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Transaction {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long transactionId;
    private String sender;
    private String receiver;
    private int amount;
    private long timestamp;
    

    public Transaction(String sender, String receiver, int amount) {
		super();
		this.sender = sender;
		this.receiver = receiver;
		this.amount = amount;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Transaction() {
		super();
	}

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public int getAmount() {
        return amount;
    }

	@Override
	public int hashCode() {
		return Objects.hash(amount, receiver, sender, timestamp);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		return amount == other.amount && Objects.equals(receiver, other.receiver)
				&& Objects.equals(sender, other.sender) && timestamp == other.timestamp;
	}

	@Override
	public String toString() {
	    return String.format(
	        "{\"sender\":\"%s\",\"receiver\":\"%s\",\"amount\":%d,\"timestamp\":%d}",
	        sender, receiver, amount, timestamp
	    );
	}
	
}

