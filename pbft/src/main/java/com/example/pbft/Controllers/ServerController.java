package com.example.pbft.Controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbft.Service.ResetService;
import com.example.pbft.Service.ServerService;

@RestController
public class ServerController {
	
	private final ServerService serverService; 
	private final ResetService resetService;

	public ServerController(ServerService serverService, ResetService resetService) {
		super();
		this.serverService = serverService;
		this.resetService = resetService;
	}

	@PostMapping("/servers/disconnect")
	public ResponseEntity<String> disconnectServers(@RequestBody List<Integer> servers) {
		if (serverService.disconnectServers(servers)) {
			return ResponseEntity.status(200).body("Servers disconnected successfully");
		}
		return ResponseEntity.status(401).body("Unable to disconnect servers");
	}

	@GetMapping("/servers/disconnected")
	public List<Integer> getDisconnectedServers() {
		return serverService.getDisconnectedServers();
	}
	
	@PostMapping("/servers/byzantine")
	public ResponseEntity<String> byzantineServers(@RequestBody List<Integer> servers){
		if(serverService.byzantineServers(servers)){
			return ResponseEntity.status(200).body("Byzantine Servers saved successfully");
		}
		return ResponseEntity.status(200).body("Unable to save byzantine servers");
	}
	
	@GetMapping("/servers/byzantine")
	public List<Integer> getByazantineServers() {
		return serverService.getByzantineServers();
	}
	
	@GetMapping("/servers/publickeys")
	public ResponseEntity<String> getPublicKeysFromPeers(){
		if(serverService.getPublicKeys()) {
			return ResponseEntity.status(200).body("Public Keys Fetch Successful.");
		}
		return ResponseEntity.status(401).body("Unable to fetch public keys from peers.");
	}
	
	@PostMapping("/reset")
	public void resetServers() {
		resetService.reset();
	}
	
}
