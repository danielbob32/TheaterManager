package il.cshaifasweng.OCSFMediatorExample.server;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App
{
    private static SimpleServer server;
    public static void main( String[] args )
    {
        try {
            server = new SimpleServer(3000);
            System.out.println("Server initialized. Listening on port 3000");
            server.listen();
            System.out.println("Server is running and listening for connections");
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}