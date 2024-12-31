package com.example.pbft.Models;

public class ReceiveKey {
	int server;
	String publicKey; 
	
	public int getServer() {
		return server;
	}
	public void setServer(int server) {
		this.server = server;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
}
