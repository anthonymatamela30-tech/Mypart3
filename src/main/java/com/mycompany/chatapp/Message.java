/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chatapp;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Message {

    private String messageID;
    private int messageNumber;
    private String sender;
    private String recipient;
    private String message;
    private String messageHash;
    private String status;

    private static int counter = 0;

    // Part 3 arrays. They grow as messages are added.
    private static Message[] sentMessages = new Message[0];
    private static Message[] disregardedMessages = new Message[0];
    private static Message[] storedMessages = new Message[0];

    private static String[] messageHashes = new String[0];
    private static String[] messageIDs = new String[0];

    // Load saved messages when the class is first used.
    static {
        refreshStoredMessages();
    }

    // Original two-argument constructor, retained for your existing tests.
    public Message(String recipient, String message) {
        this("Unknown", recipient, message);
    }

    // Constructor used by QuickChat.
    public Message(String sender, String recipient, String message) {

        this.sender = sender;
        this.recipient = recipient;
        this.message = message;

        this.messageNumber = counter++;

        this.messageID = String.valueOf(
                ThreadLocalRandom.current().nextLong(
                        1_000_000_000L,
                        10_000_000_000L
                )
        );

        this.status = "Pending";
    }

    // Constructor used when a message is loaded from JSON.
    private Message(
            String sender,
            String recipient,
            String message,
            int messageNumber,
            String messageID,
            String messageHash,
            String status) {

        this.sender = sender == null ? "Unknown" : sender;
        this.recipient = recipient == null ? "" : recipient;
        this.message = message == null ? "" : message;
        this.messageNumber = messageNumber;
        this.messageID = messageID == null
                ? generateMessageID() : messageID;
        this.messageHash = messageHash;
        this.status = status == null ? "Stored" : status;

        counter = Math.max(counter, messageNumber + 1);
    }

    private static String generateMessageID() {
        return String.valueOf(
                ThreadLocalRandom.current().nextLong(
                        1_000_000_000L,
                        10_000_000_000L
                )
        );
    }

    // Getters
    public String getMessageID() {
        return messageID;
    }

    public String getMessageHash() {
        if (messageHash == null || messageHash.isEmpty()) {
            createMessageHash();
        }
        return messageHash;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public String getStatus() {
        return status;
    }

    // Check that the message ID is no longer than ten characters.
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    // Validate a South African international recipient number.
    public boolean checkRecipientCell() {

        return recipient != null
                && recipient.startsWith("+27")
                && recipient.length() == 12
                && recipient.substring(3).matches("\\d{9}");
    }

    // Validate the message length.
    // The response is retained to remain compatible with your existing test.
    public String sentMessage() {

        if (message == null || message.length() > 250) {

            int excess = message == null ? 0 : message.length() - 250;

            return "Message exceeds 250 characters by "
                    + excess + ", please reduce size.";
        }

        return "Message ready to send";
    }

    // Create the message hash.
    public String createMessageHash() {

        String trimmedMessage = message == null ? "" : message.trim();

        String[] words;

        if (trimmedMessage.isEmpty()) {
            words = new String[]{"EMPTY"};
        } else {
            words = trimmedMessage.split("\\s+");
        }

        String firstWord = cleanWord(words[0]);
        String lastWord = cleanWord(words[words.length - 1]);

        if (firstWord.isEmpty()) {
            firstWord = "EMPTY";
        }

        if (lastWord.isEmpty()) {
            lastWord = "EMPTY";
        }

        messageHash = messageID.substring(0, 2)
                + ":" + messageNumber
                + ":" + firstWord.toUpperCase()
                + lastWord.toUpperCase();

        return messageHash;
    }

    private static String cleanWord(String word) {
        return word.replaceAll("[^A-Za-z0-9]", "");
    }

    // Display message details in the required order.
    public String printMessage() {

        return "Message ID: " + messageID
                + "\nMessage Hash: " + getMessageHash()
                + "\nRecipient: " + recipient
                + "\nMessage: " + message;
    }

    // Track the message ID and hash without adding duplicates.
    private void trackMessage() {

        if (messageHash == null || messageHash.isEmpty()) {
            createMessageHash();
        }

        messageIDs = appendUniqueString(messageIDs, messageID);
        messageHashes = appendUniqueString(messageHashes, messageHash);
    }

    // Record a sent message in the Sent Messages array.
    public void markAsSent() {

        status = "Sent";
        trackMessage();

        sentMessages = appendMessageIfMissing(
                sentMessages, this
        );
    }

    // Record a disregarded message in the Disregarded Messages array.
    public void markAsDisregarded() {

        status = "Disregarded";
        trackMessage();

        disregardedMessages = appendMessageIfMissing(
                disregardedMessages, this
        );
    }

    // Store a message in the JSON file and Stored Messages array.
    public JSONObject storeMessage() {

        status = "Stored";
        trackMessage();

        storedMessages = appendMessageIfMissing(
                storedMessages, this
        );

        saveStoredMessagesToJSON();

        return toJSONObject();
    }

    // Convert one message into a JSON object.
    private JSONObject toJSONObject() {

        JSONObject obj = new JSONObject();

        obj.put("MessageID", messageID);
        obj.put("MessageNumber", messageNumber);
        obj.put("MessageHash", getMessageHash());
        obj.put("Sender", sender);
        obj.put("Recipient", recipient);
        obj.put("Message", message);
        obj.put("Status", status);

        return obj;
    }

    // Save all stored messages to message.json.
    private static void saveStoredMessagesToJSON() {

        JSONArray jsonMessages = new JSONArray();

        for (Message msg : storedMessages) {
            jsonMessages.add(msg.toJSONObject());
        }

        try (FileWriter writer = new FileWriter("message.json")) {

            writer.write(jsonMessages.toJSONString());
            writer.flush();

        } catch (IOException e) {

            System.out.println(
                    "Error saving messages: " + e.getMessage()
            );
        }
    }

    /*
     * Read the JSON file into the Stored Messages array.
     * This also supports older JSON objects created by your
     * previous version, which did not save Sender or MessageNumber.
     */
    public static void refreshStoredMessages() {

        File file = new File("message.json");

        if (!file.exists()) {
            storedMessages = new Message[0];
            return;
        }

        try (FileReader reader = new FileReader(file)) {

            Object data = new JSONParser().parse(reader);

            if (!(data instanceof JSONArray)) {
                System.out.println("Invalid message file format.");
                return;
            }

            JSONArray jsonMessages = (JSONArray) data;
            Message[] loadedMessages = new Message[0];

            for (Object item : jsonMessages) {

                if (!(item instanceof JSONObject)) {
                    continue;
                }

                JSONObject obj = (JSONObject) item;

                String id = jsonString(obj, "MessageID", null);
                String hash = jsonString(obj, "MessageHash", null);
                String sender = jsonString(obj, "Sender", "Unknown");
                String recipient = jsonString(obj, "Recipient", "");
                String text = jsonString(obj, "Message", "");

                Object numberValue = obj.get("MessageNumber");

                int number;

                if (numberValue == null) {
                    number = counter++;
                } else {
                    number = parseInteger(numberValue, counter++);
                }

                Message loaded = new Message(
                        sender,
                        recipient,
                        text,
                        number,
                        id,
                        hash,
                        "Stored"
                );

                if (loaded.messageHash == null
                        || loaded.messageHash.isEmpty()) {
                    loaded.createMessageHash();
                }

                loadedMessages = appendMessageIfMissing(
                        loadedMessages, loaded
                );

                messageIDs = appendUniqueString(
                        messageIDs, loaded.messageID
                );

                messageHashes = appendUniqueString(
                        messageHashes, loaded.messageHash
                );
            }

            storedMessages = loadedMessages;

        } catch (IOException | ParseException e) {

            System.out.println(
                    "Could not load saved messages: " + e.getMessage()
            );
        }
    }

    private static String jsonString(
            JSONObject obj, String key, String fallback) {

        Object value = obj.get(key);

        return value == null ? fallback : String.valueOf(value);
    }

    private static int parseInteger(Object value, int fallback) {

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    // Display the sender and recipient of every stored message.
    public static String displayStoredSendersAndRecipients() {

        refreshStoredMessages();

        if (storedMessages.length == 0) {
            return "No stored messages found.";
        }

        StringBuilder result = new StringBuilder();

        for (Message msg : storedMessages) {

            result.append("Sender: ")
                    .append(msg.sender)
                    .append("\nRecipient: ")
                    .append(msg.recipient)
                    .append("\n\n");
        }

        return result.toString().trim();
    }

    // Find and return the longest stored message.
    public static String displayLongestStoredMessage() {

        refreshStoredMessages();

        if (storedMessages.length == 0) {
            return "No stored messages found.";
        }

        Message longest = storedMessages[0];

        for (Message msg : storedMessages) {

            if (msg.message.length() > longest.message.length()) {
                longest = msg;
            }
        }

        return longest.message;
    }

    // Search by ID. Sender/recipient matching is also supported
    // as a fallback for the inconsistent example in the task sheet.
    public static String searchMessageByID(String searchID) {

        refreshStoredMessages();

        if (searchID == null || searchID.trim().isEmpty()) {
            return "Please enter a message ID.";
        }

        String search = searchID.trim();

        Message found = findByID(sentMessages, search);

        if (found == null) {
            found = findByID(storedMessages, search);
        }

        if (found == null) {
            found = findByID(disregardedMessages, search);
        }

        // Fallback: permit a sender or recipient number to be searched.
        if (found == null) {
            found = findBySenderOrRecipient(storedMessages, search);
        }

        if (found == null) {
            found = findBySenderOrRecipient(sentMessages, search);
        }

        if (found == null) {
            found = findBySenderOrRecipient(disregardedMessages, search);
        }

        if (found == null) {
            return "No message found with ID: " + search;
        }

        return "Recipient: " + found.recipient
                + "\nMessage: " + found.message;
    }

    private static Message findByID(
            Message[] messages, String searchID) {

        for (Message msg : messages) {

            if (msg.messageID.equals(searchID)) {
                return msg;
            }
        }

        return null;
    }

    private static Message findBySenderOrRecipient(
            Message[] messages, String value) {

        for (Message msg : messages) {

            if (value.equals(msg.sender)
                    || value.equals(msg.recipient)) {
                return msg;
            }
        }

        return null;
    }

    // Search sent and stored messages for a particular recipient.
    public static String searchMessagesByRecipient(String searchRecipient) {

        refreshStoredMessages();

        if (searchRecipient == null || searchRecipient.trim().isEmpty()) {
            return "Please enter a recipient number.";
        }

        String search = searchRecipient.trim();

        StringBuilder result = new StringBuilder();
        Set<String> seenIDs = new HashSet<>();

        appendRecipientMatches(
                sentMessages, search, seenIDs, result
        );

        appendRecipientMatches(
                storedMessages, search, seenIDs, result
        );

        if (result.length() == 0) {
            return "No messages found for recipient: " + search;
        }

        return result.toString().trim();
    }

    private static void appendRecipientMatches(
            Message[] messages,
            String recipient,
            Set<String> seenIDs,
            StringBuilder result) {

        for (Message msg : messages) {

            if (recipient.equals(msg.recipient)
                    && seenIDs.add(msg.messageID)) {

                result.append(msg.message)
                        .append(System.lineSeparator());
            }
        }
    }

    // Delete a stored message using its hash.
    public static String deleteMessageByHash(String searchHash) {

        refreshStoredMessages();

        if (searchHash == null || searchHash.trim().isEmpty()) {
            return "Please enter a message hash.";
        }

        String search = searchHash.trim();

        for (int i = 0; i < storedMessages.length; i++) {

            Message msg = storedMessages[i];

            if (msg.getMessageHash().equalsIgnoreCase(search)) {

                Message deleted = msg;

                storedMessages = removeMessageAt(
                        storedMessages, i
                );

                removeTrackingIfUnused(deleted);
                saveStoredMessagesToJSON();

                return "Message: \"" + deleted.message
                        + "\" successfully deleted.";
            }
        }

        return "No stored message found with hash: " + search;
    }

    private static Message[] removeMessageAt(
            Message[] messages, int index) {

        Message[] result = new Message[messages.length - 1];

        System.arraycopy(messages, 0, result, 0, index);

        System.arraycopy(
                messages,
                index + 1,
                result,
                index,
                messages.length - index - 1
        );

        return result;
    }

    private static void removeTrackingIfUnused(Message deleted) {

        boolean stillExists =
                containsID(sentMessages, deleted.messageID)
                || containsID(disregardedMessages, deleted.messageID)
                || containsID(storedMessages, deleted.messageID);

        if (!stillExists) {

            messageIDs = removeString(
                    messageIDs, deleted.messageID
            );

            messageHashes = removeString(
                    messageHashes, deleted.messageHash
            );
        }
    }

    private static boolean containsID(
            Message[] messages, String id) {

        for (Message msg : messages) {

            if (msg.messageID.equals(id)) {
                return true;
            }
        }

        return false;
    }

    private static String[] removeString(
            String[] values, String value) {

        int position = -1;

        for (int i = 0; i < values.length; i++) {

            if (values[i].equals(value)) {
                position = i;
                break;
            }
        }

        if (position == -1) {
            return values;
        }

        String[] result = new String[values.length - 1];

        System.arraycopy(values, 0, result, 0, position);

        System.arraycopy(
                values,
                position + 1,
                result,
                position,
                values.length - position - 1
        );

        return result;
    }

    // Display a report containing message details.
    public static String displayMessageReport() {

        refreshStoredMessages();

        StringBuilder report = new StringBuilder(
                "MESSAGE REPORT\n\n"
        );

        boolean hasMessages = false;

        hasMessages |= appendReportSection(
                report, "Sent Messages", sentMessages
        );

        hasMessages |= appendReportSection(
                report, "Stored Messages", storedMessages
        );

        hasMessages |= appendReportSection(
                report, "Disregarded Messages", disregardedMessages
        );

        if (!hasMessages) {
            return "No messages available to report.";
        }

        return report.toString().trim();
    }

    private static boolean appendReportSection(
            StringBuilder report,
            String heading,
            Message[] messages) {

        if (messages.length == 0) {
            return false;
        }

        report.append(heading).append("\n");

        for (Message msg : messages) {

            report.append("Message ID: ")
                    .append(msg.messageID)
                    .append("\nMessage Hash: ")
                    .append(msg.getMessageHash())
                    .append("\nRecipient: ")
                    .append(msg.recipient)
                    .append("\nMessage: ")
                    .append(msg.message)
                    .append("\n\n");
        }

        return true;
    }

    // Populate a new array without adding the same message ID twice.
    private static Message[] appendMessageIfMissing(
            Message[] messages, Message message) {

        for (Message existing : messages) {

            if (existing.messageID.equals(message.messageID)) {
                return messages;
            }
        }

        Message[] result = Arrays.copyOf(
                messages, messages.length + 1
        );

        result[result.length - 1] = message;

        return result;
    }

    private static String[] appendUniqueString(
            String[] values, String value) {

        if (value == null) {
            return values;
        }

        for (String existing : values) {

            if (existing.equals(value)) {
                return values;
            }
        }

        String[] result = Arrays.copyOf(
                values, values.length + 1
        );

        result[result.length - 1] = value;

        return result;
    }

    // Array getters for Part 3 and unit tests.
    public static Message[] getSentMessagesArray() {
        return Arrays.copyOf(sentMessages, sentMessages.length);
    }

    public static Message[] getDisregardedMessagesArray() {
        return Arrays.copyOf(
                disregardedMessages, disregardedMessages.length
        );
    }

    public static Message[] getStoredMessagesArray() {
        refreshStoredMessages();

        return Arrays.copyOf(storedMessages, storedMessages.length);
    }

    public static String[] getMessageHashesArray() {
        return Arrays.copyOf(messageHashes, messageHashes.length);
    }

    public static String[] getMessageIDsArray() {
        return Arrays.copyOf(messageIDs, messageIDs.length);
    }

    public static int getTotalMessagesSent() {
        return sentMessages.length;
    }

    // Method requested in the assignment.
    public int returnTotalMessagess() {
        return sentMessages.length;
    }

    // Retained from your original class.
    public static void readMessage() {

        refreshStoredMessages();

        System.out.println("\nStored Messages:");

        if (storedMessages.length == 0) {
            System.out.println("No stored messages found.");
            return;
        }

        for (Message msg : storedMessages) {
            System.out.println(msg.printMessage());
            System.out.println();
        }
    }
}
  
