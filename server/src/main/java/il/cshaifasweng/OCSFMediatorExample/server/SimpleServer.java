package il.cshaifasweng.OCSFMediatorExample.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleServer extends AbstractServer {
	private ObjectMapper objectMapper = new ObjectMapper();
	private ServerDB db;
	private final Map<Integer, ConnectionToClient> connectedClients = new ConcurrentHashMap<>();
	private final Object movieLock = new Object();
	private final Object ticketLock = new Object();
	private final Object ticketTabLock = new Object();
	private final Object screeningLock = new Object();
	private final Object priceChangeRequestLock = new Object();
	private final Object complaintLock = new Object();
	private final Object movieLinkLock = new Object();

	public SimpleServer(int port) {
		super(port);
		try {
			this.db = new ServerDB();
			this.objectMapper.registerModule(new Hibernate5Module());
			SchedulerService.initialize(this.db, this);
			SchedulerService.initialize(this.db, this);
		} catch (Exception e) {
			System.err.println("Failed to initialize ServerDB: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		new Thread(() -> handleClientRequest(msg, client)).start();
	}

	protected void handleClientRequest(Object msg, ConnectionToClient client) {
		Message message = (Message) msg;
		String request = message.getMessage();
		try {
			String action = request.split(":")[0];

			switch (action) {
				case "login":
					handleLoginRequest(message, client);
					break;
				case "personLogout":
					handleLogoutRequest(message);
					break;
				case "add movie":
					handleAddMovieRequest(message, client);
					break;

				case "getNotifications":
					handleGetNotificationsRequest(message, client);
					break;

				case "markNotificationAsRead":
					handleMarkNotificationAsReadRequest(message, client);
					break;

				case "getMovies":
					synchronized (movieLock) {
						movieListRequest(message, client);
					}
					break;

				case "updatePrice":
					synchronized (movieLock) {
						handleUpdatePriceRequest(message, client);
					}
					break;

				case "deleteMovie":
					synchronized (movieLock) {
						handleDeleteMovieRequest(message, client);
					}
					break;

				case "updateMovie":
					synchronized (movieLock) {
						handleUpdateMovieRequest(message, client);
					}
					break;

				case "addScreening":
					synchronized (screeningLock) {
						handleAddScreeningRequest(message, client);
					}
					break;

				case "deleteScreening":
					synchronized (screeningLock) {
						handleDeleteScreeningRequest(message, client);
					}
					break;

				case "createPriceChangeRequest":
					synchronized (priceChangeRequestLock) {
						handleCreatePriceChangeRequest(message, client);
					}
					break;

				case "getPriceChangeRequests":
					synchronized (priceChangeRequestLock) {
						handleGetPriceChangeRequests(message, client);
					}
					break;

				case "approvePriceChangeRequest":
					synchronized (priceChangeRequestLock) {
						handleApprovePriceChangeRequest(message, client);
					}
					break;

				case "denyPriceChangeRequest":
					synchronized (priceChangeRequestLock) {
						handleDenyPriceChangeRequest(message, client);
					}
					break;

				case "getSeatAvailability":
					synchronized (ticketLock) {
						handleGetSeatAvailability(message, client);
					}
					break;

				case "checkTicketTab":
					synchronized (ticketTabLock) {
						handleCheckTicketTab(message, client);
					}
					break;

				case "processPayment":
					synchronized (ticketLock) {
						handleProcessPaymentRequest(message.getData(), client);
					}
					break;

				case "getScreeningById":
					synchronized (screeningLock) {
						handleGetScreeningByIdRequest(message, client);
					}
					break;

				case "purchaseTicketTab":
					synchronized (ticketTabLock) {
						handlePurchaseTicketTabRequest(message.getData(), client);
					}
					break;

				case "purchaseLink":
					synchronized (movieLinkLock) {
						handlePurchaseLinkRequest(message.getData(), client);
					}
					break;

				case "getCinemaList":
					handleGetCinemaList(client);
					break;

				case "generateReport":
					handleGenerateReportRequest(message.getData(), client);
					break;

				case "getMovieById":
					synchronized (movieLock) {
						handleGetMovieByIdRequest(message, client);
					}
					break;

				// Yonathan's Cases:
				case "fetchUserBookings":
					handleFetchUserBookings(message, client);
					break;

				case "cancelPurchase":
					handleCancelBooking(message, client);
					break;

				case "submitComplaint":
					synchronized (complaintLock) {
						handleSubmitComplaint(message, client);
					}
					break;

				case "fetchAllComplaints":
					synchronized (complaintLock) {
						handleFetchAllComplaints(message, client);
					}
					break;

				case "fetchCustomerComplaints":
					synchronized (complaintLock) {
						handleFetchCustomerComplaints(message, client);
					}
					break;

				case "fetchComplaints":
					synchronized (complaintLock) {
						handleFetchComplaints(message, client);
					}
					break;

				case "respondToComplaint":
					synchronized (complaintLock) {
						handleRespondToComplaint(message, client);
					}
					break;

				case "updateComplaint":
					synchronized (complaintLock) {
						handleUpdateComplaint(message, client);
					}
					break;

				case "fetchRandomCustomer":
					handleFetchRandomCustomer(message, client);
					break;

				default:
					client.sendToClient(new Warning("Unknown request type"));
			}
		} catch (IOException e) {
			handleIOException(e, client);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void handleLogoutRequest(Message message) {
		System.out.println("IN SIMPLE SERVER, GOT LOGOUT REQUEST, AFTER LOGIN CONNECTED IDS:");
		for(Map.Entry<Integer, ConnectionToClient> entry : connectedClients.entrySet()) {
			System.out.println(entry.getKey());
		}
		int personId = Integer.parseInt(message.getData());

		// Update the person's login status
		db.updatePersonLoginStatus(personId, false);
        connectedClients.remove(personId);
		System.out.println("IN SIMPLE SERVER, GOT LOGOUT REQUEST, AFTER LOGIN CONNECTED IDS:");
		for(Map.Entry<Integer, ConnectionToClient> entry : connectedClients.entrySet()) {
			System.out.println(entry.getKey());
		}
	}


	private void handleLoginRequest(Message message, ConnectionToClient client) throws IOException {
		String[] loginParts = message.getMessage().split(":", 2);
		String person = loginParts.length > 1 ? loginParts[1] : "";
		System.out.println("IN SIMPLE SERVER, GOT LOGIN REQUEST, CURRENTLY CONNECTED IDS:");
		for(Map.Entry<Integer, ConnectionToClient> entry : connectedClients.entrySet()) {
			System.out.println(entry.getKey());
		}
		switch (person) {
			case "worker":
				Worker worker = objectMapper.readValue(message.getData(), Worker.class);
				processLogin(worker, client);
				break;
			case "customer":
				Customer customer = objectMapper.readValue(message.getData(), Customer.class);
				processLogin(customer, client);
				break;
			default:
				client.sendToClient(new Warning("Unknown login type"));
		}
		System.out.println("IN SIMPLE SERVER, GOT LOGIN REQUEST, AFTER LOGIN CONNECTED IDS:");
		for(Map.Entry<Integer, ConnectionToClient> entry : connectedClients.entrySet()) {
			System.out.println(entry.getKey());
		}
	}


	// Handles the add movie request
	private void handleAddMovieRequest(Message message, ConnectionToClient client) throws IOException {
		String movieData = message.getData();
		Movie movie = objectMapper.readValue(movieData, Movie.class);
		boolean movieExists = db.checkMovieExists(movie.getEnglishName(), movie.getHebrewName());
		if (movieExists) {
			client.sendToClient(new Message(0, "Movie add:failed", "Movie already exists"));
		} else {
			boolean addSuccess = db.addMovie(movie);
			if (addSuccess) {
				client.sendToClient(new Message(0, "Movie add:success"));
				sendToAllClients(new Message(0, "new_movie_added", objectMapper.writeValueAsString(movie)));
			} else {
				client.sendToClient(new Message(0, "Movie add:failed"));
			}
		}
	}

	// Handles the get notifications request
	private void handleGetNotificationsRequest(Message message, ConnectionToClient client) throws IOException {
		int customerId = Integer.parseInt(message.getData());
		List<Notification> notifications = db.getUnreadNotificationsForCustomer(customerId);
		client.sendToClient(new Message(0, "notifications", objectMapper.writeValueAsString(notifications)));
	}

	// Handles the mark notification as read request
	private void handleMarkNotificationAsReadRequest(Message message, ConnectionToClient client) throws IOException {
		String[] parts = message.getData().split(",");
		int notificationId = Integer.parseInt(parts[0]);
		int customerId = Integer.parseInt(parts[1]);
		db.markNotificationAsRead(notificationId, customerId);
		client.sendToClient(new Message(0, "notificationMarkedAsRead"));
	}

	// Handles the update price request
	private void handleUpdatePriceRequest(Message message, ConnectionToClient client) throws IOException {
		String[] parts = message.getData().split(",");
		int movieId = Integer.parseInt(parts[0]);
		String movieType = parts[1];
		int newPrice = Integer.parseInt(parts[2]);
		boolean success = db.updateMoviePrice(movieId, movieType, newPrice);
		if (success) {
			message.setMessage("Price:success");
		} else {
			message.setMessage("Price:failed");
		}
		client.sendToClient(message);
	}

	// Handles the delete movie request
	private void handleDeleteMovieRequest(Message message, ConnectionToClient client) throws IOException {
		String deleteMovieType = message.getMessage().split(":")[1];
		int movieId = Integer.parseInt(message.getData());
		boolean deleteSuccess = db.deleteMovie(movieId, deleteMovieType);
		if (deleteSuccess) {
			client.sendToClient(new Message(0, "Movie delete:success"));
		} else {
			client.sendToClient(new Message(0, "Movie delete:failed", "Movie is currently in use"));
		}
	}

	// Handles the update movie request
	private void handleUpdateMovieRequest(Message message, ConnectionToClient client) throws IOException {
		Movie movieToUpdate = objectMapper.readValue(message.getData(), Movie.class);
		boolean updateSuccess = db.updateMovie(movieToUpdate);
		if (updateSuccess) {
			client.sendToClient(new Message(0, "Movie update:success"));
		} else {
			client.sendToClient(new Message(0, "Movie update:failed", "Error occurred"));
		}
	}

	// Handles the add screening request
	private void handleAddScreeningRequest(Message message, ConnectionToClient client) throws IOException {
		Screening newScreening = objectMapper.readValue(message.getData(), Screening.class);
		boolean addSuccess = db.addScreening(newScreening);
		if (addSuccess) {
			client.sendToClient(new Message(0, "Screening add:success"));
		} else {
			client.sendToClient(new Message(0, "Screening add:failed", "Hall is not available at the specified time"));
		}
	}

	// Handles the delete screening request
	private void handleDeleteScreeningRequest(Message message, ConnectionToClient client) throws IOException {
		int screeningId = Integer.parseInt(message.getData());
		boolean deleteScreeningSuccess = db.deleteScreening(screeningId);
		if (deleteScreeningSuccess) {
			client.sendToClient(new Message(0, "Screening delete:success"));
		} else {
			client.sendToClient(new Message(0, "Screening delete:failed", "Failed to delete screening"));
		}
	}

	// Handles the create price change request
	private void handleCreatePriceChangeRequest(Message message, ConnectionToClient client) throws IOException {
		PriceChangeRequest priceRequest = objectMapper.readValue(message.getData(), PriceChangeRequest.class);
		db.createPriceChangeRequest(priceRequest);
		client.sendToClient(new Message(0, "Price change request created successfully"));
	}

	// Handles the get price change requests
	private void handleGetPriceChangeRequests(Message message, ConnectionToClient client) throws IOException {
		List<PriceChangeRequest> requests = db.getPriceChangeRequests();
		String requestsJson = objectMapper.writeValueAsString(requests);
		client.sendToClient(new Message(0, "priceChangeRequests", requestsJson));
	}

	// Handles the approve price change request
	private void handleApprovePriceChangeRequest(Message message, ConnectionToClient client) throws IOException {
		int requestId = Integer.parseInt(message.getData());
		boolean priceSuccess = db.updatePriceChangeRequestStatus(requestId, true);
		if (priceSuccess) {
			PriceChangeRequest updatedRequest = db.getPriceChangeRequestById(requestId);
			String updatedRequestJson = objectMapper.writeValueAsString(updatedRequest);
			client.sendToClient(new Message(0, "Price change request approved and price updated successfully", updatedRequestJson));
		} else {
			client.sendToClient(new Message(0, "Failed to approve price change request. It may already be approved."));
		}
	}

	// Handles the deny price change request
	private void handleDenyPriceChangeRequest(Message message, ConnectionToClient client) throws IOException {
		int requestId = Integer.parseInt(message.getData());
		boolean priceSuccess = db.updatePriceChangeRequestStatus(requestId, false);
		if (priceSuccess) {
			PriceChangeRequest updatedRequest = db.getPriceChangeRequestById(requestId);
			String updatedRequestJson = objectMapper.writeValueAsString(updatedRequest);
			client.sendToClient(new Message(0, "Price change request denied", updatedRequestJson));
		} else {
			client.sendToClient(new Message(0, "Failed to deny price change request. It may already be approved."));
		}
	}

	// Handles the get movie by ID request
	private void handleGetMovieByIdRequest(Message message, ConnectionToClient client) throws IOException {
		int movieId = message.getExtraData();
		Movie movie = db.getMovieById(movieId);
		String jsonMovie = objectMapper.writeValueAsString(movie);
		message.setData(jsonMovie);
		message.setMessage("movie refreshed");
		client.sendToClient(message);
	}


	// Handles I/O exceptions
	private void handleIOException(IOException e, ConnectionToClient client) {
		System.out.println("DEBUG: Error processing message - " + e.getMessage());
		e.printStackTrace();
		try {
			client.sendToClient(new Warning("Server error: " + e.getMessage()));
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}


//	protected void handleClientRequest(Object msg, ConnectionToClient client) {
//		Message message = (Message) msg;
////		System.out.println("Received message from client: " + msg);
//		String request = message.getMessage();
//		try {
//			String action = request.split(":")[0];
//
//			switch (action) {
//				case "login":
//					String[] loginParts = request.split(":", 2);
//					String person = loginParts.length > 1 ? loginParts[1] : "";
//
//					switch (person) {
//						case "worker":
//							Worker worker = objectMapper.readValue(message.getData(), Worker.class);
//							handleLoginRequest(worker, client);
//							break;
//						case "customer":
//							Customer customer = objectMapper.readValue(message.getData(), Customer.class);
//							handleLoginRequest(customer, client);
//							break;
//						default:
//							client.sendToClient(new Warning("Unknown login type"));
//					}
//					break;
//
//				case "add movie":
//					String movieData = message.getData();
//					Movie movie = objectMapper.readValue(movieData, Movie.class);
//					boolean movieExists = db.checkMovieExists(movie.getEnglishName(), movie.getHebrewName());
//					if (movieExists) {
//						client.sendToClient(new Message(0, "Movie add:failed", "Movie already exists"));
//					} else {
//						boolean add_success = db.addMovie(movie);
//						if (add_success) {
//							client.sendToClient(new Message(0, "Movie add:success"));
//							// Notify all connected clients about the new movie
//							sendToAllClients(new Message(0, "new_movie_added", objectMapper.writeValueAsString(movie)));
//						} else {
//							client.sendToClient(new Message(0, "Movie add:failed"));
//						}
//					}
//					break;
//				case "getNotifications":
//					int customerId = Integer.parseInt(message.getData());
//					List<Notification> notifications = db.getUnreadNotificationsForCustomer(customerId);
//					client.sendToClient(new Message(0, "notifications", objectMapper.writeValueAsString(notifications)));
//					break;
//
//				case "markNotificationAsRead":
//					String[] parts2 = message.getData().split(",");
//					int notificationId = Integer.parseInt(parts2[0]);
//					int customerIdForMarkRead = Integer.parseInt(parts2[1]);
//					db.markNotificationAsRead(notificationId, customerIdForMarkRead);
//					client.sendToClient(new Message(0, "notificationMarkedAsRead"));
//					break;
//
//				case "getMovies":
//					System.out.println("in SimpleServer getMovies request");
//					movieListRequest(message, client);
//					break;
//
//				case "updatePrice":
//					System.out.println("in SimpleServer updatePrice request");
//					String[] parts = message.getData().split(",");
//					int movieId = Integer.parseInt(parts[0]);
//					String movieType = parts[1];
//					int newPrice = Integer.parseInt(parts[2]);
//					boolean success = db.updateMoviePrice(movieId, movieType, newPrice);
//					if (success) {
//						System.out.println("Price updated successfull in simple server");
//						message.setMessage("Price:success");
//					} else {
//						message.setMessage("Price:failed");
//					}
//					client.sendToClient(message);
//					break;
//
//				case "deleteMovie":
//					String deleteMovieType = request.split(":")[1];
//					int movieId2 = Integer.parseInt(message.getData());
//					boolean deleteSuccess = db.deleteMovie(movieId2, deleteMovieType);
//					if (deleteSuccess) {
//						client.sendToClient(new Message(0, "Movie delete:success"));
//					} else {
//						client.sendToClient(new Message(0, "Movie delete:failed", "Movie is currently in use"));
//					}
//					break;
//
//				case "updateMovie":
//					Movie movie_to_update = objectMapper.readValue(message.getData(), Movie.class);
//					boolean updateSuccess = db.updateMovie(movie_to_update);
//					if (updateSuccess) {
//						client.sendToClient(new Message(0, "Movie update:success"));
//					} else {
//						client.sendToClient(new Message(0, "Movie update:failed", "error occured"));
//					}
//					break;
//
//				case "addScreening":
//					Screening newScreening = objectMapper.readValue(message.getData(), Screening.class);
////					System.out.println("Received screening to add: " + newScreening);
//					boolean addSuccess = db.addScreening(newScreening);
//					if (addSuccess) {
//						client.sendToClient(new Message(0, "Screening add:success"));
//					} else {
//						client.sendToClient(new Message(0, "Screening add:failed", "Hall is not available at the specified time"));
//					}
//					break;
//
//				case "deleteScreening":
//					int screeningId = Integer.parseInt(message.getData());
//					boolean deleteScreeningSuccess = db.deleteScreening(screeningId);
//					if (deleteScreeningSuccess) {
//						System.out.println("Screening deleted successfully");
//						client.sendToClient(new Message(0, "Screening delete:success"));
//					} else {
//						System.out.println("Failed to delete screening");
//						client.sendToClient(new Message(0, "Screening delete:failed", "Hall is not available at the specified time"));
//					}
//					break;
//
//				case "createPriceChangeRequest":
//					PriceChangeRequest price_request = objectMapper.readValue(message.getData(), PriceChangeRequest.class);
//					db.createPriceChangeRequest(price_request);
//					client.sendToClient(new Message(0, "Price change request created successfully"));
//					break;
//
//				case "getPriceChangeRequests":
//					List<PriceChangeRequest> requests = db.getPriceChangeRequests();
//					String requestsJson = objectMapper.writeValueAsString(requests);
//					client.sendToClient(new Message(0, "priceChangeRequests", requestsJson));
//					break;
//
//				case "approvePriceChangeRequest":
//					int requestId = Integer.parseInt(message.getData());
////					System.out.println("Approving price change request: " + requestId);
//					boolean price_success = db.updatePriceChangeRequestStatus(requestId, true);
//					if (price_success) {
//						PriceChangeRequest updatedRequest = db.getPriceChangeRequestById(requestId);
//						String updatedRequestJson = objectMapper.writeValueAsString(updatedRequest);
//						client.sendToClient(new Message(0, "Price change request approved and price updated successfully", updatedRequestJson));
//					} else {
//						client.sendToClient(new Message(0, "Failed to approve price change request. It may already be approved."));
//					}
//					break;
//
//				case "denyPriceChangeRequest":
//					int requestId2 = Integer.parseInt(message.getData());
//					boolean price_success2 = db.updatePriceChangeRequestStatus(requestId2, false);
//					if (price_success2) {
//						PriceChangeRequest updatedRequest = db.getPriceChangeRequestById(requestId2);
//						String updatedRequestJson = objectMapper.writeValueAsString(updatedRequest);
//						client.sendToClient(new Message(0, "Price change request denied", updatedRequestJson));
//					} else {
//						client.sendToClient(new Message(0, "Failed to deny price change request. It may already be approved."));
//					}
//					break;
//
//				case "getSeatAvailability":
//					System.out.println("in SimpleServer getSeatAvailability request");
//					handleGetSeatAvailability(message, client);
//					break;
//
//				case "checkTicketTab":
//					System.out.println("in SimpleServer checkTicketTab request");
//					handleCheckTicketTab(message, client);
//					break;
//
//				case "processPayment":
//					System.out.println("in SimpleServer processPayment request");
//					handleProcessPayment(message.getData(), client);
//					break;



//
//				case "getScreeningById":
//					System.out.println("in SimpleServer getScreeningById request");
//					handleGetScreeningById(message, client);
//					break;
//
//				case "purchaseTicketTab":
//					System.out.println("in SimpleServer purchaseTicketTab request");
//					handlePurchaseTicketTab(message.getData(), client);
//					break;
//
//				case "purchaseLink":
//					System.out.println("In SimpleServer, handling purchaseLink request.");
//					handlePurchaseLink(message.getData(), client); // Pass message data as String
//					break;
//				case "getCinemaList":
//					handleGetCinemaList(client);
//					break;
//
//				case "generateReport":
//					System.out.println("Handling generate report request");
//					handleGenerateReport(message.getData(), client);
//					break;
//
//				case "getMovieById":
//					int movie_id = message.getExtraData();
//					Movie movie3 = db.getMovieById(movie_id);
//					String jsonMovie = objectMapper.writeValueAsString(movie3);
//					message.setData(jsonMovie);
//					message.setMessage("movie refreshed");
//					client.sendToClient(message);
//					break;
//				// YONATHAN`S CASES:
//				case "fetchUserBookings":
//					System.out.println("In SimpleServer handleFetchUserBookings request");
//					handleFetchUserBookings(message, client);
//					break;
//				case "cancelPurchase":
//					System.out.println("In SimpleServer handleCancelBooking request");
//					handleCancelBooking(message, client);
//					break;
//				case "submitComplaint":
//					System.out.println("In SimpleServer submitComplaint request");
//					handleSubmitComplaint(message, client);
//					break;
//				case "fetchAllComplaints":
//					System.out.println("In SimpleServer fetchAllComplaints request");
//					handleFetchAllComplaints(message, client);
//					break;
//				case "fetchCustomerComplaints":
//					System.out.println("In SimpleServer fetchCustomerComplaints request");
//					handleFetchCustomerComplaints(message, client);
//					break;
//				case "fetchComplaints":
//					System.out.println("In SimpleServer fetchComplaints request");
//					handleFetchComplaints(message, client);
//					break;
//				case "respondToComplaint":
//					System.out.println("In SimpleServer respondToComplaint request");
//					handleRespondToComplaint(message, client);
//					break;
//				case "updateComplaint":
//					System.out.println("In SimpleServer updateComplaint request");
//					handleUpdateComplaint(message, client);
//					break;
//				case "fetchRandomCustomer":
//					System.out.println("In SimpleServer fetchRandomCustomer request");
//					handleFetchRandomCustomer(message, client);
//					break;
//
//				default:
//					client.sendToClient(new Warning("Unknown request type"));
//			}
//		} catch (IOException e) {
//			System.out.println("DEBUG: Error processing message - " + e.getMessage());
//			e.printStackTrace();
//			try {
//				client.sendToClient(new Warning("Server error: " + e.getMessage()));
//			} catch (IOException ioException) {
//				ioException.printStackTrace();
//			}
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}

	protected void handleGetSeatAvailability(Message message, ConnectionToClient client) throws Exception {
		int screeningId = Integer.parseInt(message.getData());
		List<Seat> seats = db.getSeatsForScreening(screeningId);

		Message response = new Message(0, "seatAvailabilityResponse", objectMapper.writeValueAsString(seats));
		response.setAdditionalData(String.valueOf(screeningId));
		client.sendToClient(response);
	}

	protected void handleProcessPaymentRequest(String data, ConnectionToClient client) throws Exception {
		try {
			// open json
			ObjectNode dataNode = (ObjectNode) objectMapper.readTree(data);
			String name = dataNode.get("name").asText();
			int id = dataNode.get("id").asInt();
			String email = dataNode.get("email").asText();
			String paymentMethod = dataNode.get("paymentMethod").asText();
			String paymentNum = dataNode.get("paymentNum").asText();
			int cinemaPrice = dataNode.get("cinemaPrice").asInt();
			int screeningId = dataNode.get("screeningId").asInt();
			List<Integer> seatIds = new ArrayList<>();
			JsonNode seatsIdNode = dataNode.get("seatIds");
			Iterator<String> seatIdFieldNames = seatsIdNode.fieldNames();
			while (seatIdFieldNames.hasNext()) {
				String seatIdStr = seatIdFieldNames.next();
				seatIds.add(Integer.parseInt(seatIdStr));
			}

			Booking newBooking = null;
			TicketTab ticketTab = null;

			if (paymentMethod.equals("creditCard")) {
				newBooking = db.purchaseTicketWithCreditCard(name, id, email, paymentNum, cinemaPrice, screeningId, seatIds);
			} else if (paymentMethod.equals("ticketTab")) {
				newBooking = db.purchaseTicketWithTicketTab(name, id, email, paymentNum, screeningId, seatIds);
				ticketTab = db.getTicketTabById(Integer.parseInt(paymentNum));
			}

			if (newBooking != null) {
				System.out.println("created booking successfully " + newBooking.getBookingId());

				int ticketNum = newBooking.getProducts().size();
				StringBuilder seatsString = new StringBuilder();	// building string of the seats
				for (Product product : newBooking.getProducts()) {
					if (product instanceof Ticket) {
						String row = String.valueOf(((Ticket) product).getSeat().getSeatRow());
						String number = String.valueOf(((Ticket) product).getSeat().getSeatNumber());
						seatsString.append(row).append("-").append(number).append(", ");
					}
				}
				String seats = seatsString.substring(0, seatsString.length() - 2);
				Screening screening = db.getScreeningById(screeningId);

//				TicketPurchaseInfo purchaseInfo = new TicketPurchaseInfo(screening, newBooking, seats, paymentMethod, paymentNum);

				ObjectNode bookingNode = objectMapper.createObjectNode();
				bookingNode.put("bookingId", newBooking.getBookingId());
				bookingNode.put("name", name);
				bookingNode.put("purchaseTime", newBooking.getPurchaseTime().getTime());
				bookingNode.put("ticketNum", ticketNum);
				bookingNode.put("seats", seats);
				bookingNode.put("movie", screening.getMovie().getEnglishName());
				bookingNode.put("cinema", screening.getCinema().getCinemaName());
				bookingNode.put("movieHall", screening.getHall().getHallNumber());
				bookingNode.put("screeningTime", screening.getTime().getTime());
				bookingNode.put("paymentMethod", paymentMethod);

				if (paymentMethod.equals("ticketTab")) {
					if (ticketTab != null) {
						bookingNode.put("amountLeft", ticketTab.getAmount());
					} else {
						bookingNode.put("amountLeft", 0);
					}
				}
				String jsonBooking = objectMapper.writeValueAsString(bookingNode);
				client.sendToClient(new Message(0,"addedTicketsSuccessfully", jsonBooking));

			} else {
				client.sendToClient(new Message(0, "addingTicketsFailed"));
			}
		} catch (Exception e) {
			client.sendToClient(new Message(0, "failedSendingBookingInfo"));
		}
	}

	protected void handleCheckTicketTab(Message message, ConnectionToClient client) throws IOException {
		String[] data = message.getData().split(",");
		int ticketTabId = Integer.parseInt(data[0]);
		int customerId = Integer.parseInt(data[1]);
		int seatsNum = Integer.parseInt(data[2]);
		boolean isValid = db.checkTicketTabValidity(ticketTabId, customerId, seatsNum);
		System.out.println("Ticket tab " + ticketTabId + " for customer " + customerId + " validity: " + isValid);
		Message response = new Message(0, "ticketTabResponse", String.valueOf(isValid));
		response.setAdditionalData(String.valueOf(ticketTabId));
		client.sendToClient(response);
	}

	protected void handleGetScreeningByIdRequest(Message message, ConnectionToClient client) throws IOException {
		System.out.println("getting screening #" + message.getData());
		Screening screening = db.getScreeningById(Integer.parseInt(message.getData()));
		String jsonScreening = objectMapper.writeValueAsString(screening);
		message.setData(jsonScreening);
		message.setMessage("screeningById");
		client.sendToClient(message);
	}

	protected void handlePurchaseTicketTabRequest(String data, ConnectionToClient client) throws IOException {
		try {
			ObjectNode dataNode = (ObjectNode) objectMapper.readTree(data);
			int id = dataNode.get("id").asInt();
			String name = dataNode.get("name").asText();
			String email = dataNode.get("email").asText();
			String creditCard = dataNode.get("creditCard").asText();

			Booking newBooking = db.purchaseTicketTab(name, id, email, creditCard);

			if (newBooking != null) {
				ObjectNode bookingNode = objectMapper.createObjectNode();
				bookingNode.put("bookingId", newBooking.getBookingId());
				bookingNode.put("name", name);
				bookingNode.put("purchaseTime", newBooking.getPurchaseTime().getTime());
				bookingNode.put("ticketTabId", newBooking.getTicketTabId());

				String jsonBooking = objectMapper.writeValueAsString(bookingNode);
				client.sendToClient(new Message(0,"purchasedTicketTabSuccessfully", jsonBooking));
			} else {
			client.sendToClient(new Message(0, "purchasingTicketTabFailed"));
			}
		} catch (Exception e) {
			client.sendToClient(new Message(0, "failedSendingBookingInfo"));
		}
    }


protected void handlePurchaseLinkRequest(String data, ConnectionToClient client) {
    System.out.println("DEBUG: Processing purchase link");
    try {
        ObjectNode dataNode = (ObjectNode) objectMapper.readTree(data);
        String name = dataNode.has("name") ? dataNode.get("name").asText() : "";
        int id = dataNode.has("id") ? dataNode.get("id").asInt() : 0;
        String email = dataNode.has("email") ? dataNode.get("email").asText() : "";
        String creditCard = dataNode.has("creditCard") ? dataNode.get("creditCard").asText() : "";
        int movieId = dataNode.has("movieId") ? dataNode.get("movieId").asInt() : 0;
        String selectedDate = dataNode.has("selectedDate") ? dataNode.get("selectedDate").asText() : "";
        String selectedTime = dataNode.has("selectedTime") ? dataNode.get("selectedTime").asText() : "";
        int totalPrice = dataNode.has("totalPrice") ? dataNode.get("totalPrice").asInt() : 0;

//        System.out.println("DEBUG: Purchase data - Name: " + name + ", ID: " + id + ", Email: " + email);

        Movie movie = db.getMovieById(movieId);
        if (movie == null) {
            client.sendToClient(new Message(0, "Movie not found"));
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date openTime = sdf.parse(selectedDate + " " + selectedTime);

		// Check if the selected time is in the past
		Date currentTime = new Date();
		if (currentTime.after(openTime)) {
			client.sendToClient(new Message(0, "purchasingHomeMovieLinkFailed", "Cannot purchase link for a past date and time."));
			return;
		}

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(openTime);
        calendar.add(Calendar.MINUTE, movie.getDuration());
        calendar.add(Calendar.MINUTE, movie.getDuration());
        Date closeTime = calendar.getTime();

        HomeMovieLink link = new HomeMovieLink(openTime, closeTime, false, HomeMovieLink.generateRandomLink("www.Theater.link."),
                                               id, totalPrice, true, new Date());
        link.setMovie(movie);
		// // Simulating a purchase for a movie starting in 30 seconds and ending 60 seconds later
		// Calendar calendar1 = Calendar.getInstance();
		// Date now = calendar1.getTime();
		// calendar1.add(Calendar.SECOND, 30);
		// Date openTime1 = calendar1.getTime();
		// calendar1.add(Calendar.SECOND, 30);
		// Date closeTime1 = calendar1.getTime();

		// HomeMovieLink testLink = new HomeMovieLink(openTime1, closeTime1, false, "test_link", id, 10, true, now);
		// testLink.setMovie(movie);
		// Customer customertest = null;
		// // Simulating a purchase for a movie starting in 30 seconds and ending 60 seconds later
		// Calendar calendar1 = Calendar.getInstance();
		// Date now = calendar1.getTime();
		// calendar1.add(Calendar.SECOND, 30);
		// Date openTime1 = calendar1.getTime();
		// calendar1.add(Calendar.SECOND, 30);
		// Date closeTime1 = calendar1.getTime();

		// HomeMovieLink testLink = new HomeMovieLink(openTime1, closeTime1, false, "test_link", id, 10, true, now);
		// testLink.setMovie(movie);
		// Customer customertest = null;
        Customer customer = null;


        try {
			// customertest = new Customer("test", "test", 1);
			// db.addPerson(customertest);
			// customertest = new Customer("test", "test", 1);
			// db.addPerson(customertest);
            customer = (Customer) db.getPersonById(id);
            System.out.println("DEBUG: Existing customer found: " + (customer != null));
            if (customer == null) {
                customer = new Customer(name, email, id);
                db.addPerson(customer);
                System.out.println("DEBUG: New customer added with ID: " + customer.getPersonId());
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Error handling customer: " + e.getMessage());
            e.printStackTrace();
        }

		
		
        Booking newBooking = db.purchaseHomeMovieLink(name, id, email, creditCard, link);
        System.out.println("DEBUG: New booking created: " + (newBooking != null));
		// Booking testBooking = db.purchaseHomeMovieLink("test", 1, "test", "test", testLink);
		SchedulerService.scheduleHomeLinkAvailability(link);
		System.out.println("DEBUG: Test link scheduled. Open time: " + openTime + ", Close time: " + closeTime);

		// Booking testBooking = db.purchaseHomeMovieLink("test", 1, "test", "test", testLink);
		SchedulerService.scheduleHomeLinkAvailability(link);
		System.out.println("DEBUG: Test link scheduled. Open time: " + openTime + ", Close time: " + closeTime);


        if (newBooking != null) {
            ObjectNode bookingNode = objectMapper.createObjectNode();
            bookingNode.put("bookingId", newBooking.getBookingId());
            bookingNode.put("name",name);
            bookingNode.put("purchaseTime", newBooking.getPurchaseTime().getTime());
            bookingNode.put("movie", movie.getEnglishName());
            bookingNode.put("openTime", link.getOpenTime().getTime());
            bookingNode.put("closeTime", link.getCloseTime().getTime());
            bookingNode.put("watchLink", link.getWatchLink());
            bookingNode.put("totalPrice", link.getPrice());
			System.out.println("DEBUG: Purchase link successful");
			System.out.println("DEBUG: Purchase link successful");

            String jsonBooking = objectMapper.writeValueAsString(bookingNode);
            client.sendToClient(new Message(0, "purchasedHomeMovieLinkSuccessfully", jsonBooking));
        } else {
            client.sendToClient(new Message(0, "purchasingHomeMovieLinkFailed"));
        }
		// if (testBooking != null) {
		// 	ObjectNode bookingNode = objectMapper.createObjectNode();
		// 	bookingNode.put("bookingId", testBooking.getBookingId());
		// 	bookingNode.put("name", "test");
		// 	bookingNode.put("purchaseTime", testBooking.getPurchaseTime().getTime());
		// 	bookingNode.put("movie", movie.getEnglishName());
		// 	bookingNode.put("openTime", testLink.getOpenTime().getTime());
		// 	bookingNode.put("closeTime", testLink.getCloseTime().getTime());
		// 	bookingNode.put("watchLink", testLink.getWatchLink());
		// 	bookingNode.put("totalPrice", testLink.getPrice());
		// 	System.out.println("DEBUG: Purchase link successful");
		// 	// Schedule availability and notifications
		// 	SchedulerService.scheduleHomeLinkAvailability(testLink);
		// 	System.out.println("DEBUG: Scheduled link availability");
		// 	String jsonBooking = objectMapper.writeValueAsString(bookingNode);
		// 	client.sendToClient(new Message(0, "purchasedHomeMovieLinkSuccessfully", jsonBooking));
		// } else {
		// 	client.sendToClient(new Message(0, "purchasingHomeMovieLinkFailed"));
		// }

		// if (testBooking != null) {
		// 	ObjectNode bookingNode = objectMapper.createObjectNode();
		// 	bookingNode.put("bookingId", testBooking.getBookingId());
		// 	bookingNode.put("name", "test");
		// 	bookingNode.put("purchaseTime", testBooking.getPurchaseTime().getTime());
		// 	bookingNode.put("movie", movie.getEnglishName());
		// 	bookingNode.put("openTime", testLink.getOpenTime().getTime());
		// 	bookingNode.put("closeTime", testLink.getCloseTime().getTime());
		// 	bookingNode.put("watchLink", testLink.getWatchLink());
		// 	bookingNode.put("totalPrice", testLink.getPrice());
		// 	System.out.println("DEBUG: Purchase link successful");
		// 	// Schedule availability and notifications
		// 	SchedulerService.scheduleHomeLinkAvailability(testLink);
		// 	System.out.println("DEBUG: Scheduled link availability");
		// 	String jsonBooking = objectMapper.writeValueAsString(bookingNode);
		// 	client.sendToClient(new Message(0, "purchasedHomeMovieLinkSuccessfully", jsonBooking));
		// } else {
		// 	client.sendToClient(new Message(0, "purchasingHomeMovieLinkFailed"));
		// }

    } catch (Exception e) {
        System.out.println("DEBUG: Error in handlePurchaseLink: " + e.getMessage());
        e.printStackTrace();
        try {
            client.sendToClient(new Message(0, "failedProcessingPurchaseLink", e.getMessage()));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}


	private void processLogin(Object loginRequest, ConnectionToClient client) {
		System.out.println("Handling login request");
		Person p = (Person) loginRequest;
		String message = "";
		if(isAlreadyLoggedIn(p))
		{
			try {
				client.sendToClient(new Message(0, "Person login:failed"));
			}catch(Exception e) {
				System.out.println("Couldn't send an 'already logged in message' to client");
				e.printStackTrace();
			}
			return;
		}
		try {
			if (loginRequest instanceof Worker) {
				System.out.println("Worker login request");
				Worker worker = (Worker) loginRequest;
				worker = db.checkWorkerCredentials(worker.getPersonId(), worker.getPassword());
				System.out.println("Worker login: " + (worker != null));
				System.out.println("Worker: " + worker);
				if (worker != null) {
					connectedClients.put(worker.getPersonId(), client);
					if (worker instanceof CinemaManager) {
						CinemaManager manager = (CinemaManager) worker;
						p = manager;

						message = "Cinema manager login:successful";
						System.out.println("Inside if: Cinema manager login successful");
					} else {
						p = worker;
						message = "Worker login:successful";
						System.out.println("Inside if: Worker login successful");
					}
				} else {
					message = "Worker login:failed";
					System.out.println("Inside else: Worker login failed");
				}
				
			} else if (loginRequest instanceof Customer) {
				Customer customer = (Customer) loginRequest;
				customer = db.checkCustomerCredentials(customer.getPersonId());
				message = customer!=null ? "Customer login:successful" : "Customer login:failed";
				if(customer!=null) {
					connectedClients.put(customer.getPersonId(), client);
					p = customer;
				}
			} else {
				message = "Invalid login request type";
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = "An error occurred during login: " + e.getMessage();
		}

		try {
			System.out.println("About to serialize the worker/customer object: " + p);
			String jsonString = objectMapper.writeValueAsString(p);
//			System.out.println("Serialization successful, JSON: " + jsonString);
			Message message1 = new Message(0, message, jsonString);
			client.sendToClient(message1);
			System.out.println("Message sent to client successfully");
		} catch (Exception e) {
			System.err.println("Error during serialization or sending: " + e.getMessage());
			e.printStackTrace();
		}
		
	}

	private boolean isAlreadyLoggedIn(Person person) {
		return connectedClients.containsKey(person.getPersonId());
	}

	@Override
	protected synchronized void clientDisconnected(ConnectionToClient client) {
		Integer personId = null;

		// Find the person associated with this client
		for (Map.Entry<Integer, ConnectionToClient> entry : connectedClients.entrySet()) {
			if (entry.getValue().equals(client)) {
				personId = entry.getKey();
				break;
			}
		}

		if (personId != null) {
			// Update the person's login status
			db.updatePersonLoginStatus(personId, false);
			connectedClients.remove(personId);
		}
		super.clientDisconnected(client);
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
		db.close(null);
	}

	@Override
	protected void serverClosed() {
		super.serverClosed();
		System.out.println("Server closed");
		db.close(null);
	}

	protected void movieListRequest(Message message, ConnectionToClient client) throws Exception {
      //  System.out.println("In SimpleServer, Handling getMovies.");
      //  System.out.println("In SimpleServer, Handling getMovies.");
		List<Movie> movies = db.getAllMovies();
		for (Movie movie : movies) {
			//System.out.println("Movie Title: " + movie.getEnglishName());
			//System.out.println("Movie Title: " + movie.getEnglishName());
		}
		String movieType = message.getData();
		//System.out.println("got the movies from serverDB");
		//System.out.println("In SimpleServer, got back from serverDB.getAllMovies");
		//System.out.println("got the movies from serverDB");
		//System.out.println("In SimpleServer, got back from serverDB.getAllMovies");
		String jsonMovies = objectMapper.writeValueAsString(movies);
		message.setData(jsonMovies);
		message.setAdditionalData(movieType);
		message.setMessage("movieList");
//		System.out.println("In SimpleServer, Sending the client: \n ." + jsonMovies);
		client.sendToClient(message);

	}

	private void handleGetCinemaList(ConnectionToClient client) throws IOException {
    List<String> cinemas = db.getCinemaList();
    client.sendToClient(new Message(0, "cinemaList", objectMapper.writeValueAsString(cinemas)));
	}

//	private void handleGenerateReportRequest(String data, ConnectionToClient client) throws IOException {
//		JsonNode dataNode = objectMapper.readTree(data);
//		String reportType = dataNode.get("reportType").asText();
//		LocalDate month = LocalDate.parse(dataNode.get("month").asText());
//		String cinema = dataNode.get("cinema").asText();
//
//		String reportData = db.generateReport(reportType, month, cinema);
//
//		ObjectNode responseNode = objectMapper.createObjectNode();
//		responseNode.put("reportType", reportType);
//		responseNode.put("reportData", reportData);
//
//		client.sendToClient(new Message(0, "reportData", objectMapper.writeValueAsString(responseNode)));
//	}

	private void handleGenerateReportRequest(String data, ConnectionToClient client) throws IOException {
		JsonNode dataNode = objectMapper.readTree(data);
		String reportType = dataNode.get("reportType").asText();
		LocalDate month = LocalDate.parse(dataNode.get("month").asText());
		String cinema = dataNode.get("cinema").asText();

		switch (reportType) {
			case "Monthly Ticket Sales":
				generateMonthlyTicketSalesReport(month, cinema, client);
				break;

			case "Monthly Ticket Sales Manager":
				generateMonthlyTicketSalesManagerReport(month, cinema, client);
				break;

			case "Ticket Tab Sales":
				generateTicketTabSalesReport(month, cinema, client);
				break;

			case "Home Movie Link Sales":
				generateHomeMovieLinkSalesReport(month, cinema, client);
				break;

			case "Customer Complaints Histogram":
				generateCustomerComplaintsHistogramReport(month, cinema, client);
				break;

			default:
				client.sendToClient(new Warning("Unknown report type"));
				break;
		}
	}

	private void generateMonthlyTicketSalesReport(LocalDate month, String cinema, ConnectionToClient client) throws IOException {
		synchronized (ticketLock) {
			String reportData = db.generateReport("Monthly Ticket Sales", month, cinema);
			sendReportToClient("Monthly Ticket Sales", reportData, client);
		}
	}

	private void generateMonthlyTicketSalesManagerReport(LocalDate month, String cinema, ConnectionToClient client) throws IOException {
		synchronized (ticketLock) {
			String reportData = db.generateReport("Monthly Ticket Sales Manager", month, cinema);
			sendReportToClient("Monthly Ticket Sales Manager", reportData, client);
		}
	}

	private void generateTicketTabSalesReport(LocalDate month, String cinema, ConnectionToClient client) throws IOException {
		synchronized (ticketTabLock) {
			String reportData = db.generateReport("Ticket Tab Sales", month, cinema);
			sendReportToClient("Ticket Tab Sales", reportData, client);
		}
	}

	private void generateHomeMovieLinkSalesReport(LocalDate month, String cinema, ConnectionToClient client) throws IOException {
		synchronized (movieLinkLock) {
			String reportData = db.generateReport("Home Movie Link Sales", month, cinema);
			sendReportToClient("Home Movie Link Sales", reportData, client);
		}
	}

	private void generateCustomerComplaintsHistogramReport(LocalDate month, String cinema, ConnectionToClient client) throws IOException {
		synchronized (complaintLock) {
			String reportData = db.generateReport("Customer Complaints Histogram", month, cinema);
			sendReportToClient("Customer Complaints Histogram", reportData, client);
		}
	}

	private void sendReportToClient(String reportType, String reportData, ConnectionToClient client) throws IOException {
		ObjectNode responseNode = objectMapper.createObjectNode();
		responseNode.put("reportType", reportType);
		responseNode.put("reportData", reportData);

		client.sendToClient(new Message(0, "reportData", objectMapper.writeValueAsString(responseNode)));
	}


	// YONATHAN`S PARTS:
	protected void handleFetchRandomCustomer(Message message, ConnectionToClient client) {
		try {
			Person randomCustomer = db.fetchRandomCustomer();
			Message response = new Message(0, "fetchRandomCustomerResponse", objectMapper.writeValueAsString(randomCustomer));
			client.sendToClient(response);
		} catch (Exception e) {
			System.err.println("Error in handleFetchRandomCustomer: " + e.getMessage());
			e.printStackTrace();
		}
	}

	protected void handleFetchUserBookings(Message message, ConnectionToClient client) {
		try {
			//System.out.println("In SimpleServer, handleFetchUserBookings function");
			//System.out.println("In SimpleServer, handleFetchUserBookings function");
			int userId = Integer.parseInt(message.getData());
			//System.out.println("1");
			//System.out.println("1");
			List<Booking> bookings = db.fetchUserBookings(userId);
			//System.out.println("2");
			//System.out.println("2");
			for (Booking booking : bookings) {
				System.out.println("Booking ID: " + booking.getBookingId() + " - isActive: " + booking.isActive());
			}
			//System.out.println("Serialized Bookings: " + objectMapper.writeValueAsString(bookings));
			//System.out.println("Serialized Bookings: " + objectMapper.writeValueAsString(bookings));
			Message response = new Message(0, "fetchUserBookingsResponse", objectMapper.writeValueAsString(bookings));
			//System.out.println("3");
			//System.out.println("3");
			response.setAdditionalData(String.valueOf(userId));
			//System.out.println("4");
			//System.out.println("4");
			client.sendToClient(response);
		} catch (Exception e) {
			System.err.println("Error in handleFetchUserBookings: " + e.getMessage());
			e.printStackTrace();
		}
	}

	protected void handleCancelBooking(Message message, ConnectionToClient client) {
		try {
			int bookingId = Integer.parseInt(message.getData());
			double refund = Double.parseDouble(message.getAdditionalData());
			db.cancelBooking(bookingId);
			System.out.println("Refund is " + refund);

			Message response = new Message(0, "cancelBookingResponse", "Booking cancelled", bookingId + ":" + String.valueOf(refund));
			client.sendToClient(response);
		} catch (Exception e) {
			System.err.println("Error in handleCancelBooking: " + e.getMessage());
			e.printStackTrace();
		}
	}

	protected void handleSubmitComplaint(Message message, ConnectionToClient client) {
		try {
			System.out.println("In SimpleServer, handleSubmitComplaint");
			System.out.println("Received message data: " + message.getData());
			Complaint c = objectMapper.readValue(message.getData(), Complaint.class);

			db.addComplaint(c);
			System.out.println("Complaint submitted, got back from serverDB");

			Message response = new Message(0, "submitComplaintResponse", "Complaint submitted");
			client.sendToClient(response);
		} catch (Exception e) {
			System.err.println("Error in handleSubmitComplaint: " + e.getMessage());
			e.printStackTrace();
		}
	}

	protected void handleFetchAllComplaints(Message message, ConnectionToClient client) {
		try {
			List<Complaint> complaints = db.fetchAllComplaints();
			Message response = new Message(0, "fetchComplaintsResponse", objectMapper.writeValueAsString(complaints));
			client.sendToClient(response);
		} catch (Exception e) {
			System.err.println("Error in handleFetchAllComplaints: " + e.getMessage());
			e.printStackTrace();
		}
	}

	protected void handleFetchCustomerComplaints(Message message, ConnectionToClient client) {
		try {
			String customerId = message.getData();
			List<Complaint> complaints = db.fetchCustomerComplaints(customerId);
			Message response = new Message(0, "fetchCustomerComplaintsResponse", objectMapper.writeValueAsString(complaints));
			client.sendToClient(response);
		} catch (Exception e) {
			System.err.println("Error in handleFetchCustomerComplaints: " + e.getMessage());
			e.printStackTrace();
		}
	}

	protected void handleFetchComplaints(Message message, ConnectionToClient client) {
		try {
			String status = message.getData();
			List<Complaint> complaints = db.fetchComplaints(status);

			Message response = new Message(0, "fetchComplaintsResponse", objectMapper.writeValueAsString(complaints));
			response.setAdditionalData(status);
			client.sendToClient(response);
		} catch (Exception e) {
			System.err.println("Error in handleFetchComplaints: " + e.getMessage());
			e.printStackTrace();
		}
	}

	protected void handleRespondToComplaint(Message message, ConnectionToClient client) {
		try {
			String[] data = message.getData().split(";");
			int complaintId = Integer.parseInt(data[0]);
			String responseText = data[1];
			int refund = Integer.parseInt(data[2]);
			db.respondToComplaint(complaintId, responseText, refund);

			Message response = new Message(0, "respondToComplaintResponse", "Response submitted");
			client.sendToClient(response);
		} catch (Exception e) {
			System.err.println("Error in handleRespondToComplaint: " + e.getMessage());
			e.printStackTrace();
		}
	}

	protected void handleUpdateComplaint(Message message, ConnectionToClient client) {
		try {
			Complaint complaint = objectMapper.readValue(message.getData(), Complaint.class);
			db.updateComplaint(complaint);
			Message response = new Message(0, "respondToComplaintResponse", "Complaint updated successfully");
			client.sendToClient(response);
		} catch (Exception e) {
			System.err.println("Error in handleUpdateComplaint: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Add this method to SimpleServer class fixed using coplit
	// public void sendToAllClients(Message message) throws Exception {
	// 	for (Object clientObj : getClientConnections()) {
	// 		ConnectionToClient client = (ConnectionToClient) clientObj;
	// 		client.sendToClient(message);
	// 		System.out.println("Message sent to client, inside simpleserver sendtoallclients: " + message.getMessage());
	// 	}
	// }


}