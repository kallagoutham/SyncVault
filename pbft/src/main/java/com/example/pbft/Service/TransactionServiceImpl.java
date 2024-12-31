package com.example.pbft.Service;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.pbft.GlobalVariables.Variables;
import com.example.pbft.Models.Account;
import com.example.pbft.Models.CombinedLogs;
import com.example.pbft.Models.NewView;
import com.example.pbft.Models.PrePreapreRequest;
import com.example.pbft.Models.PrepareAndCommit;
import com.example.pbft.Models.Reply;
import com.example.pbft.Models.Transaction;
import com.example.pbft.Models.TransactionRequest;
import com.example.pbft.Models.ViewChange;
import com.example.pbft.Repository.AccountRepository;
import com.example.pbft.Utils.DigitalSignatureUtil;
import com.example.pbft.Utils.HashUtils;
import com.example.pbft.Utils.PeerUtils;
import com.example.pbft.Utils.ThresholdSignatureUtil;

@Service
public class TransactionServiceImpl implements TransactionService {

	private final Variables variables;
	private final HashUtils hashUtils;
	private final DigitalSignatureUtil digitalSignatureUtil;
	private final PeerUtils peerUtils;
	private final RestTemplate restTemplate;
	private final ThresholdSignatureUtil thresholdSignatureUtil;
	private final AccountRepository accountRepository;
	private final PerformanceService performanceService;
	Set<Integer> localExecutedTransactions = new HashSet<>();
	int timeoutInSeconds = 2;

	public TransactionServiceImpl(Variables variables, HashUtils hashUtils,
			DigitalSignatureUtil digitalSignatureUtil, PeerUtils peerUtils, RestTemplate restTemplate,
			ThresholdSignatureUtil thresholdSignatureUtil, AccountRepository accountRepository,PerformanceService performanceService) {
		super();
		this.variables = variables;
		this.hashUtils = hashUtils;
		this.digitalSignatureUtil = digitalSignatureUtil;
		this.peerUtils = peerUtils;
		this.restTemplate = restTemplate;
		this.thresholdSignatureUtil = thresholdSignatureUtil;
		this.accountRepository = accountRepository;
		this.performanceService = performanceService;
	}

	@Override
	public List<Account> PrintDB() {
		performanceService.logTaskStart();
		performanceService.logTaskEnd(1);
		return accountRepository.findAll();
	}

	@Override
	public List<Transaction> PrintLog() {
		// Implement local log functions here.
		return null;
	}

	@Override
	public boolean processTransaction(TransactionRequest transactionRequest) {
		if (verifyRequestSignature(transactionRequest)) {
			PrePreapreRequest ppr = generatePrePrepareMessage(transactionRequest);
			List<PrepareAndCommit> replies = PrePreparePhase(ppr);
			variables.getPreprepare().add(ppr);
			PrepareAndCommit prepare = generatePrepareMessage(ppr);
			if (replies.size() + 1 == 3 * variables.getFaultsTolerated() + 1) {
				variables.getPrepare().add(prepare);
				if (!peerUtils.isByzantineServer()) {
					optimisticCommitPhase(prepare, replies);
					return true;
				} else {
					if (!variables.isViewChangeInProgress()) {
						variables.setViewChangeInProgress(true);
						viewChange();
						return false;
					}
				}
			} else if (replies.size() + 1 >= 2 * variables.getFaultsTolerated() + 1) {
				variables.getPrepare().add(prepare);
				if (!peerUtils.isByzantineServer()) {
					replies = PreparePhase(prepare, replies);
					if (replies.size() + 1 >= 2 * variables.getFaultsTolerated() + 1) {
						PrepareAndCommit commit = generateCommitMessage(prepare);
						CommitPhase(commit, replies);
						return true;
					}
				} else {
					if (!variables.isViewChangeInProgress()) {
						variables.setViewChangeInProgress(true);
						viewChange();
						return false;
					}
				}
			} else {
				filter();
				if (!variables.isViewChangeInProgress()) {
					variables.setViewChangeInProgress(true);
					viewChange();
				}
				return true;
			}
		}
		return false;
	}

