package com.example.pbft.BootStrap;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.pbft.Models.Account;
import com.example.pbft.Repository.AccountRepository;
import com.example.pbft.Service.ResetService;
import com.example.pbft.Service.SignatureService;

@Component
public class BootStrapData implements CommandLineRunner {

	private final ResetService resetService;
	private final SignatureService signatureService;
	private final AccountRepository accountRepository;

	public BootStrapData(ResetService resetService, SignatureService signatureService,
			AccountRepository accountRepository) {
		super();
		this.resetService = resetService;
		this.signatureService = signatureService;
		this.accountRepository = accountRepository;
	}

	@Override
	public void run(String... args) throws Exception {
		resetService.reset();
		Account account1 = new Account(1L, "A", 10);
		Account account2 = new Account(2L, "B", 10);
		Account account3 = new Account(3L, "C", 10);
		Account account4 = new Account(4L, "D", 10);
		Account account5 = new Account(5L, "E", 10);
		Account account6 = new Account(6L, "F", 10);
		Account account7 = new Account(7L, "G", 10);
		Account account8 = new Account(8L, "H", 10);
		Account account9 = new Account(9L, "I", 10);
		Account account10 = new Account(10L, "J", 10);
		List<Account> accounts = Arrays.asList(account1, account2, account3, account4, account5, account6, account7,
				account8, account9, account10);
		accountRepository.saveAll(accounts);
		signatureService.generateKeyPair();
	}

}
