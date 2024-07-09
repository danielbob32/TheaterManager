package il.cshaifasweng.OCSFMediatorExample.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.entities.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import il.cshaifasweng.OCSFMediatorExample.entities.Warning;

public class SimpleServer extends AbstractServer {
	private ObjectMapper objectMapper = new ObjectMapper();
	private ServerDB db;
	public SimpleServer(int port) {
		super(port);
		db = new ServerDB();
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		Message message = (Message) msg;
		String request = message.getMessage();
		try {
			message.setMessage("");
			//we got an empty message, so we will send back an error message with the error details.
			if (request.isBlank()){
				message.setMessage("Error! we got an empty message");
				client.sendToClient(message);
			}
			// we got a request to return a list of the movies we have in the system.
			else if(request.startsWith("getMovies")){
				System.out.println("in SimpleServer getMovies request");
				movieListRequest(message, client);
			}
			else{
				message.setMessage("Don't know how to handle this request");
				client.sendToClient(message);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void movieListRequest(Message message, ConnectionToClient client) throws Exception {
//      System.out.println("In SimpleServer, Handling getMovies.");
		List<Movie> movies = db.getAllMovies();
		System.out.println("got the movies from serverDB");
//      System.out.println("In SimpleServer, got back from serverDB.getAllMovies");
		String jsonMovies = objectMapper.writeValueAsString(movies);
		message.setData(jsonMovies);
		message.setMessage("movieList");
//                System.out.println("In SimpleServer, Sending the client: \n ." + jsonMovies);
		client.sendToClient(message);
	}

}