	private void CommitPhase(PrepareAndCommit commit, List<PrepareAndCommit> replies) {
		performanceService.logTaskStart();
		boolean isAuthorized = thresholdSignatureUtil.verifyThresholdSignatures(replies,
				2 * variables.getFaultsTolerated());
		if (isAuthorized) {
			@SuppressWarnings("unused")
			List<CompletableFuture<PrepareAndCommit>> futures = peerUtils.getAllServersList().stream()
					.map(url -> CompletableFuture.supplyAsync(() -> {
						try {
							return restTemplate.postForObject("http://" + url + "/api/commit", commit,
									PrepareAndCommit.class);
						} catch (Exception e) {
							return null;
						}
					}).orTimeout(timeoutInSeconds, TimeUnit.SECONDS)).toList();
		}
		performanceService.logTaskEnd(10);
		return;
	}

	private List<PrepareAndCommit> PreparePhase(PrepareAndCommit prepare, List<PrepareAndCommit> replies) {
		performanceService.logTaskStart();
		boolean isAuthorized = thresholdSignatureUtil.verifyThresholdSignatures(replies,
				2 * variables.getFaultsTolerated());
		if (isAuthorized) {
			List<CompletableFuture<PrepareAndCommit>> futures = peerUtils.getPeersList().stream()
					.map(url -> CompletableFuture.supplyAsync(() -> {
						try {
							return restTemplate.postForObject("http://" + url + "/api/prepare", prepare,
									PrepareAndCommit.class);
						} catch (Exception e) {
							return null;
						}
					}).orTimeout(timeoutInSeconds, TimeUnit.SECONDS)).toList();
			performanceService.logTaskEnd(10);
			return futures.stream().map(future -> {
				try {
					return future.get();
				} catch (InterruptedException | ExecutionException e) {
					return null;
				}
			}).filter(result -> result != null).filter(result -> {
				try {
					return digitalSignatureUtil.verifySignature(result.getDigest(), result.getSignature(),
							variables.getPublicKeys().get(result.getI()));
				} catch (Exception e) {
					return false;
				}
			}).collect(Collectors.toList());
		}
		performanceService.logTaskEnd(10);
		return new ArrayList<>();
	}

	@Override
	public void processOptimisticCommitRequest(PrepareAndCommit prepare) {
		performanceService.logTaskStart();
		if (peerUtils.isByzantineServer()) {
			return;
		}
		prepare.setI(peerUtils.getServerPort());
		PrepareAndCommit commit = generateCommitMessage(prepare);
		if (!variables.getPrepare().contains(prepare)) {
			variables.getPrepare().add(prepare);
			variables.getCommitted().add(commit);
		}
		executeTransactionsUntil(commit);
		performanceService.logTaskEnd(1);
		return;
	}

	private void optimisticCommitPhase(PrepareAndCommit prepare, List<PrepareAndCommit> replies) {
		performanceService.logTaskStart();
		boolean isAuthorized = thresholdSignatureUtil.verifyThresholdSignatures(replies,
				2 * variables.getFaultsTolerated());
		if (isAuthorized) {
			@SuppressWarnings("unused")
			List<CompletableFuture<PrepareAndCommit>> futures = peerUtils.getAllServersList().stream()
					.map(url -> CompletableFuture.supplyAsync(() -> {
						try {
							return restTemplate.postForObject("http://" + url + "/api/optimisticcommit", prepare,
									PrepareAndCommit.class);
						} catch (Exception e) {
							return null;
						}
					}).orTimeout(timeoutInSeconds, TimeUnit.SECONDS)).toList();
		}
		performanceService.logTaskEnd(10);
		return;

	}

