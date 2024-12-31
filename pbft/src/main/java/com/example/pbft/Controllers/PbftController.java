package com.example.pbft.Controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbft.Models.CombinedLogs;
import com.example.pbft.Models.NewView;
import com.example.pbft.Models.PrePreapreRequest;
import com.example.pbft.Models.PrepareAndCommit;
import com.example.pbft.Models.Reply;
import com.example.pbft.Models.ViewChange;
import com.example.pbft.Service.TransactionService;

@RestController
public class PbftController {

    private final TransactionService transactionService;

    public PbftController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/preprepare")
    public PrepareAndCommit processPrePrepareRequest(@RequestBody PrePreapreRequest ppq) {
        return transactionService.processPrePrepareRequest(ppq);
    }

    @PostMapping("/prepare")
    public PrepareAndCommit processPrepareRequest(@RequestBody PrepareAndCommit prepare) {
        return transactionService.processPrepareRequest(prepare);
    }

    @PostMapping("/optimisticcommit")
    public void processOptimisticCommitRequest(@RequestBody PrepareAndCommit prepare) {
        transactionService.processOptimisticCommitRequest(prepare);
    }
    
    @PostMapping("/commit")
    public void processCommitRequest(@RequestBody PrepareAndCommit commit) {
        transactionService.processCommitRequest(commit);
    }

    @PostMapping("/initiate/view/change")
    public void viewChange(@RequestBody ViewChange viewChange) {
        transactionService.initiateViewChange(viewChange);
    }

    @PostMapping("/view/change")
    public ViewChange viewChangeBroadCast(@RequestBody  ViewChange viewChange) {
        return transactionService.ViewChangeAccept(viewChange);
    }

    @PostMapping("/new/view")
    public void newViewMessage(@RequestBody NewView newView) {
        transactionService.newView(newView);
    }

    @GetMapping("/log/preprepare")
    public List<PrePreapreRequest> getPrePreareLog() {
        return transactionService.getPrePrepareLog();
    }

    @GetMapping("/log/prepare")
    public List<PrepareAndCommit> getPrepareLog() {
        return transactionService.getPrepareLog();
    }

    @GetMapping("/log/commit")
    public List<PrepareAndCommit> getCommitLog() {
        return transactionService.getCommitLog();
    }

    @GetMapping("/log/executed")
    public List<Reply> getExecuted() {
        return transactionService.getExecuted();
    }

    @GetMapping("/logs/view-change")
    public List<ViewChange> getViewChangeLogs() {
        return transactionService.viewChangeLogs();
    }

    @GetMapping("/logs/new-view")
    public List<NewView> getNewViewLogs() {
        return transactionService.getNewViewLogs();
    }

    @GetMapping("/logs/all")
    public CombinedLogs getAllLogs() {
        return transactionService.getCombinedLogs();
    }
    
    @GetMapping("/logs/checkpoint/all")
    public CombinedLogs getAllCheckPointLogs() {
    	return transactionService.getCombinedCheckPointLogs();
    }
}
