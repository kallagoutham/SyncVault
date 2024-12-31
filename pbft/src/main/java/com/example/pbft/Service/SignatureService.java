package com.example.pbft.Service;

import java.security.PublicKey;

import com.example.pbft.Models.ReceiveKey;

public interface SignatureService {

	boolean generateKeyPair();
	PublicKey getKey();
	String receiveKey(ReceiveKey receiveKey);
	void printPeerPublicKeys();

}
