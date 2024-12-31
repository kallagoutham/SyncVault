package com.example.pbft.Controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbft.Models.Account;
import com.example.pbft.Models.Transaction;
import com.example.pbft.Models.TransactionRequest;
import com.example.pbft.Service.PerformanceService;
import com.example.pbft.Service.TransactionService;


@RestController
public class TransactionController {
	
	private final TransactionService transactionService;
	private final PerformanceService performanceService;

	public TransactionController(TransactionService transactionService, PerformanceService performanceService) {
		super();
		this.transactionService = transactionService;
		this.performanceService = performanceService;
	}

	@GetMapping("/bank/datastore")
	public List<Account> currentDataStore(){
		return transactionService.PrintDB();
	}
	
	@GetMapping("/bank/local/log")
	public List<Transaction> getLocalLog(){
		return transactionService.PrintLog();
	}
	
	@PostMapping("/bank/transaction")
	public ResponseEntity<String> processTransaction(@RequestBody TransactionRequest transactionRequest) {
		if(transactionService.processTransaction(transactionRequest)) {
			return ResponseEntity.status(200).body("success");
		}
		return ResponseEntity.status(408).body("Request Timed out");
	}
	
	@GetMapping("/status/{sequenceNumber}")
    public String getServerStatus(@PathVariable int sequenceNumber) {
        return transactionService.getStatus(sequenceNumber);
    }
	
	@GetMapping("/bank/performance")
	public List<String> printPerformanceMetrics() {
		return performanceService.printPerformance();
	}
	
	@PostMapping("/f/pp")
	public void filter() {
		transactionService.filterPrePrepare();
	}
	
}
