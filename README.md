# Implementing the Linear-PBFT Protocol

## Project Overview
This project focuses on implementing a variant of the Practical Byzantine Fault Tolerance (PBFT) protocol, known as Linear-PBFT. Unlike PBFT, Linear-PBFT reduces the communication complexity of the prepare and commit phases to linear levels while maintaining the fault-tolerance properties of PBFT.

### Objectives
- Develop a distributed banking application using the Linear-PBFT protocol.
- Implement the normal case operation and view-change routine of Linear-PBFT.
- Handle Byzantine nodes and ensure consensus despite faults.
- Optimize throughput and latency.

## Features and Functionality

### Core Features
1. **Consensus Mechanism**:
   - Implements the Linear-PBFT protocol for fault-tolerant consensus.
   - Supports Byzantine leader and replica detection.
2. **Distributed Banking Application**:
   - All servers maintain a replicated log of client transactions.
   - Uses a key-value store to maintain client balances.
3. **Fault Tolerance**:
   - Handles Byzantine leader and replica behaviors.
   - Supports view changes to elect a new leader when failures are detected.
4. **Performance Metrics**:
   - Measures throughput and latency.
5. **Signatrure and Hashing**:
   - At every stage of communication among servers. Hash(SHA256) is created for every message and the digest is Signed with RSA Key and sent over the network to ensure integrity when data is transmitted over a unreliable network.
    
### Additional Functions
- **PrintLog**: Outputs the log of a given server.
- **PrintDB**: Displays the current key-value datastore.
- **PrintStatus**: Reports the transaction status (e.g., Pre-prepared, Prepared, Committed, Executed).
- **PrintView**: Shows all NEW-VIEW messages exchanged during the protocol.

## System Description

### Architecture
- **Clients and Servers**:
  - 7 servers (3f + 1) where f = 2.
  - 10 clients send transactions to the leader or any server acting as the leader.
- **Transaction Processing**:
  - Transactions are submitted in the format `(S, R, amt)` where `S` is the sender, `R` is the receiver, and `amt` is the amount to transfer.
  - All nodes validate and process requests to ensure fault tolerance.

### Linear-PBFT Protocol
1. **Normal Case Operation**:
   - Linear communication phases replace the quadratic prepare and commit phases.
   - A collector node (typically the leader) aggregates and broadcasts certificates for authentication.
2. **View Change**:
   - Initiates when the leader is suspected to be faulty.
   - New leaders propose NEW-VIEW messages to ensure continuity.
3. **Checkpointing (Optional)**:
   - Used to garbage-collect completed consensus instances and restore replicas.

### Datastore Structure
- Represented as a key-value store with:
  - Account balances.
  - Metadata for processed transactions.

## Implementation Details

### Prescribed Conditions
- The system should:
  - Support 10 clients and 7 servers.
  - Allow complete reset between test cases to simulate independent scenarios.

### Functions
1. **PrintLog(Server)**: Displays the metadata of all requests processed by the specified server.
2. **PrintDB()**: Outputs the current key-value datastore state.
3. **PrintStatus(Sequence Number)**: Reports the status of a transaction across servers.
4. **PrintView()**: Displays NEW-VIEW messages exchanged during view changes.
5. **Performance()**: Measures throughput and latency of the system.

### Input Format
- Input files should be CSV files with columns:
  1. **Set Number**: Identifier for the test case.
  2. **Transactions**: List of transactions in the format `(Sender, Receiver, Amount)`.
  3. **Live Servers**: List of active servers for the transaction set.
  4. **Byzantine Servers**: List of servers exhibiting Byzantine behavior.

#### Example Input
```csv
Set Number, Transactions, Live Servers, Byzantine Servers
1, (A, C, 1); (C, E, 2), [S1, S2, S3, S4, S5, S6, S7], [S4, S6]
2, (A, E, 6); (C, A, 7), [S1, S2, S3, S5, S6, S7], [S3]
```

## Setup Instructions

### Repository Setup
1. Clone the GitHub repository:
   ```bash
   https://github.com/kallagoutham/linear-pbft.git
   ```
2. Run 7 PBFT servers on different ports(i.e.,8080,8081,8082,8083,8084,8085,8086).
3. Compile Client.java and run Client.class file. Then a menu will be appeared give options as per required in order to run and evaluate the results.
4. A sample test cases (.csv) file is provided under lab2_resources folder.

### Running the Program
1. Prepare an input file with the required transaction format.
2. Execute the program to process transactions sequentially.
3. Use functions (`PrintLog`, `PrintDB`, `PrintStatus`, `PrintView`) to monitor system states.

## Testing

### Test Cases
1. Valid transactions processed with Byzantine behavior.
2. Fault tolerance during leader failures.
3. View changes triggered by timeout or errors.
4. System reset between independent test cases.

### Example Input File
```csv
Set Number, Transactions, Live Servers, Byzantine Servers
1, (A, C, 1); (C, E, 2), [S1, S2, S3, S4, S5, S6, S7], [S4, S6]
2, (A, E, 6); (C, A, 7), [S1, S2, S3, S5, S6, S7], [S3]
```

## Bonus Features
- [x] **Checkpointing Mechanism**:
   - Decentralized garbage collection of old data.
   - Synchronization of dark replicas.
- [ ] **Threshold Signatures**:
   - Replace individual signatures with a single threshold signature to reduce overhead.
- [x] **Optimistic Phase Reduction**:
   - Eliminate additional phases during non-faulty operation.

## References
- "Practical Byzantine Fault Tolerance" by Miguel Castro and Barbara Liskov.
- PBFT lecture notes.
- [GitHub Setup Instructions](https://docs.github.com/en/get-started/getting-started-with-git/set-up-git)

Start early and follow the guidelines to successfully implement this challenging project!
