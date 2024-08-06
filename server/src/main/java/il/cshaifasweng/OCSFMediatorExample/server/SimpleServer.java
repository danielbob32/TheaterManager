package il.cshaifasweng.OCSFMediatorExample.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import il.cshaifasweng.OCSFMediatorExample.entities.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import il.cshaifasweng.OCSFMediatorExample.entities.Worker;
import il.cshaifasweng.OCSFMediatorExample.entities.*;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
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
					boolean add_success = db.addMovie(movie);
					if (add_success) {
						client.sendToClient(new Message(0,"Movie add:success"));
					} else {
						client.sendToClient(new Message(0,"Movie add:failed"));
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

				default:
					client.sendToClient(new Warning("Unknown request type"));
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
			Iterator<JsonNode> seatsIdIterator = seatsIdNode.elements();
			while (seatsIdIterator.hasNext()) {
				seatIds.add(seatsIdIterator.next().asInt());
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
}