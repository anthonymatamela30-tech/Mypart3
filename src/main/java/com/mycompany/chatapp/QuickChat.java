/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chatapp;

import java.util.Scanner;

public class QuickChat {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        // REGISTRATION: USERNAME
        String username;

        while (true) {

            System.out.print("Enter your username: ");
            username = input.nextLine();

            if (username.contains("_") && username.length() <= 5) {
                System.out.println("Username successfully captured.");
                break;
            }

            System.out.println(
                    "Username is not correctly formatted; "
                    + "please ensure that your username contains "
                    + "an underscore and is no more than five "
                    + "characters in length."
            );
        }

        System.out.print("Enter your first name: ");
        String firstName = input.nextLine();

        System.out.print("Enter your last name: ");
        String lastName = input.nextLine();

        // REGISTRATION: PASSWORD
        String password;

        while (true) {

            System.out.print("Enter your password: ");
            password = input.nextLine();

            boolean hasCapital = false;
            boolean hasNumber = false;
            boolean hasSpecial = false;

            for (char c : password.toCharArray()) {

                if (Character.isUpperCase(c)) {
                    hasCapital = true;
                }

                if (Character.isDigit(c)) {
                    hasNumber = true;
                }

                if (!Character.isLetterOrDigit(c)) {
                    hasSpecial = true;
                }
            }

            if (password.length() >= 8
                    && hasCapital
                    && hasNumber
                    && hasSpecial) {

                System.out.println("Password successfully captured.");
                break;
            }

            System.out.println(
                    "Password is not correctly formatted; "
                    + "please ensure that the password contains at "
                    + "least eight characters, a capital letter, "
                    + "a number, and a special character."
            );
        }

        // REGISTRATION: CELL NUMBER
        String cellNumber;

        while (true) {

            System.out.print(
                    "Enter your South African cell number: "
            );

            cellNumber = input.nextLine();

            if (cellNumber.startsWith("+27")
                    && cellNumber.length() == 12
                    && cellNumber.substring(3).matches("\\d{9}")) {

                System.out.println(
                        "Cell phone number successfully captured."
                );

                break;
            }

            System.out.println(
                    "Cell phone number incorrectly formatted or "
                    + "does not contain a valid international code."
            );
        }

        // REGISTER USER
        Login user = new Login(
                username, password, cellNumber
        );

        String registrationResult = user.registerUser();

        System.out.println(registrationResult);

        if (!registrationResult.equals(
                "User has been registered successfully.")) {

            input.close();
            return;
        }

        // LOGIN
        System.out.println("\nLOGIN");

        System.out.print("Enter your username to login: ");
        String loginUsername = input.nextLine();

        System.out.print("Enter your password to login: ");
        String loginPassword = input.nextLine();

        boolean loginStatus = user.loginUser(
                loginUsername, loginPassword
        );

        System.out.println(
                user.returnLoginStatus(
                        loginStatus, firstName, lastName
                )
        );

        // Do not continue if login fails.
        if (!loginStatus) {
            input.close();
            return;
        }

        System.out.println("\nWelcome to QuickChat.");

        // SET MESSAGE LIMIT
        int maxMessages;

        while (true) {

            maxMessages = readInteger(
                    input,
                    "How many messages do you wish to enter? "
            );

            if (maxMessages > 0) {
                break;
            }

            System.out.println(
                    "Please enter a number greater than zero."
            );
        }

        int messagesEntered = 0;
        boolean running = true;

