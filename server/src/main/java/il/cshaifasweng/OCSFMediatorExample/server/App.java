package il.cshaifasweng.OCSFMediatorExample.server;

import java.io.Console;
import java.io.IOException;
import java.util.Scanner;

public class App {
    private static SimpleServer server;

    public static void main(String[] args) {
        String password = promptForPassword();
        try {
            server = new SimpleServer(3000, password);
            System.out.println("Server initialized. Listening on port 3000");
            server.listen();
            System.out.println("Server is running and listening for connections");
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String promptForPassword() {
        Console console = System.console();
        if (console != null) {
            char[] passwordArray = console.readPassword("Enter database password: ");
            return new String(passwordArray);
        } else {
            // Fallback for environments where console is not available
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter database password: ");
            return scanner.nextLine();
        }
    }
}