	public List<PrepareAndCommit> PrePreparePhase(PrePreapreRequest ppr) {
		performanceService.logTaskStart();
		List<CompletableFuture<PrepareAndCommit>> futures = peerUtils.getPeersList().stream()
				.map(url -> CompletableFuture.supplyAsync(() -> {
					try {
						return restTemplate.postForObject("http://" + url + "/api/preprepare", ppr,
								PrepareAndCommit.class);
					} catch (Exception e) {
						return null;
					}
				}).orTimeout(timeoutInSeconds, TimeUnit.SECONDS)).toList();
		performanceService.logTaskEnd(10);
		return futures.stream().map(future -> {
			try {
				return future.get();
			} catch (InterruptedException | ExecutionException e) {
				return null;
			}
		}).filter(result -> result != null).filter(result -> {
			try {
				return digitalSignatureUtil.verifySignature(result.getDigest(), result.getSignature(),
						variables.getPublicKeys().get(result.getI()));
			} catch (Exception e) {
				return false;
			}
		}).collect(Collectors.toList());
	}

	@Override
	public void processCommitRequest(PrepareAndCommit commit) {
		performanceService.logTaskStart();
		if (peerUtils.isByzantineServer()) {
			return;
		}
		commit.setI(peerUtils.getServerPort());
		if (!variables.getCommitted().contains(commit)) {
			variables.getCommitted().add(commit);
		}
		executeTransactionsUntil(commit);
		performanceService.logTaskEnd(2);
		return;
	}

	@Override
	public PrepareAndCommit processPrepareRequest(PrepareAndCommit prepare) {
		performanceService.logTaskStart();
		if (peerUtils.isByzantineServer()) {
			return null;
		}
		prepare.setI(peerUtils.getServerPort());
		if (!variables.getPrepare().contains(prepare)) {
			variables.getPrepare().add(prepare);
		}
		PrepareAndCommit commit = generateCommitMessage(prepare);
		performanceService.logTaskEnd(1);
		return commit;
	}

	@Override
	public PrepareAndCommit processPrePrepareRequest(PrePreapreRequest ppq) {
		performanceService.logTaskStart();
		// give reply if they are not byzantine if byzantine just dont respond
		if (!variables.getPreprepare().contains(ppq)) {
			variables.getPreprepare().add(ppq);
		}
		if (peerUtils.isByzantineServer()) {
			return null;
		}
		PrepareAndCommit prepare = generatePrepareMessage(ppq);
		performanceService.logTaskEnd(1);
		return prepare;
	}

	private PrepareAndCommit generateCommitMessage(PrepareAndCommit prepare) {
		performanceService.logTaskStart();
		PrepareAndCommit commit = new PrepareAndCommit();
		commit.setType("COMMITED");
		commit.setMessage(prepare.getMessage());
		commit.setV(prepare.getV());
		commit.setN(prepare.getN());
		commit.setI(peerUtils.getServerPort());
		try {
			commit.setDigest(hashUtils.hashWithSHA256(prepare.getMessage().toString()));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Exception while generating digest");
		}
		try {
			commit.setSignature(digitalSignatureUtil.signMessage(prepare.getDigest(), variables.getPrivateKey()));
		} catch (Exception e) {
			System.out.println("Exception while signing digest..");
		}
		performanceService.logTaskEnd(1);
		return commit;
	}

	private PrepareAndCommit generatePrepareMessage(PrePreapreRequest ppr) {
		performanceService.logTaskStart();
		PrepareAndCommit prepare = new PrepareAndCommit();
		prepare.setType("PREPARED");
		prepare.setMessage(ppr.getMessage());
		prepare.setV(ppr.getV());
		prepare.setN(ppr.getN());
		prepare.setI(peerUtils.getServerPort());
		try {
			prepare.setDigest(hashUtils.hashWithSHA256(ppr.getMessage().toString()));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Exception while generating digest");
		}
		try {
			prepare.setSignature(digitalSignatureUtil.signMessage(prepare.getDigest(), variables.getPrivateKey()));
		} catch (Exception e) {
			System.out.println("Exception while signing digest..");
		}
		performanceService.logTaskEnd(1);
		return prepare;
	}

