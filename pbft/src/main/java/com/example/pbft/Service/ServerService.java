package com.example.pbft.Service;

import java.util.List;

public interface ServerService {

	boolean disconnectServers(List<Integer> servers);
	List<Integer> getDisconnectedServers();
	boolean byzantineServers(List<Integer> servers);
	List<Integer> getByzantineServers();
	boolean getPublicKeys();

}