        // MAIN MENU
        while (running) {

            System.out.println("\nPlease choose an option:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");

            int option = readInteger(input, "Choose an option: ");

            switch (option) {

                case 1:

                    if (messagesEntered >= maxMessages) {

                        System.out.println(
                                "You have reached your message limit of "
                                + maxMessages + " messages."
                        );

                        break;
                    }

                    // Allow only the selected number of messages.
                    while (messagesEntered < maxMessages) {

                        System.out.println(
                                "\nEntering message "
                                + (messagesEntered + 1)
                                + " of " + maxMessages
                        );

                        System.out.print("Enter recipient number: ");
                        String recipient = input.nextLine();

                        System.out.print("Enter your message: ");
                        String messageText = input.nextLine();

                        Message msg = new Message(
                                cellNumber, recipient, messageText
                        );

                        // Validate the recipient.
                        if (!msg.checkRecipientCell()) {

                            System.out.println(
                                    "Invalid recipient number. "
                                    + "Please use +27 followed by "
                                    + "9 digits."
                            );

                            continue;
                        }

                        // Validate the message ID.
                        if (!msg.checkMessageID()) {

                            System.out.println("Invalid message ID.");
                            continue;
                        }

                        // Validate message length.
                        String messageStatus = msg.sentMessage();

                        if (!messageStatus.equals(
                                "Message ready to send")) {

                            System.out.println(messageStatus);
                            continue;
                        }

                        // Display message details.
                        System.out.println(msg.printMessage());

                        System.out.println("\n1) Send Message");
                        System.out.println("2) Disregard Message");
                        System.out.println("3) Store Message");

                        int choice = readInteger(
                                input, "Choose an option: "
                        );

                        boolean completed = false;

                        if (choice == 1) {

                            msg.markAsSent();

                            System.out.println(
                                    "Message successfully sent."
                            );

                            completed = true;

                        } else if (choice == 2) {

                            msg.markAsDisregarded();

                            System.out.println(
                                    "Message disregarded."
                            );

                            completed = true;

                        } else if (choice == 3) {

                            msg.storeMessage();

                            System.out.println(
                                    "Message successfully stored."
                            );

                            completed = true;

                        } else {

                            System.out.println(
                                    "Invalid option. Message not processed."
                            );
                        }

                        if (completed) {

                            messagesEntered++;

                            System.out.println(
                                    "Messages entered: "
                                    + messagesEntered
                                    + " of " + maxMessages
                            );
                        }
                    }

                    if (messagesEntered >= maxMessages) {

                        System.out.println(
                                "You have reached your message entry limit."
                        );

                        System.out.println(
                                "Total messages sent: "
                                + Message.getTotalMessagesSent()
                        );
                    }

                    break;

                case 2:

                    System.out.println("Coming Soon.");
                    break;

                case 3:

                    System.out.println("Application Closed.");
                    running = false;
                    break;

                case 4:

                    storedMessagesMenu(input);
                    break;

                default:

                    System.out.println(
                            "Invalid option. Please choose 1, 2, 3, or 4."
                    );
            }
        }

        input.close();
    }

    // PART 3: STORED MESSAGES SUBMENU
    private static void storedMessagesMenu(Scanner input) {

        boolean inSubmenu = true;

        while (inSubmenu) {

            System.out.println("\n--- STORED MESSAGES ---");
            System.out.println("1) Display senders and recipients");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search messages for a recipient");
            System.out.println("5) Delete a message using its hash");
            System.out.println("6) Display the full message report");
            System.out.println("7) Return to main menu");

            int choice = readInteger(input, "Choose an option: ");

            switch (choice) {

                case 1:

                    System.out.println(
                            Message.displayStoredSendersAndRecipients()
                    );

                    break;

                case 2:

                    System.out.println("\nLongest stored message:");

                    System.out.println(
                            Message.displayLongestStoredMessage()
                    );

                    break;

                case 3:

                    System.out.print("Enter message ID: ");
                    String messageID = input.nextLine();

                    System.out.println(
                            Message.searchMessageByID(messageID)
                    );

                    break;

                case 4:

                    System.out.print("Enter recipient number: ");
                    String recipient = input.nextLine();

                    System.out.println(
                            Message.searchMessagesByRecipient(recipient)
                    );

                    break;

                case 5:

                    System.out.print("Enter message hash: ");
                    String hash = input.nextLine();

                    System.out.println(
                            Message.deleteMessageByHash(hash)
                    );

                    break;

                case 6:

                    System.out.println(
                            Message.displayMessageReport()
                    );

                    break;

                case 7:

                    inSubmenu = false;
                    break;

                default:

                    System.out.println(
                            "Invalid option. Please choose from 1 to 7."
                    );
            }
        }
    }

    // Safely read numeric menu options.
    private static int readInteger(
            Scanner input, String prompt) {

        while (true) {

            System.out.print(prompt);
            String value = input.nextLine();

            try {
                return Integer.parseInt(value.trim());

            } catch (NumberFormatException e) {

                System.out.println(
                        "Invalid input. Please enter a whole number."
                );
            }
        }
    }
}