	public PrePreapreRequest generatePrePrepareMessage(TransactionRequest transactionRequest) {
		performanceService.logTaskStart();
		PrePreapreRequest preprepareRequest = new PrePreapreRequest();
		preprepareRequest.setType("PRE-PREPARE");
		preprepareRequest.setV(variables.getView());
		preprepareRequest.setN(variables.getCounter());
		try {
			preprepareRequest.setDigest(hashUtils.hashWithSHA256(transactionRequest.getTransaction().toString()));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Exception while generating digest...");
		}
		try {
			preprepareRequest.setSignature(
					digitalSignatureUtil.signMessage(preprepareRequest.getDigest(), variables.getPrivateKey()));
		} catch (Exception e) {
			System.out.println("Exception while signing digest...");
		}
		preprepareRequest.setMessage(transactionRequest.getTransaction());
		performanceService.logTaskEnd(1);
		return preprepareRequest;
	}

	@Override
	public List<PrepareAndCommit> getPrepareLog() {
		performanceService.logTaskStart();
		performanceService.logTaskEnd(1);
		return variables.getPrepare();
	}

	@Override
	public List<PrepareAndCommit> getCommitLog() {
		performanceService.logTaskStart();
		performanceService.logTaskEnd(1);
		return variables.getCommitted();
	}

	@Override
	public List<PrePreapreRequest> getPrePrepareLog() {
		performanceService.logTaskStart();
		performanceService.logTaskEnd(1);
		return variables.getPreprepare();
	}

	private boolean verifyRequestSignature(TransactionRequest transactionRequest) {
		performanceService.logTaskStart();
		try {
			return digitalSignatureUtil.verifySignature(
					hashUtils.hashWithSHA256(transactionRequest.getTransaction().toString()),
					transactionRequest.getSignature(), variables.getPublicKeys().get(0));
		} catch (Exception e) {
			System.out.println("Error while verifying the signature from the client...");
		}
		performanceService.logTaskEnd(1);
		return false;
	}

	private Reply generateReplyMessage(PrepareAndCommit commit) {
		performanceService.logTaskStart();
		Reply reply = new Reply();
		reply.setType("EXECUTED");
		reply.setI(commit.getI());
		reply.setTimestamp(commit.getMessage().getTimestamp());
		reply.setV(commit.getV());
		reply.setN(commit.getN());
		performanceService.logTaskEnd(1);
		return reply;
	}

	private synchronized void executeTransactionsUntil(PrepareAndCommit commit) {
		performanceService.logTaskStart();
		int targetN = commit.getN();
		if (targetN > variables.getLastExecutedN()) {
			variables.getPendingTransactions().put(targetN, commit);
		}
		while (variables.getPendingTransactions().containsKey(variables.getLastExecutedN() + 1)) {
			int nextN = variables.getLastExecutedN() + 1;
			PrepareAndCommit nextCommit = variables.getPendingTransactions().get(nextN);
			if (localExecutedTransactions.contains(nextN)) {
				variables.getPendingTransactions().remove(nextN);
				variables.incrementLastExecutedN();
				continue;
			}
			localExecutedTransactions.add(nextN);
			Reply reply = generateReplyMessage(nextCommit);
			Account sender = accountRepository.findByName(nextCommit.getMessage().getSender());
			Account receiver = accountRepository.findByName(nextCommit.getMessage().getReceiver());
			if (sender != null && receiver != null && sender.getBalance() >= nextCommit.getMessage().getAmount()) {
				sender.setBalance(sender.getBalance() - nextCommit.getMessage().getAmount());
				receiver.setBalance(receiver.getBalance() + nextCommit.getMessage().getAmount());
				accountRepository.save(sender);
				accountRepository.save(receiver);
				variables.getExecuted().add(reply);
			} else {
				variables.getExecuted().add(reply);
				System.out.println("Transaction N=" + nextN + " failed due to insufficient balance or invalid accounts.");
			}
			variables.getPendingTransactions().remove(nextN);
			variables.incrementLastExecutedN();
		}
		if((variables.getExecuted().size() % variables.getCHECKPOINT_INTERVAL()) == 0){
			updateCheckPoint();
		}
		performanceService.logTaskEnd(1);
		localExecutedTransactions.clear();
	}

