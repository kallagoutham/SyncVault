package com.example.pbft.Models;

import java.util.List;

public class CombinedLogs {
    private List<PrePreapreRequest> prePrepareLogs;
    private List<PrepareAndCommit> prepareLogs;
    private List<PrepareAndCommit> commitLogs;
    private List<Reply> executedLogs;
    private List<ViewChange> viewChangeLogs;
    private List<NewView> newViewLogs;


    public List<PrePreapreRequest> getPrePrepareLogs() {
        return prePrepareLogs;
    }

    public void setPrePrepareLogs(List<PrePreapreRequest> prePrepareLogs) {
        this.prePrepareLogs = prePrepareLogs;
    }

    public List<PrepareAndCommit> getPrepareLogs() {
        return prepareLogs;
    }

    public void setPrepareLogs(List<PrepareAndCommit> prepareLogs) {
        this.prepareLogs = prepareLogs;
    }

    public List<PrepareAndCommit> getCommitLogs() {
        return commitLogs;
    }

    public void setCommitLogs(List<PrepareAndCommit> commitLogs) {
        this.commitLogs = commitLogs;
    }

    public List<Reply> getExecutedLogs() {
        return executedLogs;
    }

    public void setExecutedLogs(List<Reply> executedLogs) {
        this.executedLogs = executedLogs;
    }

    public List<ViewChange> getViewChangeLogs() {
        return viewChangeLogs;
    }

    public void setViewChangeLogs(List<ViewChange> viewChangeLogs) {
        this.viewChangeLogs = viewChangeLogs;
    }

    public List<NewView> getNewViewLogs() {
        return newViewLogs;
    }

    public void setNewViewLogs(List<NewView> newViewLogs) {
        this.newViewLogs = newViewLogs;
    }
}
