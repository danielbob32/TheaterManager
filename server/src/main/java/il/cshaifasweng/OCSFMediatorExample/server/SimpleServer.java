package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import il.cshaifasweng.OCSFMediatorExample.entities.Worker;
import il.cshaifasweng.OCSFMediatorExample.entities.*;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class SimpleServer extends AbstractServer {
	private ObjectMapper objectMapper = new ObjectMapper();
	private ServerDB db;

	public SimpleServer(int port) {
		super(port);
		try {
			this.db = new ServerDB();
		} catch (Exception e) {
			System.err.println("Failed to initialize ServerDB: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		System.out.println("Received message from client: " + msg);
		Message message = (Message) msg;
		String request = message.getMessage();
		try {
			if (request.startsWith("login")) {
				String person = request.split(":")[1];
				if (person.equals("worker")) {
					Worker worker = objectMapper.readValue(message.getData(), Worker.class);
					handleLoginRequest(worker, client);
				} else if (person.equals("customer")) {
					Customer customer = objectMapper.readValue(message.getData(), Customer.class);
					handleLoginRequest(customer, client);
				}
			} else if (request.startsWith("add")) {
				String movieData = message.getData();
				Movie movie = objectMapper.readValue(movieData, Movie.class);
				db.addMovie(movie);
				if (movie.getIsHome()) {
					String homeMovieLinkData = message.getAdditionalData();
					HomeMovieLink homeMovieLink = objectMapper.readValue(homeMovieLinkData, HomeMovieLink.class);
					db.addHomeMovie(homeMovieLink);
					Warning warning = new Warning("Movie has been added successfully");
					client.sendToClient(warning);
				} else {
					Warning warning = new Warning("Movie has been added successfully");
					client.sendToClient(warning);
				}
			} else if (request.startsWith("getMovies")){
				System.out.println("in SimpleServer getMovies request");
				movieListRequest(message, client);
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				client.sendToClient(new Warning("Error: " + e.getMessage()));
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




//		if (msg instanceof Worker || msg instanceof Customer) {
//			handleLoginRequest(msg, client);
//		} else {
//			System.out.println("Received unknown message type: " + msg.getClass().getName());
//			String msgString = msg.toString();
//			if (msgString.startsWith("#warning")) {
//				Warning warning = new Warning("Warning from server!");
//				try {
//					client.sendToClient(warning);
//					System.out.format("Sent warning to client %s\n", client.getInetAddress().getHostAddress());
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}


	private void handleLoginRequest(Object loginRequest, ConnectionToClient client) {
		Person p = (Person) loginRequest;
		boolean loginSuccess = false;
		String message = "";
		try {
			if (loginRequest instanceof Worker) {
				Worker worker = (Worker) loginRequest;
				worker = db.checkWorkerCredentials(worker.getPersonId(), worker.getPassword());
				if (worker != null) {
					p = worker;
					message = "Worker login:successful";
				} else {
					message = "Worker login:failed";
				}
			} else if (loginRequest instanceof Customer) {
				Customer customer = (Customer) loginRequest;
				loginSuccess = db.checkCustomerCredentials(customer.getPersonId());
				//message = loginSuccess ? "Customer login successful" : "Invalid customer ID";
				message = loginSuccess ? "Customer login:successful" : "Customer login:failed";
			} else {
				message = "Invalid login request type";
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = "An error occurred during login: " + e.getMessage();
		}

		try {
			Warning warning = new Warning(message);
			Message message1 = new Message(0, message, objectMapper.writeValueAsString(p));
			client.sendToClient(message1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void serverStarted() {
		super.serverStarted();
		System.out.println("Server started");
	}

	@Override
	protected void serverStopped() {
		super.serverStopped();
		System.out.println("Server stopped");
		db.close();
	}

	@Override
	protected void serverClosed() {
		super.serverClosed();
		System.out.println("Server closed");
		db.close();
	}

	protected void movieListRequest(Message message, ConnectionToClient client) throws Exception {
        System.out.println("In SimpleServer, Handling getMovies.");
		List<Movie> movies = db.getAllMovies();
		System.out.println("got the movies from serverDB");
		System.out.println("In SimpleServer, got back from serverDB.getAllMovies");
		String jsonMovies = objectMapper.writeValueAsString(movies);
		message.setData(jsonMovies);
		message.setMessage("movieList");
		System.out.println("In SimpleServer, Sending the client: \n ." + jsonMovies);
		client.sendToClient(message);

	}
}