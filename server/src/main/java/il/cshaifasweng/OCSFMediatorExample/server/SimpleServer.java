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
		System.out.println("Received message from client: " + msg);
		Message message = (Message) msg;
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
						} else {
							client.sendToClient(new Message(0, "Movie add:failed"));
						}
					}
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
					System.out.println("Received screening to add: " + newScreening);
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
					String requests2 = objectMapper.writeValueAsString(requests);
					System.out.println("Sending price change requests to client: " + requests2);
					client.sendToClient(new Message(0, "priceChangeRequests", requests2));
					break;

				case "approvePriceChangeRequest":
					int requestId = Integer.parseInt(message.getData());
					System.out.println("Approving price change request: " + requestId);
					boolean price_success = db.updatePriceChangeRequestStatus(requestId, true);
					if (price_success) {
						PriceChangeRequest approvedRequest = db.getPriceChangeRequestById(requestId);
						if (approvedRequest != null) {
							boolean priceUpdateSuccess = db.updateMoviePrice(approvedRequest.getMovie().getId(), approvedRequest.getMovieType(), approvedRequest.getNewPrice());
							if (priceUpdateSuccess) {
								List<PriceChangeRequest> requests4 = db.getPriceChangeRequests();
								String requests3 = objectMapper.writeValueAsString(requests4);
								client.sendToClient(new Message(0, "Price change request approved and price updated successfully", requests3));
							} else {
								client.sendToClient(new Message(0, "Price change request approved but price update failed"));
							}
						} else {
							client.sendToClient(new Message(0, "Price change request approved but request details not found"));
						}
					} else {
						client.sendToClient(new Message(0, "Failed to approve price change request"));
					}
					break;

				case "denyPriceChangeRequest":
					int requestId2 = Integer.parseInt(message.getData());
					boolean price_success2 = db.updatePriceChangeRequestStatus(requestId2, false);
					if (price_success2) {
						List<PriceChangeRequest> requests4 = db.getPriceChangeRequests();
						String requests3 = objectMapper.writeValueAsString(requests4);
						client.sendToClient(new Message(0, "Price change request denied and price hasn't updated successfully", requests3));
					} else {
						client.sendToClient(new Message(0, "Failed to deny price change request"));
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
				System.out.println("0");
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
				bookingNode.put("name", newBooking.getCustomer().getName());
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
						System.out.println("1");
						bookingNode.put("amountLeft", ticketTab.getAmount());
					} else {
						System.out.println("2");
						bookingNode.put("amountLeft", 0);
					}
				}

				System.out.println("3");
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
				bookingNode.put("name", newBooking.getCustomer().getName());
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
		String movieType = message.getData();
		System.out.println("got the movies from serverDB");
		System.out.println("In SimpleServer, got back from serverDB.getAllMovies");
		String jsonMovies = objectMapper.writeValueAsString(movies);
		message.setData(jsonMovies);
		message.setAdditionalData(movieType);
		message.setMessage("movieList");
		//System.out.println("In SimpleServer, Sending the client: \n ." + jsonMovies);
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
}