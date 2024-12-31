package com.example.pbft.Models;

import java.util.List;

public class NewView {

	private String type;
	private int view;
	private String digest;
	private String signature;
	List<PrePreapreRequest> o;
	List<ViewChange> v;
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
	public List<PrePreapreRequest> getO() {
		return o;
	}
	public void setO(List<PrePreapreRequest> o) {
		this.o = o;
	}
	public List<ViewChange> getV() {
		return v;
	}
	public void setV(List<ViewChange> v) {
		this.v = v;
	}
	
}