	private void updateCheckPoint() {
		variables.setCheckpoint(variables.getCheckpoint() + variables.getCHECKPOINT_INTERVAL());
		variables.setChpreprepare(variables.getPreprepare());
		variables.setChprepare(variables.getPrepare());
		variables.setChcommitted(variables.getCommitted());
		variables.setChexecuted(variables.getExecuted());
		variables.setChviewChange(variables.getViewChange());
		variables.setChnewViews(variables.getNewViews());
		variables.getPrepare().clear();
		variables.getPreprepare().clear();
		variables.getCommitted().clear();
		variables.getExecuted().clear();
		variables.getViewChange().clear();
		variables.getNewViews().clear();
	}

	@Override
	public List<Reply> getExecuted() {
		performanceService.logTaskStart();
		performanceService.logTaskEnd(1);
		return variables.getExecuted();
	}

	public void viewChange() {
		performanceService.logTaskStart();
		ViewChange viewChange = generateViewChangeMessage();
		List<CompletableFuture<Void>> futures = peerUtils.getAllServersListExceptPrimary().stream()
				.map(url -> CompletableFuture.runAsync(() -> {
					try {
						restTemplate.postForLocation("http://" + url + "/api/initiate/view/change", viewChange);
					} catch (Exception e) {
					}

				}).orTimeout(timeoutInSeconds, TimeUnit.SECONDS)).toList();
		CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		allFutures.join();
		performanceService.logTaskEnd(7);
	}

	private ViewChange generateViewChangeMessage() {
		performanceService.logTaskStart();
		ViewChange viewChange = new ViewChange();
		viewChange.setType("VIEW-CHANGE");
		viewChange.setView(variables.getView() + 1);
		viewChange.setI(peerUtils.getServerPort());
		viewChange.setCheckPointCertificate(variables.getPrepare());
		try {
			viewChange.setDigest(hashUtils.hashWithSHA256(viewChange.getType()));
			try {
				viewChange.setSignature(
						digitalSignatureUtil.signMessage(viewChange.getDigest(), variables.getPrivateKey()));
			} catch (Exception e) {
				System.out.println("Error signing message...");
			}
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error hashing...");
		}
		performanceService.logTaskEnd(1);
		return viewChange;
	}

	@Override
	public void filter() {
		performanceService.logTaskStart();
		@SuppressWarnings("unused")
		List<CompletableFuture<Void>> futures = peerUtils.getAllServersList().stream()
				.map(url -> CompletableFuture.runAsync(() -> {
					try {
						restTemplate.postForLocation("http://" + url + "/api/f/pp", null);
					} catch (Exception e) {
					}
				}).orTimeout(timeoutInSeconds, TimeUnit.SECONDS)).toList();

		performanceService.logTaskEnd(7);
	}

	@Override
	public void filterPrePrepare() {
		performanceService.logTaskStart();
		performanceService.logTaskEnd(1);
		variables.FilterPrePrepareLog();
	}

	@Override
	public void initiateViewChange(ViewChange viewChange) {
		performanceService.logTaskStart();
		viewChange.setI(peerUtils.getServerPort());
		try {
			viewChange.setDigest(hashUtils.hashWithSHA256(viewChange.getType()));
			try {
				viewChange.setSignature(
						digitalSignatureUtil.signMessage(viewChange.getDigest(), variables.getPrivateKey()));
			} catch (Exception e) {
				System.out.println("Signing error...");
			}
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Hashing error...");
		}
		List<CompletableFuture<ViewChange>> futures = peerUtils.getPeersList().stream()
				.map(url -> CompletableFuture.supplyAsync(() -> {
					try {
						return restTemplate.postForObject("http://" + url + "/api/view/change", viewChange,
								ViewChange.class);
					} catch (Exception e) {
						return null;
					}
				}).orTimeout(timeoutInSeconds, TimeUnit.SECONDS)).toList();

		List<ViewChange> replies = futures.stream().map(CompletableFuture::join).filter(Objects::nonNull)
				.filter(result -> result != null).toList();
		if (replies.size() >= variables.getFaultsTolerated() * 2) {
			if (!peerUtils.isByzantineServer()) {
				if (peerUtils.isLeader(peerUtils.getServerPort())) {
					System.out.println("Initiating new view messages...");
					NewView newView = generateNewViewMessage(replies, variables.getPreprepare());
					broadcastNewViewMessage(newView);
				}
			}
		}
		performanceService.logTaskEnd(1);
	}

