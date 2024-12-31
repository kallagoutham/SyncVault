package com.example.pbft.Service;

import java.util.List;

import com.example.pbft.Models.Account;
import com.example.pbft.Models.CombinedLogs;
import com.example.pbft.Models.NewView;
import com.example.pbft.Models.PrePreapreRequest;
import com.example.pbft.Models.PrepareAndCommit;
import com.example.pbft.Models.Reply;
import com.example.pbft.Models.Transaction;
import com.example.pbft.Models.TransactionRequest;
import com.example.pbft.Models.ViewChange;

public interface TransactionService {

	List<Account> PrintDB();
	List<Transaction> PrintLog();
	boolean processTransaction(TransactionRequest transactionRequest);
	PrepareAndCommit processPrePrepareRequest(PrePreapreRequest ppq);
	List<PrePreapreRequest> getPrePrepareLog();
	PrepareAndCommit processPrepareRequest(PrepareAndCommit prepare);
	void processCommitRequest(PrepareAndCommit commit);
	List<PrepareAndCommit> getPrepareLog();
	List<PrepareAndCommit> getCommitLog();
	List<Reply> getExecuted();
	void processOptimisticCommitRequest(PrepareAndCommit prepare);
	void filter();
	void filterPrePrepare();
	void initiateViewChange(ViewChange viewChange);
	ViewChange ViewChangeAccept(ViewChange viewChange);
	void newView(NewView newView);
	List<ViewChange> viewChangeLogs();
	List<NewView> getNewViewLogs();
	String getStatus(int sequenceNumber);
	CombinedLogs getCombinedLogs();
	CombinedLogs getCombinedCheckPointLogs();

}
