package com.example.pbft.GlobalVariables;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.pbft.Models.NewView;
import com.example.pbft.Models.PrePreapreRequest;
import com.example.pbft.Models.PrepareAndCommit;
import com.example.pbft.Models.Reply;
import com.example.pbft.Models.ViewChange;

@Component
public class Variables {
	
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private List<Integer> disconnectedServers = new ArrayList<>();
	private List<Integer> byzantineServers = new ArrayList<>();
	private HashMap<Integer, PublicKey> publicKeys = new HashMap<>();
	private int view = 1;
	private int checkpoint=0;
	private List<PrePreapreRequest> preprepare = new ArrayList<>();
	private List<PrepareAndCommit> prepare = new ArrayList<>();
	private List<PrepareAndCommit> committed = new ArrayList<>();
	private List<Reply> executed = new ArrayList<>();
	private AtomicInteger counter = new AtomicInteger(1);
	private final ReentrantLock lock = new ReentrantLock();
	private final int faultsTolerated = 2;
	private AtomicInteger lastExecutedN = new AtomicInteger(0);
	private HashMap<Integer, PrepareAndCommit> pendingTransactions = new HashMap<>();
	private List<ViewChange> viewChange = new ArrayList<>();
	private List<NewView> newViews = new ArrayList<>();
	private boolean viewChangeInProgress = false;
	private final int THRESHOLD = 5; 
    private final int TOTAL_SHARES = 7; 
    private final int CHECKPOINT_INTERVAL = 100;
    private List<PrePreapreRequest> Chpreprepare = new ArrayList<>();
	private List<PrepareAndCommit> Chprepare = new ArrayList<>();
	private List<PrepareAndCommit> Chcommitted = new ArrayList<>();
	private List<Reply> Chexecuted = new ArrayList<>();
	private List<ViewChange> ChviewChange = new ArrayList<>();
	private List<NewView> ChnewViews = new ArrayList<>();
	
	

	public int getCHECKPOINT_INTERVAL() {
		return CHECKPOINT_INTERVAL;
	}

	public boolean isViewChangeInProgress() {
		lock.lock();
		try {
			return viewChangeInProgress;
		}finally {
			lock.unlock();
		}
	}

	public void setViewChangeInProgress(boolean viewChangeInProgress) {
		lock.lock();
		try {
			this.viewChangeInProgress = viewChangeInProgress;
		}finally {
			lock.unlock();
		}
	}

	public int getFaultsTolerated() {
		return faultsTolerated;
	}

	public int getCounter() {
		lock.lock();
		try {
			return counter.get();
		} finally {
			incrementCounter();
			lock.unlock();
		}
	}

	public void setCounter(int value) {
		lock.lock();
		try {
			counter.set(value);
		}finally {
			lock.unlock();
		}
	}

	public void setLastExecutedN(int value) {
		lock.lock();
		try {
			lastExecutedN.set(value);
		}finally {
			lock.unlock();
		}
	}
	
	public void incrementCounter() {
		counter.incrementAndGet();
	}

	public int getLastExecutedN() {
		lock.lock();
		try {
			return lastExecutedN.get();
		} finally {
			lock.unlock();
		}
	}

	public void incrementLastExecutedN() {
		lock.lock();
		lastExecutedN.incrementAndGet();
		lock.unlock();
	}

	public int getCheckpoint() {
		return checkpoint;
	}

	public void setCheckpoint(int checkpoint) {
		this.checkpoint = checkpoint;
	}

	public List<PrePreapreRequest> getPreprepare() {
		return preprepare;
	}

	public void setPreprepare(List<PrePreapreRequest> preprepare) {
		this.preprepare = preprepare;
	}

	public List<PrepareAndCommit> getPrepare() {
		return prepare;
	}

	public void setPrepare(List<PrepareAndCommit> prepare) {
		this.prepare = prepare;
	}

	public List<PrepareAndCommit> getCommitted() {
		return committed;
	}

	public void setCommitted(List<PrepareAndCommit> committed) {
		this.committed = committed;
	}

	public int getView() {
		return view;
	}

	public void setView(int view) {
		this.view = view;
	}

