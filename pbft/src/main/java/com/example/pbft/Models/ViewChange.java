package com.example.pbft.Models;

import java.util.ArrayList;
import java.util.List;

public class ViewChange {

	private String type;
	private int view;
	private int n;
	private List<PrepareAndCommit> checkPointCertificate = new ArrayList<>();
	private int i;
	private String digest;
	private String signature;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getView() {
		return view;
	}
	public void setView(int view) {
		this.view = view;
	}
	public int getN() {
		return n;
	}
	public void setN(int n) {
		this.n = n;
	}
	public int getI() {
		return i;
	}
	public void setI(int i) {
		this.i = i;
	}
	public String getDigest() {
		return digest;
	}
	public void setDigest(String digest) {
		this.digest = digest;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public List<PrepareAndCommit> getCheckPointCertificate() {
		return checkPointCertificate;
	}
	public void setCheckPointCertificate(List<PrepareAndCommit> checkPointCertificate) {
		this.checkPointCertificate = checkPointCertificate;
	}
	
}
