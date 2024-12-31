
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class Client {

    private static final List<String> peers = Arrays.asList(
            "localhost:8080",
            "localhost:8081",
            "localhost:8082",
            "localhost:8083",
            "localhost:8084",
            "localhost:8085",
            "localhost:8086"
    );

    private static final Map<String, Integer> serverPortMap = new HashMap<>();
    static {
        serverPortMap.put("S1", 8080);
        serverPortMap.put("S2", 8081);
        serverPortMap.put("S3", 8082);
        serverPortMap.put("S4", 8083);
        serverPortMap.put("S5", 8084);
        serverPortMap.put("S6", 8085);
        serverPortMap.put("S7", 8086);
    }

    private static KeyPair keyPair;

    static {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            keyPair = keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error generating RSA key pair.");
        }
    }

    public static PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public static PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public static String signMessage(String message, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes());
        byte[] signedBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signedBytes);
    }
    
    public static boolean verifySignature(String message, String signatureStr, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(message.getBytes());
        byte[] signatureBytes = Base64.getDecoder().decode(signatureStr);
        return signature.verify(signatureBytes);
    }

    public static String hashWithSHA256(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    private static final String BASE_URL_8080 = "http://localhost:8080/api";
    private static final String BASE_URL_8081 = "http://localhost:8081/api";
    private static final String BASE_URL_8082 = "http://localhost:8082/api";
    private static final String BASE_URL_8083 = "http://localhost:8083/api";
    private static final String BASE_URL_8084 = "http://localhost:8084/api";
    private static final String BASE_URL_8085 = "http://localhost:8085/api";
    private static final String BASE_URL_8086 = "http://localhost:8086/api";

    private static AtomicInteger view = new AtomicInteger(0);

    public static void main(String[] args) {
        String csvFile = "lab2_test_cases.csv";
        String line;
        String currentSet = null;
        Map<String, List<String>> transactions = new HashMap<>();
        List<String> allLines = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        String disconnectedServers = "";
        String byzantineServers = "";
        int numberOfNodes = 7;

        exchangeKeys();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                allLines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV file.");
        }
        int currentLineIndex = 0;

        while (true) {
            System.out.println("\nChoose an option:");
            System.out.println("1: Process Next Set of Transactions");
            System.out.println("2: Print Status");
            System.out.println("3: Print DB");
            System.out.println("4: Print Log");
            System.out.println("5: Print View");
            System.out.println("6: Reset Server States");
            System.out.println("7: Print Performance Metrics");
            System.out.println("8: Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    if (currentLineIndex < allLines.size()) {
                        while (currentLineIndex < allLines.size()) {
                            line = allLines.get(currentLineIndex);
                            currentLineIndex++;
                            String[] columns = line.split(",", 3);
                            if (!columns[0].trim().isEmpty()) {
                                if (currentSet != null) {
                                    transactions.clear();
                                    disconnectedServers = "";
                                    byzantineServers = "";
                                }
                                currentSet = columns[0].trim();
                            }

                            if (columns.length > 1 && !columns[1].trim().isEmpty()) {
                                int startIndex = line.indexOf('(');
                                int endIndex = line.indexOf(')');
                                if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                                    String extractedTransaction = line.substring(startIndex + 1, endIndex);
                                    String client = extractedTransaction.split(",")[0].trim();
                                    if (transactions.containsKey(client)) {
                                        transactions.get(client).add(extractedTransaction);
                                    } else {
                                        List<String> transactionList = new ArrayList<>();
                                        transactionList.add(extractedTransaction);
                                        transactions.put(client, transactionList);
                                    }
                                }
                                int start = line.indexOf("[");
                                int end = line.indexOf("]");
                                if (start != -1 && end != -1 && start < end) {
                                    disconnectedServers = getDisconnectedServers(line.substring(start + 1, end));
                                }
                                start = line.indexOf("[", start + 1);
                                end = line.indexOf("]", end + 1);
                                if (start != -1 && end != -1 && start < end) {
                                    List<String> byzantineServersList = Arrays.asList(line.substring(start + 1, end).replaceAll("\\s+", "").split(","));
                                    byzantineServers = String.join(",", byzantineServersList);
                                }
                            }
                            if (currentLineIndex < allLines.size() && !allLines.get(currentLineIndex).split(",", 3)[0].trim().isEmpty()) {
                                break;
                            }
                        }
                        if (currentSet != null) {
                            printTransactions(currentSet, transactions, disconnectedServers, byzantineServers, view, numberOfNodes);
                        }
                    } else {
                        System.out.println("No more sets to process.");
                    }
                    break;
                case 2:
                    printStatus();
                    break;
                case 3:
                    printDB();
                    break;
                case 4:
                    printLog();
                    break;
                case 5:
                    printView();
                    break;
                case 6:
                    resetServerStates();
                    break;
                case 7:
                    PrintPerformance();
                    break;
                case 8:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private static void printTransactions(String set, Map<String,List<String>> transactions, String disconnectedServers, String byzantineServers, AtomicInteger view, int numberOfNodes) {
        List<Boolean> transactionResults = new ArrayList<>();
        disconnectServers(disconnectedServers);
        byzantineServers(byzantineServers);
        System.out.println("*********************************Set - " + set + " Transactions*********************************");
    while(transactionResults.isEmpty() || !transactionResults.stream().allMatch(Boolean.TRUE::equals)){
        transactionResults.clear();
        ExecutorService executorService = Executors.newFixedThreadPool(transactions.size());
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(transactions.size());
        for (Map.Entry<String, List<String>> entry : transactions.entrySet()) {
            List<String> transactionList = entry.getValue();
            Runnable clientTask = () -> {
                // System.out.println("Transactions for the client :" + client + " :   " + transactionList);
                for (String transaction : transactionList) {
                    // System.out.println(transaction+ " " + sequenceNumber);
                    String[] transactionArray = Arrays.stream(transaction.split(","))
                            .map(String::trim)
                            .toArray(String[]::new);
                    //System.out.println("Transaction : " + transactionArray[0] + " " + transactionArray[1] + " " + transactionArray[2]);
                    Runnable transactionTask = () -> {
                        String sender = transactionArray[0];
                        String receiver = transactionArray[1];
                        String amount = transactionArray[2];
                        //System.out.println(sender+""+ receiver+""+ amount+""+ view+""+ numberOfNodes);
                        transactionResults.add(processTransaction(sender, receiver, amount, view.get(), numberOfNodes));
                    };
                    scheduledExecutorService.schedule(transactionTask, 2, TimeUnit.SECONDS); // Schedule each transaction with a delay of 1 second
                }
            };
            executorService.submit(clientTask);
        }
        view.incrementAndGet();
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        scheduledExecutorService.shutdown();
        try {
            if (!scheduledExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduledExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduledExecutorService.shutdownNow();
        }

    }

        System.out.println("*******************************Set - " + set + " Transactions Complete*****************************");
    }

    private static boolean processTransaction(String sender, String receiver, String amt, int view, int numberOfNodes) {
        int amount;
        long timestamp = Instant.now().toEpochMilli();
        System.out.println("Processing transaction from " + sender + " to " + receiver + " for amount " + amt + " at timestamp " + timestamp);
        try {
            amount = Integer.parseInt(amt);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please provide a valid integer.");
            return false;
        }
        String targetUrl = getBaseUrlForPrimary(view, numberOfNodes) + "/bank/transaction";
        String requestString = String.format(
                "{\"sender\":\"%s\",\"receiver\":\"%s\",\"amount\":%d,\"timestamp\":%d}",
                sender, receiver, amount, timestamp
            );
        String signature = "";
        try {
            String digest = hashWithSHA256(requestString);
            try {
                signature = signMessage(digest, getPrivateKey());
            } catch (Exception e) {
                System.out.println("Error signing transaction data.");
            }
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error hashing transaction data.");
        }
        String jsonInputString = String.format(
                "{\"type\":\"REQUEST\",\"transaction\":{\"sender\":\"%s\",\"receiver\":\"%s\",\"amount\":%d,\"timestamp\":%d},\"client\":\"%s\",\"signature\":\"%s\"}",
                sender, receiver, amount, timestamp, sender,signature
        );
        try {
            URL url = new URL(targetUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return true;
                // System.out.println("Transaction processed successfully by " + targetUrl);
            } 
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private static void printDB() {
        String[] headers = {"Server", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
        for (String header : headers) {
            System.out.printf("%-8s", header);
        }
        System.out.println();
        System.out.println("-------------------------------------------------------------------------------------");
        for (int i=1;i<=7;i++) {
            try {
                URL url = new URL("http://" + peers.get(i-1)+ "/api/bank/datastore");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                if (connection.getResponseCode() != 200) {
                    continue;
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                connection.disconnect();
                List<Integer> balances = parseBalances(response.toString());
                System.out.printf("S%-7d", i);
                for (int balance : balances) {
                    System.out.printf("%-8d", balance);
                }
                System.out.println();

            } catch (Exception e) {
                System.out.println("Error fetching data for server S" + i + ": " + e.getMessage());
            }
        }
    }

    private static void printLog() {
        System.out.println("Select the Server (S1-S7) for which you want to see all the logs : ");
        String server = new Scanner(System.in).nextLine();
        String targetUrl = getBaseUrlForSender(server) + "/logs/all";
        try {
            URL url = new URL(targetUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("All logs : \n" + formatJson(removeSignatureFields(response.toString())));
            } else {
                System.out.println("Error response from server: " + con.getResponseMessage());
            }
        } catch (Exception e) {
            System.out.println("Error while fetching all logs : " + server);
        }
    }

    private static void disconnectServers(String server) {
        String[] servers = server.split(",");
        List<Integer> server_ports = Arrays.stream(servers)
                .map(serverPortMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        for (Integer port : server_ports) {
            System.out.println("Disconnecting Server : " + port);
        }
        for (String peer : peers) {
            sendDisconnectRequestToPeer(peer, server_ports);
        }
    }

    private static void byzantineServers(String server) {
        String[] servers = server.split(",");
        List<Integer> server_ports = Arrays.stream(servers)
                .map(serverPortMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        for (String peer : peers) {
            sendByzantineRequestToPeer(peer, server_ports);
        }
    }

    private static void sendDisconnectRequestToPeer(String peer, List<Integer> server_ports) {
        try {
            URL url = new URL("http://" + peer + "/api/servers/disconnect");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            String jsonInputString = server_ports.toString();
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Error response from server: " + conn.getResponseMessage());
            }

        } catch (Exception e) {
            System.out.println("Error while disconnecting servers on peer: " + peer);
            e.printStackTrace();
        }
    }

    private static void sendByzantineRequestToPeer(String peer, List<Integer> server_ports) {
        try {
            URL url = new URL("http://" + peer + "/api/servers/byzantine");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            String jsonInputString = server_ports.toString();
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Error response from server: " + conn.getResponseMessage());
            }

        } catch (Exception e) {
            System.out.println("Error while sending byzantine request to peer: " + peer);
            e.printStackTrace();
        }
    }
    
    private static String getDisconnectedServers(String liveServers) {
        String allServers = "S1,S2,S3,S4,S5,S6,S7";
        List<String> allServersList = Arrays.asList(allServers.split(","));
        List<String> liveServersList = Arrays.asList(liveServers.replaceAll("\\s+", "").split(","));
        List<String> disconnectedServers = allServersList.stream()
                .filter(element -> !liveServersList.contains(element))
                .collect(Collectors.toList());
        return String.join(",", disconnectedServers);

    }

    private static String getBaseUrlForPrimary(int v, int n) {
        switch (v % n) {
            case 1:
                return BASE_URL_8080;
            case 2:
                return BASE_URL_8081;
            case 3:
                return BASE_URL_8082;
            case 4:
                return BASE_URL_8083;
            case 5:
                return BASE_URL_8084;
            case 6:
                return BASE_URL_8085;
            case 0:
                return BASE_URL_8086;
            default:
                return BASE_URL_8080;
        }
    }

    private static String getBaseUrlForSender(String server) {
        switch (server) {
            case "S1":
                return BASE_URL_8080;
            case "S2":
                return BASE_URL_8081;
            case "S3":
                return BASE_URL_8082;
            case "S4":
                return BASE_URL_8083;
            case "S5":
                return BASE_URL_8084;
            case "S6":
                return BASE_URL_8085;
            case "S7":
                return BASE_URL_8086;
            default:
                return BASE_URL_8080;
        }
    }

    private static void exchangeKeys() {
        System.out.println("Exchanging keys with peers...");
        for (String peer : peers) {
            try {
                URL url = new URL("http://" + peer + "/api/servers/publickeys");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.getResponseCode();
            } catch (Exception e) {
                System.out.println("Error while exchanging keys on peer: " + peer);
            }
        }
        for (String peer : peers) {
            try {
                URL url = new URL("http://" + peer + "/api/receive/key");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                String jsonInputString = String.format(
                        "{\"server\":\"%d\",\"publicKey\":\"%s\"}",
                        0,Base64.getEncoder().encodeToString(getPublicKey().getEncoded())
                );
                try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
                conn.getResponseCode();
            } catch (Exception e) {
                System.out.println("Error while exchanging keys on peer: " + peer);
            }
        }
        System.out.println("Key exchange complete.");
    }

    private static void printStatus() {
        System.out.println("Enter the sequence number : ");
        int sequenceNumber = new Scanner(System.in).nextInt();
        String[] statuses = new String[7];
        for (int i = 1; i <= 7; i++) {
            statuses[i - 1] = fetchServerStatus(peers.get(i-1), sequenceNumber);
        }
        System.out.printf("%-8s", "Server");
        for (int i = 1; i <= statuses.length; i++) {
            System.out.printf("S%-7d", i);
        }
        System.out.println();
        System.out.println("--------------------------------------------------------------------");
        System.out.printf("%-8s", "Status");
        for (String status : statuses) {
            System.out.printf("%-8s", status);
        }
        System.out.println();
    }

    private static void printView() {
        System.out.println("Select the Server (S1-S7) for which you want to see the New View Logs : ");
        String server = new Scanner(System.in).nextLine();
        String targetUrl = getBaseUrlForSender(server) + "/logs/new-view";
        try {
            URL url = new URL(targetUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("New View Logs : \n" + formatJson(removeSignatureFields(response.toString())));
            } else {
                System.out.println("Error response from server: " + con.getResponseMessage());
            }
        } catch (Exception e) {
            System.out.println("Error while fetching new-view logs : " + server);
        }
    }

    private static void PrintPerformance() {
        System.out.println("Select the Server (S1-S7) for which you want to see the performance metrics : ");
        String server = new Scanner(System.in).nextLine();
        String targetUrl = getBaseUrlForSender(server) + "/bank/performance";
        try {
            URL url = new URL(targetUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("Performance Metrics for the server " + response.toString());
            } else {
                System.out.println("Error response from server: " + con.getResponseMessage());
            }
        } catch (Exception e) {
            System.out.println("Error while fetching performance metrics from server: " + server);
        }
    }

    private static void resetServerStates() {
        System.out.println("Resetting server states...");
        view.set(0);
        for (String peer : peers) {
            try {
                URL url = new URL("http://" + peer + "/api/reset");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.getResponseCode();
            } catch (Exception e) {
                System.out.println("Error while resetting server states on peer: " + peer);
            }
        }
        System.out.println("Server states reset complete.");
    }

    private static List<Integer> parseBalances(String jsonResponse) {
        List<Integer> balances = new ArrayList<>();
        jsonResponse = jsonResponse.trim();
        if (jsonResponse.startsWith("[") && jsonResponse.endsWith("]")) {
            jsonResponse = jsonResponse.substring(1, jsonResponse.length() - 1);
        }
        String[] items = jsonResponse.split("\\},\\{");
        for (String item : items) {
            item = item.replaceAll("^\\{", "").replaceAll("\\}$", "").trim();
            int balanceIndex = item.indexOf("\"balance\":");
            if (balanceIndex != -1) {
                int start = balanceIndex + 10;
                int end = item.indexOf(",", start);
                if (end == -1) {
                    end = item.length(); 
                }
                String balanceStr = item.substring(start, end).trim();
                try {
                    balances.add(Integer.parseInt(balanceStr));
                } catch (NumberFormatException e) {
                    System.out.println("Error parsing balance: " + balanceStr);
                }
            }
        }

        return balances;
    }

    private static String fetchServerStatus(String peer, int sequenceNumber) {
        try {
            URL url = new URL("http://" + peer + "/api/status/" + sequenceNumber);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                return "Error response from server: " + conn.getResponseMessage();
            }
        } catch (Exception e) {
            return "Error while fetching status from peer: " + peer;
        }
    }

    public static String removeSignatureFields(String jsonInput) {
        StringBuilder result = new StringBuilder();
        int length = jsonInput.length();
        int index = 0;

        while (index < length) {
            int sigIndex = jsonInput.indexOf("\"signature\"", index);
            if (sigIndex == -1) {
                // No more "signature" fields, append the rest of the string
                result.append(jsonInput.substring(index));
                break;
            }

            // Append everything before the "signature" field
            result.append(jsonInput.substring(index, sigIndex));

            // Find the start of the value (after the colon)
            int colonIndex = jsonInput.indexOf(':', sigIndex);
            if (colonIndex == -1) {
                // Malformed JSON, return what we have
                break;
            }

            int valueStart = colonIndex + 1;

            // Skip any whitespace
            while (valueStart < length && Character.isWhitespace(jsonInput.charAt(valueStart))) {
                valueStart++;
            }

            // Determine if the value is an object, array, string, or primitive
            char valueFirstChar = jsonInput.charAt(valueStart);
            int valueEnd = valueStart;

            if (valueFirstChar == '{' || valueFirstChar == '[') {
                // Value is an object or array, need to find the matching closing brace/bracket
                char openChar = valueFirstChar;
                char closeChar = (openChar == '{') ? '}' : ']';
                int braceCount = 1;
                valueEnd++;

                while (valueEnd < length && braceCount > 0) {
                    char c = jsonInput.charAt(valueEnd);

                    if (c == openChar) {
                        braceCount++;
                    } else if (c == closeChar) {
                        braceCount--;
                    } else if (c == '"' && jsonInput.charAt(valueEnd - 1) != '\\') {
                        // Skip strings inside the object/array
                        valueEnd++;
                        while (valueEnd < length && (jsonInput.charAt(valueEnd) != '"' || jsonInput.charAt(valueEnd - 1) == '\\')) {
                            valueEnd++;
                        }
                    }

                    valueEnd++;
                }
            } else if (valueFirstChar == '"') {
                // Value is a string, find the closing quote
                valueEnd++;
                while (valueEnd < length && (jsonInput.charAt(valueEnd) != '"' || jsonInput.charAt(valueEnd - 1) == '\\')) {
                    valueEnd++;
                }
                valueEnd++; // Include the closing quote
            } else {
                // Value is a number, boolean, or null
                while (valueEnd < length && jsonInput.charAt(valueEnd) != ',' && jsonInput.charAt(valueEnd) != '}' && jsonInput.charAt(valueEnd) != ']') {
                    valueEnd++;
                }
            }

            // Skip any trailing commas or whitespace
            while (valueEnd < length && (jsonInput.charAt(valueEnd) == ',' || Character.isWhitespace(jsonInput.charAt(valueEnd)))) {
                valueEnd++;
            }

            // Move index to the position after the value
            index = valueEnd;
        }

        return result.toString();
    }

    public static String formatJson(String jsonString) {
        StringBuilder prettyJson = new StringBuilder();
        int indentLevel = 0;
        boolean inQuotes = false;
        for (char charFromJson : jsonString.toCharArray()) {
            switch (charFromJson) {
                case '"':
                    // Toggle the inQuotes flag if the previous character isn't a backslash
                    if (prettyJson.length() > 0 && prettyJson.charAt(prettyJson.length() - 1) != '\\') {
                        inQuotes = !inQuotes;
                    }
                    prettyJson.append(charFromJson);
                    break;
                case ' ':
                    if (inQuotes) {
                        prettyJson.append(charFromJson);
                    }
                    break;
                case '{':
                case '[':
                    prettyJson.append(charFromJson);
                    if (!inQuotes) {
                        prettyJson.append('\n');
                        indentLevel++;
                        addIndentation(prettyJson, indentLevel);
                    }
                    break;
                case '}':
                case ']':
                    if (!inQuotes) {
                        prettyJson.append('\n');
                        indentLevel--;
                        addIndentation(prettyJson, indentLevel);
                        prettyJson.append(charFromJson);
                    } else {
                        prettyJson.append(charFromJson);
                    }
                    break;
                case ',':
                    prettyJson.append(charFromJson);
                    if (!inQuotes) {
                        prettyJson.append('\n');
                        addIndentation(prettyJson, indentLevel);
                    }
                    break;
                case ':':
                    prettyJson.append(charFromJson);
                    if (!inQuotes) {
                        prettyJson.append(' ');
                    }
                    break;
                default:
                    prettyJson.append(charFromJson);
                    break;
            }
        }

        return prettyJson.toString();
    }

    private static void addIndentation(StringBuilder sb, int indentLevel) {
        for (int i = 0; i < indentLevel; i++) {
            sb.append("    "); // 4 spaces per indent level
        }
    }

}

