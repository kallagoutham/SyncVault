# SyncVault PBFT Ledger

An implementation of a **PBFT-inspired** distributed banking ledger with linear-style consensus and secure state replication.

## Overview

This project models a replicated banking system where client transactions are ordered and executed across multiple servers even when some nodes fail or behave maliciously. It brings a PBFT touch to a banking ledger by keeping consensus fault-tolerant while reducing communication overhead in the prepare/commit path.

The application keeps a replicated transaction log, maintains account balances in a shared datastore, and exposes HTTP endpoints for PBFT message exchange, server state inspection, and performance tracking.

## Why this name?

If you want a trendier name than just `linear-pbft`, I recommend **SyncVault PBFT Ledger**. It communicates:

- the secure ledger idea (`Vault`)
- the synchronization of replicas (`Sync`)
- the PBFT consensus foundation (`PBFT`)

Other good options are:

- `SyncVault PBFT`
- `PBFT SyncVault`
- `SyncVault Ledger`

## Key Features

- **PBFT fault tolerance**: handles faulty or malicious replicas through PBFT-style message exchange and view changes.
- **Linearized consensus flow**: reduces the communication cost of agreement compared with a classic quadratic PBFT design.
- **Replicated banking ledger**: stores account balances and transaction history across servers.
- **View change support**: allows the system to move to a new leader when the current leader fails or is suspected to be faulty.
- **Performance tracking**: reports latency, throughput, and total processed tasks.
- **Message integrity**: uses SHA-256 hashing and RSA-based signatures for request protection.
- **Checkpoint/log inspection**: exposes endpoints to inspect logs, datastore state, and view-change records.

## Architecture

### Components

- **Spring Boot server**: exposes REST endpoints for PBFT message exchange and inspection.
- **Controllers**: handle transactions, server state, signatures, and PBFT protocol messages.
- **Services**: implement the protocol flow, persistence logic, and performance metrics.
- **H2 in-memory database**: stores replicated banking data during execution.
- **Utilities**: provide hashing, RSA key generation, peer coordination, and threshold-signature helpers.

### Runtime model

- The system is configured for **7 PBFT servers** (`3f + 1` for `f = 2`).
- Client inputs are processed as banking transfers between accounts.
- Each request goes through PBFT-style stages such as `pre-prepare`, `prepare`, `commit`, and execution.
- View-change endpoints support recovery when the leader or replicas are unavailable.

## Repository Structure

- `pbft/` — Spring Boot application and protocol implementation
- `lab_2_resources/` — client driver, CSV inputs, and Postman collection
- `Recording and Report/` — project notes and submission material
- `README.md` — project documentation

## Main API Endpoints

The app uses `/api` as the base context path.

### Transaction and state

- `GET /api/bank/datastore` — view the current account datastore
- `GET /api/bank/local/log` — view the local transaction log
- `POST /api/bank/transaction` — submit a banking transaction
- `GET /api/status/{sequenceNumber}` — inspect the status of a request by sequence number
- `GET /api/bank/performance` — view latency and throughput metrics

### PBFT protocol

- `POST /api/preprepare`
- `POST /api/prepare`
- `POST /api/optimisticcommit`
- `POST /api/commit`
- `POST /api/initiate/view/change`
- `POST /api/view/change`
- `POST /api/new/view`
- `GET /api/log/preprepare`
- `GET /api/log/prepare`
- `GET /api/log/commit`
- `GET /api/log/executed`
- `GET /api/logs/view-change`
- `GET /api/logs/new-view`
- `GET /api/logs/all`
- `GET /api/logs/checkpoint/all`

### Server and signature utilities

- `POST /api/servers/disconnect`
- `GET /api/servers/disconnected`
- `POST /api/servers/byzantine`
- `GET /api/servers/byzantine`
- `GET /api/servers/publickeys`
- `POST /api/reset`
- `POST /api/generate/keypair`
- `GET /api/get/key`
- `POST /api/receive/key`
- `GET /api/print/keys`

## Input Format

Test cases are provided as CSV files in `lab_2_resources/`.

Expected columns:

1. **Set Number** — test case identifier
2. **Transactions** — semicolon-separated transfers in the form `(Sender, Receiver, Amount)`
3. **Live Servers** — list of active servers for the test case
4. **Byzantine Servers** — list of servers that should behave maliciously or incorrectly

### Example

```csv
Set Number,Transactions,Live Servers,Byzantine Servers
1,"(A, C, 1); (C, E, 2)",[S1, S2, S3, S4, S5, S6, S7],[S4, S6]
2,"(A, E, 6); (C, A, 7)",[S1, S2, S3, S5, S6, S7],[S3]
```

## Setup

### Prerequisites

- Java 17
- Maven Wrapper (`./mvnw`) or Maven
- 7 local ports available, typically `8080` through `8086`

### Run the server

From the `pbft/` directory, start one instance per port. For example:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

Repeat until all 7 server instances are running on `8080`–`8086`.

### Run the client

The client driver is located in `lab_2_resources/Client.java`.

```bash
cd lab_2_resources
javac Client.java
java Client
```

Use the menu in the client to trigger the required test cases and protocol actions.

## Configuration

The default server settings live in `pbft/src/main/resources/application.properties`.

- Default port: `8080`
- Context path: `/api`
- Database: in-memory H2 (`jdbc:h2:mem:testdb`)
- H2 console: enabled at `/h2-console`

## Observability

Useful inspection endpoints include:

- transaction log and datastore views
- per-sequence status lookup
- PBFT phase logs
- view-change and new-view traces
- performance metrics for latency and throughput

These are especially helpful when validating Byzantine scenarios, leader failures, and resets between test cases.

## Notes

- The repository includes a Postman collection in `lab_2_resources/PBFT Implementation.postman_collection.json` for testing the API flow.
- The code currently focuses on the banking workload and protocol mechanics rather than a general-purpose PBFT framework.
- Checkpointing and threshold-signature support are present as part of the project direction, with some features depending on the current implementation state.

## References

- Miguel Castro and Barbara Liskov, *Practical Byzantine Fault Tolerance*
- PBFT lecture notes and course material
- GitHub documentation: <https://docs.github.com/en/get-started/getting-started-with-git/set-up-git>

---

👨‍💻 **Kalla Goutham**  
🌐 [Website](https://gouthamkalla.netlify.app/) | [LinkedIn](https://www.linkedin.com/in/goutham-kalla-3b6133112/) | [GitHub](https://github.com/kallagoutham)  
✉️ Reach me at: kallagoutham33@gmail.com