	public List<Integer> getByzantineServers() {
		return byzantineServers;
	}

	public void setByzantineServers(List<Integer> byzantineServers) {
		this.byzantineServers = byzantineServers;
	}

	public List<Integer> getDisconnectedServers() {
		return disconnectedServers;
	}

	public void setDisconnectedServers(List<Integer> disconnectedServers) {
		this.disconnectedServers = disconnectedServers;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public HashMap<Integer, PublicKey> getPublicKeys() {
		return publicKeys;
	}

	public void setPublicKeys(HashMap<Integer, PublicKey> publicKeys) {
		this.publicKeys = publicKeys;
	}

	public List<Reply> getExecuted() {
		return executed;
	}

	public void setExecuted(List<Reply> executed) {
		this.executed = executed;
	}

	public HashMap<Integer, PrepareAndCommit> getPendingTransactions() {
		return pendingTransactions;
	}

	public void setPendingTransactions(HashMap<Integer, PrepareAndCommit> pendingTransactions) {
		this.pendingTransactions = pendingTransactions;
	}
	
	public void addPrepareIfNotExists(PrePreapreRequest newPrepare) {
        lock.lock();
        try {
            boolean exists = preprepare.stream()
                    .anyMatch(prepareEntry -> 
                        prepareEntry.getMessage().getSender().equals(newPrepare.getMessage().getSender())
                    );
            if (!exists) {
                preprepare.add(newPrepare);
            }
        } finally {
            lock.unlock();
        }
    }

	public void FilterPrePrepareLog() {
		setPreprepare(preprepare.stream()
				.collect(Collectors.groupingBy(request -> request.getMessage().getSender(),
						Collectors.minBy(Comparator.comparingInt(PrePreapreRequest::getN))))
				.values().stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()));
	}

	public List<ViewChange> getViewChange() {
		return viewChange;
	}

	public void setViewChange(List<ViewChange> viewChange) {
		this.viewChange = viewChange;
	}

	public List<NewView> getNewViews() {
		return newViews;
	}

	public void setNewViews(List<NewView> newViews) {
		this.newViews = newViews;
	}

	public String getStatusBySequenceNumber(int n) {
	        for (Reply reply : executed) {
	            if (reply.getN() == n) {
	                return "E";
	            }
	        }
	        for (PrepareAndCommit commit : committed) {
	            if (commit.getN() == n) {
	                return "C";
	            }
	        }
	        for (PrepareAndCommit prep : prepare) {
	            if (prep.getN() == n && !byzantineServers.contains(prep.getI())) {
	                return "P";
	            }
	        }
	        for (PrePreapreRequest prePrep : preprepare) {
	            if (prePrep.getN() == n) {
	                return "PP";
	            }
	        }
	        return "X";
	    }

	public int getTHRESHOLD() {
		return THRESHOLD;
	}

	public int getTOTAL_SHARES() {
		return TOTAL_SHARES;
	}

	public List<PrePreapreRequest> getChpreprepare() {
		return Chpreprepare;
	}

	public void setChpreprepare(List<PrePreapreRequest> chpreprepare) {
		Chpreprepare = chpreprepare;
	}

	public List<PrepareAndCommit> getChprepare() {
		return Chprepare;
	}

	public void setChprepare(List<PrepareAndCommit> chprepare) {
		Chprepare = chprepare;
	}

	public List<PrepareAndCommit> getChcommitted() {
		return Chcommitted;
	}

	public void setChcommitted(List<PrepareAndCommit> chcommitted) {
		Chcommitted = chcommitted;
	}

	public List<Reply> getChexecuted() {
		return Chexecuted;
	}

	public void setChexecuted(List<Reply> chexecuted) {
		Chexecuted = chexecuted;
	}

	public List<ViewChange> getChviewChange() {
		return ChviewChange;
	}

	public void setChviewChange(List<ViewChange> chviewChange) {
		ChviewChange = chviewChange;
	}

	public List<NewView> getChnewViews() {
		return ChnewViews;
	}

	public void setChnewViews(List<NewView> chnewViews) {
		ChnewViews = chnewViews;
	}
	
}