	private void broadcastNewViewMessage(NewView newView) {
		performanceService.logTaskStart();
		List<CompletableFuture<Void>> futures = peerUtils.getAllServersListExceptPrimary().stream()
				.map(url -> CompletableFuture.runAsync(() -> {
					try {
						restTemplate.postForLocation("http://" + url + "/api/new/view", newView);
					} catch (Exception e) {
					}
				}).orTimeout(timeoutInSeconds, TimeUnit.SECONDS)).toList();
		performanceService.logTaskEnd(1);
	}

	private NewView generateNewViewMessage(List<ViewChange> replies, List<PrePreapreRequest> preprepare) {
		performanceService.logTaskStart();
		variables.setView(variables.getView());
		NewView newView = new NewView();
		newView.setType("NEW-VIEW");
		try {
			newView.setDigest(hashUtils.hashWithSHA256(newView.getType()));
			try {
				newView.setSignature(digitalSignatureUtil.signMessage(newView.getDigest(), variables.getPrivateKey()));
			} catch (Exception e) {
				System.out.println("Error in signing...");
			}
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error in hashing...");
		}
		newView.setView(variables.getView());
		newView.setO(variables.getPreprepare());
		newView.setV(replies);
		performanceService.logTaskEnd(1);
		return newView;
	}

	@Override
	public ViewChange ViewChangeAccept(ViewChange viewChange) {
		performanceService.logTaskStart();
		variables.getViewChange().add(viewChange);
		variables.setView(viewChange.getView());
		if (peerUtils.isByzantineServer()) {
			return null;
		}
		performanceService.logTaskEnd(1);
		return viewChange;
	}

	@Override
	public void newView(NewView newView) {
		performanceService.logTaskStart();
		variables.setView(newView.getView());
		variables.getNewViews().add(newView);
		performanceService.logTaskEnd(1);
	}

	@Override
	public List<ViewChange> viewChangeLogs() {
		performanceService.logTaskStart();
		performanceService.logTaskEnd(1);
		return variables.getViewChange();
	}

	@Override
	public List<NewView> getNewViewLogs() {
		performanceService.logTaskStart();
		performanceService.logTaskEnd(1);
		return variables.getNewViews();
	}

	@Override
	public String getStatus(int sequenceNumber) {
		performanceService.logTaskStart();
		performanceService.logTaskEnd(1);
		return variables.getStatusBySequenceNumber(sequenceNumber);
	}

	@Override
	public CombinedLogs getCombinedLogs() {
		performanceService.logTaskStart();
		CombinedLogs combinedLogs = new CombinedLogs();

		combinedLogs.setPrePrepareLogs(getPrePrepareLog());
		combinedLogs.setPrepareLogs(getPrepareLog());
		combinedLogs.setCommitLogs(getCommitLog());
		combinedLogs.setExecutedLogs(getExecuted());
		combinedLogs.setViewChangeLogs(viewChangeLogs());
		combinedLogs.setNewViewLogs(getNewViewLogs());

		performanceService.logTaskEnd(1);
		return combinedLogs;
	}

	@Override
	public CombinedLogs getCombinedCheckPointLogs() {
		performanceService.logTaskStart();
		CombinedLogs combinedLogs = new CombinedLogs();
		combinedLogs.setPrePrepareLogs(variables.getChpreprepare());
		combinedLogs.setPrepareLogs(variables.getChprepare());
		combinedLogs.setCommitLogs(variables.getChcommitted());
		combinedLogs.setExecutedLogs(variables.getChexecuted());
		combinedLogs.setViewChangeLogs(variables.getChviewChange());
		combinedLogs.setNewViewLogs(variables.getChnewViews());
		performanceService.logTaskEnd(1);
		return combinedLogs;

	}
	
}

