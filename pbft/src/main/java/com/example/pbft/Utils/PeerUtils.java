package com.example.pbft.Utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.pbft.GlobalVariables.Variables;

@Component
public class PeerUtils {

	@Value("${server.port}")
	private int serverPort;
	private final Variables variables;

	public PeerUtils(Variables variables) {
		super();
		this.variables = variables;
	}

	public List<String> getPeersList() {
		List<String> peers = new ArrayList<>();
		for (int i = 8080; i <= 8086; ++i) {
			if (serverPort != i && !variables.getDisconnectedServers().contains(i)) {
				peers.add("localhost:" + i);
			}
		}
		return peers;
	}
	
	public List<String> getPeersListIncludingDisconnected() {
		List<String> peers = new ArrayList<>();
		for (int i = 8080; i <= 8086; ++i) {
			if (serverPort != i ) {
				peers.add("localhost:" + i);
			}
		}
		return peers;
	}

	public List<String> getAllServersList() {
		List<String> peers = new ArrayList<>();
		for (int i = 8080; i <= 8086; ++i) {
			if (!variables.getDisconnectedServers().contains(i)) {
				peers.add("localhost:" + i);
			}
		}
		return peers;
	}
	
	public List<String> getAllServersListIncludingDisconnected(){
		List<String> peers = new ArrayList<>();
		for (int i = 8080; i <= 8086; ++i) {
				peers.add("localhost:" + i);
		}
		return peers;

	}
	
	public List<String> getAllServersListExceptPrimary(){
		List<String> peers = new ArrayList<>();
		for(int i=8080;i<=8086;++i) {
			if(!isLeader(i) && !variables.getDisconnectedServers().contains(i)){
				peers.add("localhost:"+i);
			}
		}
		return peers;
	}
	
	public boolean isLeader(int i) {

		int primary = 8080;
		switch (variables.getView() % 7) {
		case 1:
			primary = 8080;
			break;
		case 2:
			primary = 8081;
			break;
		case 3:
			primary = 8082;
			break;
		case 4:
			primary = 8083;
			break;
		case 5:
			primary = 8084;
			break;
		case 6:
			primary = 8085;
			break;
		case 0:
			primary = 8086;
			break;
		}
		return primary==i;
	}
	

	public boolean isByzantineServer() {
		return variables.getByzantineServers().contains(serverPort);
	}
	
	public int getServerPort() {
		return serverPort;
	}

}
