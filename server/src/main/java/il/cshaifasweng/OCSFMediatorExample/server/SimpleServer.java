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

public class SimpleServer extends AbstractServer {
	private ObjectMapper objectMapper = new ObjectMapper();
	private ServerDB db;

	public SimpleServer(int port) {
		super(port);
		try {
			this.db = new ServerDB();
			this.objectMapper.registerModule(new Hibernate5Module());
		} catch (Exception e) {
			System.err.println("Failed to initialize ServerDB: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		Message message = (Message) msg;
//		System.out.println("Received message from client: " + msg);
		String request = message.getMessage();
		try {
			String action = request.split(":")[0];

			switch (action) {
				case "login":
					String[] loginParts = request.split(":", 2);
					String person = loginParts.length > 1 ? loginParts[1] : "";

					switch (person) {
						case "worker":
							Worker worker = objectMapper.readValue(message.getData(), Worker.class);
							handleLoginRequest(worker, client);
							break;
						case "customer":
							Customer customer = objectMapper.readValue(message.getData(), Customer.class);
							handleLoginRequest(customer, client);
							break;
						default:
							client.sendToClient(new Warning("Unknown login type"));
					}
					break;

				case "add movie":
					String movieData = message.getData();
					Movie movie = objectMapper.readValue(movieData, Movie.class);
					boolean movieExists = db.checkMovieExists(movie.getEnglishName(), movie.getHebrewName());
					if (movieExists) {
						client.sendToClient(new Message(0, "Movie add:failed", "Movie already exists"));
					} else {
						boolean add_success = db.addMovie(movie);
						if (add_success) {
							client.sendToClient(new Message(0, "Movie add:success"));
							// Notify all connected clients about the new movie
							sendToAllClients(new Message(0, "new_movie_added", objectMapper.writeValueAsString(movie)));
						} else {
							client.sendToClient(new Message(0, "Movie add:failed"));
						}
					}
					break;
				case "getNotifications":
					int customerId = Integer.parseInt(message.getData());
					List<Notification> notifications = db.getUnreadNotificationsForCustomer(customerId);
					client.sendToClient(new Message(0, "notifications", objectMapper.writeValueAsString(notifications)));
					break;

				case "markNotificationAsRead":
					String[] parts2 = message.getData().split(",");
					int notificationId = Integer.parseInt(parts2[0]);
					int customerIdForMarkRead = Integer.parseInt(parts2[1]);
					db.markNotificationAsRead(notificationId, customerIdForMarkRead);
					client.sendToClient(new Message(0, "notificationMarkedAsRead"));
					break;

				case "getMovies":
					System.out.println("in SimpleServer getMovies request");
					movieListRequest(message, client);
					break;

				case "updatePrice":
					System.out.println("in SimpleServer updatePrice request");
					String[] parts = message.getData().split(",");
					int movieId = Integer.parseInt(parts[0]);
					String movieType = parts[1];
					int newPrice = Integer.parseInt(parts[2]);
					boolean success = db.updateMoviePrice(movieId, movieType, newPrice);
					if (success) {
						System.out.println("Price updated successfull in simple server");
						message.setMessage("Price:success");
					} else {
						message.setMessage("Price:failed");
					}
					client.sendToClient(message);
					break;

				case "deleteMovie":
					String deleteMovieType = request.split(":")[1];
					int movieId2 = Integer.parseInt(message.getData());
					boolean deleteSuccess = db.deleteMovie(movieId2, deleteMovieType);
					if (deleteSuccess) {
						client.sendToClient(new Message(0, "Movie delete:success"));
					} else {
						client.sendToClient(new Message(0, "Movie delete:failed", "Movie is currently in use"));
					}
					break;

				case "updateMovie":
					Movie movie_to_update = objectMapper.readValue(message.getData(), Movie.class);
					boolean updateSuccess = db.updateMovie(movie_to_update);
					if (updateSuccess) {
						client.sendToClient(new Message(0, "Movie update:success"));
					} else {
						client.sendToClient(new Message(0, "Movie update:failed", "error occured"));
					}
					break;

				case "addScreening":
					Screening newScreening = objectMapper.readValue(message.getData(), Screening.class);
//					System.out.println("Received screening to add: " + newScreening);
					boolean addSuccess = db.addScreening(newScreening);
					if (addSuccess) {
						client.sendToClient(new Message(0, "Screening add:success"));
					} else {
						client.sendToClient(new Message(0, "Screening add:failed", "Hall is not available at the specified time"));
					}
					break;

				case "deleteScreening":
					int screeningId = Integer.parseInt(message.getData());
					boolean deleteScreeningSuccess = db.deleteScreening(screeningId);
					if (deleteScreeningSuccess) {
						System.out.println("Screening deleted successfully");
						client.sendToClient(new Message(0, "Screening delete:success"));
					} else {
						System.out.println("Failed to delete screening");
						client.sendToClient(new Message(0, "Screening delete:failed", "Hall is not available at the specified time"));
					}
					break;

				case "createPriceChangeRequest":
					PriceChangeRequest price_request = objectMapper.readValue(message.getData(), PriceChangeRequest.class);
					db.createPriceChangeRequest(price_request);
					client.sendToClient(new Message(0, "Price change request created successfully"));
					break;

				case "getPriceChangeRequests":
					List<PriceChangeRequest> requests = db.getPriceChangeRequests();
					String requestsJson = objectMapper.writeValueAsString(requests);
					client.sendToClient(new Message(0, "priceChangeRequests", requestsJson));
					break;

				case "approvePriceChangeRequest":
					int requestId = Integer.parseInt(message.getData());
//					System.out.println("Approving price change request: " + requestId);
					boolean price_success = db.updatePriceChangeRequestStatus(requestId, true);
					if (price_success) {
						PriceChangeRequest updatedRequest = db.getPriceChangeRequestById(requestId);
						String updatedRequestJson = objectMapper.writeValueAsString(updatedRequest);
						client.sendToClient(new Message(0, "Price change request approved and price updated successfully", updatedRequestJson));
					} else {
						client.sendToClient(new Message(0, "Failed to approve price change request. It may already be approved."));
					}
					break;

				case "denyPriceChangeRequest":
					int requestId2 = Integer.parseInt(message.getData());
					boolean price_success2 = db.updatePriceChangeRequestStatus(requestId2, false);
					if (price_success2) {
						PriceChangeRequest updatedRequest = db.getPriceChangeRequestById(requestId2);
						String updatedRequestJson = objectMapper.writeValueAsString(updatedRequest);
						client.sendToClient(new Message(0, "Price change request denied", updatedRequestJson));
					} else {
						client.sendToClient(new Message(0, "Failed to deny price change request. It may already be approved."));
					}
					break;

				case "getSeatAvailability":
					System.out.println("in SimpleServer getSeatAvailability request");
					handleGetSeatAvailability(message, client);
					break;

				case "checkTicketTab":
					System.out.println("in SimpleServer checkTicketTab request");
					handleCheckTicketTab(message, client);
					break;

				case "processPayment":
					System.out.println("in SimpleServer processPayment request");
					handleProcessPayment(message.getData(), client);
					break;

				case "getScreeningById":
					System.out.println("in SimpleServer getScreeningById request");
					handleGetScreeningById(message, client);
					break;

				case "purchaseTicketTab":
					System.out.println("in SimpleServer purchaseTicketTab request");
					handlePurchaseTicketTab(message.getData(), client);
					break;

				case "purchaseLink":
					System.out.println("In SimpleServer, handling purchaseLink request.");
					handlePurchaseLink(message.getData(), client); // Pass message data as String
					break;
				case "getCinemaList":
					handleGetCinemaList(client);
					break;
	
				case "generateReport":
					System.out.println("Handling generate report request");
					handleGenerateReport(message.getData(), client);
					break;

				case "getMovieById":
					int movie_id = message.getExtraData();
					Movie movie3 = db.getMovieById(movie_id);
					String jsonMovie = objectMapper.writeValueAsString(movie3);
					message.setData(jsonMovie);
					message.setMessage("movie refreshed");
					client.sendToClient(message);
					break;
				// YONATHAN`S CASES:
				case "fetchUserBookings":
					System.out.println("In SimpleServer handleFetchUserBookings request");
					handleFetchUserBookings(message, client);
					break;
				case "cancelPurchase":
					System.out.println("In SimpleServer handleCancelBooking request");
					handleCancelBooking(message, client);
					break;
				case "submitComplaint":
					System.out.println("In SimpleServer submitComplaint request");
					handleSubmitComplaint(message, client);
					break;
				case "fetchAllComplaints":
					System.out.println("In SimpleServer fetchAllComplaints request");
					handleFetchAllComplaints(message, client);
					break;
				case "fetchCustomerComplaints":
					System.out.println("In SimpleServer fetchCustomerComplaints request");
					handleFetchCustomerComplaints(message, client);
					break;
				case "fetchComplaints":
					System.out.println("In SimpleServer fetchComplaints request");
					handleFetchComplaints(message, client);
					break;
				case "respondToComplaint":
					System.out.println("In SimpleServer respondToComplaint request");
					handleRespondToComplaint(message, client);
					break;
				case "updateComplaint":
					System.out.println("In SimpleServer updateComplaint request");
					handleUpdateComplaint(message, client);
					break;
				case "fetchRandomCustomer":
					System.out.println("In SimpleServer fetchRandomCustomer request");
					handleFetchRandomCustomer(message, client);
					break;

				default:
					client.sendToClient(new Warning("Unknown request type"));
			}
		} catch (IOException e) {
			System.out.println("DEBUG: Error processing message - " + e.getMessage());
			e.printStackTrace();
			try {
				client.sendToClient(new Warning("Server error: " + e.getMessage()));
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void handleGetSeatAvailability(Message message, ConnectionToClient client) throws Exception {
		int screeningId = Integer.parseInt(message.getData());
		List<Seat> seats = db.getSeatsForScreening(screeningId);

		Message response = new Message(0, "seatAvailabilityResponse", objectMapper.writeValueAsString(seats));
		response.setAdditionalData(String.valueOf(screeningId));
		client.sendToClient(response);
	}

	protected void handleProcessPayment(String data, ConnectionToClient client) throws Exception {
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

	protected void handleGetScreeningById(Message message, ConnectionToClient client) throws IOException {
		System.out.println("getting screening #" + message.getData());
		Screening screening = db.getScreeningById(Integer.parseInt(message.getData()));
		String jsonScreening = objectMapper.writeValueAsString(screening);
		message.setData(jsonScreening);
		message.setMessage("screeningById");
		client.sendToClient(message);
	}

	protected void handlePurchaseTicketTab(String data, ConnectionToClient client) throws IOException {
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


protected void handlePurchaseLink(String data, ConnectionToClient client) {
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

        System.out.println("DEBUG: Purchase data - Name: " + name + ", ID: " + id + ", Email: " + email);


        Movie movie = db.getMovieById(movieId);
        if (movie == null) {
            client.sendToClient(new Message(0, "Movie not found"));
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date openTime = sdf.parse(selectedDate + " " + selectedTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(openTime);
        calendar.add(Calendar.HOUR, 24);
        Date closeTime = calendar.getTime();

        HomeMovieLink link = new HomeMovieLink(openTime, closeTime, false, HomeMovieLink.generateRandomLink("www.Theater.link."),
                                               id, totalPrice, true, new Date());
        link.setMovie(movie);

        Customer customer = null;
        try {
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

        if (newBooking != null) {
            ObjectNode bookingNode = objectMapper.createObjectNode();
            bookingNode.put("bookingId", newBooking.getBookingId());
            bookingNode.put("name", customer.getName());
            bookingNode.put("purchaseTime", newBooking.getPurchaseTime().getTime());
            bookingNode.put("movie", movie.getEnglishName());
            bookingNode.put("openTime", link.getOpenTime().getTime());
            bookingNode.put("closeTime", link.getCloseTime().getTime());
            bookingNode.put("watchLink", link.getWatchLink());
            bookingNode.put("totalPrice", link.getPrice());

            String jsonBooking = objectMapper.writeValueAsString(bookingNode);
            client.sendToClient(new Message(0, "purchasedHomeMovieLinkSuccessfully", jsonBooking));
        } else {
            client.sendToClient(new Message(0, "purchasingHomeMovieLinkFailed"));
        }
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


	private void handleLoginRequest(Object loginRequest, ConnectionToClient client) {
		System.out.println("Handling login request");
		Person p = (Person) loginRequest;
		boolean loginSuccess = false;
		String message = "";
		try {
			if (loginRequest instanceof Worker) {
				System.out.println("Worker login request");
				Worker worker = (Worker) loginRequest;
				worker = db.checkWorkerCredentials(worker.getPersonId(), worker.getPassword());
				System.out.println("Worker login: " + (worker != null));
				System.out.println("Worker: " + worker);
				if (worker != null) {
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
				//message = loginSuccess ? "Customer login successful" : "Invalid customer ID";
				message = customer!=null ? "Customer login:successful" : "Customer login:failed";
				if(customer!=null)
					p = customer;
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
			System.out.println("Serialization successful, JSON: " + jsonString);
			Message message1 = new Message(0, message, jsonString);
			client.sendToClient(message1);
			System.out.println("Message sent to client successfully");
		} catch (Exception e) {
			System.err.println("Error during serialization or sending: " + e.getMessage());
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
		db.close(null);
	}

	@Override
	protected void serverClosed() {
		super.serverClosed();
		System.out.println("Server closed");
		db.close(null);
	}

	protected void movieListRequest(Message message, ConnectionToClient client) throws Exception {
        System.out.println("In SimpleServer, Handling getMovies.");
		List<Movie> movies = db.getAllMovies();
		for (Movie movie : movies) {
			System.out.println("Movie Title: " + movie.getEnglishName());
		}
		String movieType = message.getData();
		System.out.println("got the movies from serverDB");
		System.out.println("In SimpleServer, got back from serverDB.getAllMovies");
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

private void handleGenerateReport(String data, ConnectionToClient client) throws IOException {
    JsonNode dataNode = objectMapper.readTree(data);
    String reportType = dataNode.get("reportType").asText();
    LocalDate month = LocalDate.parse(dataNode.get("month").asText());
    String cinema = dataNode.get("cinema").asText();

    String reportData = db.generateReport(reportType, month, cinema);

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
			System.out.println("In SimpleServer, handleFetchUserBookings function");
			int userId = Integer.parseInt(message.getData());
			System.out.println("1");
			List<Booking> bookings = db.fetchUserBookings(userId);
			System.out.println("2");
			for (Booking booking : bookings) {
				System.out.println("Booking ID: " + booking.getBookingId() + " - isActive: " + booking.isActive());
			}
			System.out.println("Serialized Bookings: " + objectMapper.writeValueAsString(bookings));
			Message response = new Message(0, "fetchUserBookingsResponse", objectMapper.writeValueAsString(bookings));
			System.out.println("3");
			response.setAdditionalData(String.valueOf(userId));
			System.out.println("4");
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


}