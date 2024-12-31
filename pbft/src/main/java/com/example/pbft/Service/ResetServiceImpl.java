package com.example.pbft.Service;

import org.springframework.stereotype.Service;

import com.example.pbft.GlobalVariables.Variables;
import com.example.pbft.Repository.AccountRepository;

@Service
public class ResetServiceImpl implements ResetService {

	private final AccountRepository accountRepository;
	private final Variables variables;
	private final PerformanceService performanceService;

	public ResetServiceImpl(AccountRepository accountRepository, Variables variables,
			PerformanceService performanceService) {
		super();
		this.accountRepository = accountRepository;
		this.variables = variables;
		this.performanceService = performanceService;
	}

	@Override
	public void reset() {
		performanceService.logTaskStart();
		variables.getByzantineServers().clear();
		variables.getDisconnectedServers().clear();
		variables.setView(1);
		variables.getCommitted().clear();
		variables.getPreprepare().clear();
		variables.getPrepare().clear();
		variables.getExecuted().clear();
		variables.setCounter(1);
		variables.setLastExecutedN(0);
		variables.setViewChangeInProgress(false);
		variables.getNewViews().clear();
		variables.getViewChange().clear();
		variables.setCheckpoint(0);
		
		variables.getChcommitted().clear();
		variables.getChpreprepare().clear();
		variables.getChprepare().clear();
		variables.getChexecuted().clear();
		variables.getChviewChange().clear();
		variables.getChnewViews().clear();
		accountRepository.updateBalances(10);
		performanceService.logTaskEnd(1);	

	}

}
