package il.cshaifasweng.OCSFMediatorExample.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import il.cshaifasweng.OCSFMediatorExample.client.events.FailureEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.MessageEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.MovieListEvent;
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


public class SimpleClient extends AbstractClient {

	private static SimpleClient client = null;
	private ObjectMapper objectMapper;
	private Person connectedPerson;


	private SimpleClient(String host, int port) {
		super(host, port);
		this.objectMapper = new ObjectMapper();
		this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		this.connectedPerson = new Worker("Yarden", "y123", 1009);
	}

	@Override
	protected void handleMessageFromServer(Object msg) {
		if (msg instanceof Message) {
			Message message = (Message) msg;
			if (message.getMessage().equals("movieList")) {
				try {
					List<Movie> movies = objectMapper.readValue(message.getData(), new TypeReference<List<Movie>>() {
					});
					String movieType = message.getAdditionalData();
					if (movieType != null) {
						List<Movie> filteredMovies = movies.stream()
								.filter(movie -> ("Cinema Movies".equals(movieType) && movie.getIsCinema()) ||
										("Home Movies".equals(movieType) && movie.getIsHome()))
								.collect(Collectors.toList());
						EventBus.getDefault().post(new MovieListEvent(filteredMovies));
					} else {
						EventBus.getDefault().post(new MovieListEvent(movies));
					}
				} catch (IOException e) {
					e.printStackTrace();
					EventBus.getDefault().post(new FailureEvent("Failed to deserialize movies"));
				}
			} else if (message.getMessage().startsWith("Customer login:")) {
				String success = message.getMessage().split(":")[1];
				if (success.equals("successful")) {
					Platform.runLater(() -> {
						try {
							Customer current = objectMapper.readValue(message.getData(), Customer.class);
							login(current);
							App.setRoot("CustomerMenu", null);
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
				} else {
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setTitle("Error");
						alert.setHeaderText(null);
						alert.setContentText("ID was not found. Please check credentials and try again");
						alert.showAndWait();
					});
				}
			} else if (message.getMessage().startsWith("Price:")) {
				System.out.println("Price message received");
				String price_message = message.getMessage().split(":")[1];
				if (price_message.equals("success")) {
					System.out.println("Prices updated successfully in simple client");
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.INFORMATION);
						alert.setTitle("Success");
						alert.setHeaderText(null);
						alert.setContentText("Price updated successfully");
						alert.showAndWait();
					});
					try {
						sendToServer(new Message(0, "getMovies", "Cinema Movies"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setTitle("Error");
						alert.setHeaderText(null);
						alert.setContentText("Failed to update prices");
						alert.showAndWait();
					});
				}
			} else if (message.getMessage().startsWith("Worker login:")) {
				String success = message.getMessage().split(":")[1];
				if (success.equals("successful")) {
					System.out.println("Worker login successful");
					Platform.runLater(() -> {
						try {
							Worker current = objectMapper.readValue(message.getData(), Worker.class);
							String workerType = current.getWorkerType();
							System.out.println("Worker type: " + workerType);
							login(current);
							App.setRoot("WorkerMenu", workerType);
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
				} else {
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setTitle("Error");
						alert.setHeaderText(null);
						alert.setContentText("One of the fields is incorrect. Please check credentials and try again");
						alert.showAndWait();
					});
				}
			} else if (message.getMessage().startsWith("Screening add:")) {
				System.out.println("screening add message received");
				String screening_add_message = message.getMessage().split(":")[1];
				if (screening_add_message.equals("success")) {
					System.out.println("Screening added successfully in simple client");
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.INFORMATION);
						alert.setTitle("Success");
						alert.setHeaderText(null);
						alert.setContentText("Screening added successfully");
						alert.showAndWait();
					});
				} else {
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setTitle("Error");
						alert.setHeaderText(null);
						alert.setContentText(message.getData() != null ? message.getData() : "Failed to add screening");
						alert.showAndWait();
					});
				}
			} else if (message.getMessage().startsWith("Screening delete:")) {
					System.out.println("screening delete message received");
					String screening_delete_message = message.getMessage().split(":")[1];
					if (screening_delete_message.equals("success")) {
						System.out.println("Screening added successfully in simple client");
						Platform.runLater(() -> {
							Alert alert = new Alert(Alert.AlertType.INFORMATION);
							alert.setTitle("Success");
							alert.setHeaderText(null);
							alert.setContentText("Screening deleted successfully");
							alert.showAndWait();
						});
					} else {
						Platform.runLater(() -> {
							Alert alert = new Alert(Alert.AlertType.ERROR);
							alert.setTitle("Error");
							alert.setHeaderText(null);
							alert.setContentText("Failed to delete screening");
							alert.showAndWait();
						});
					}
			} else if (message.getMessage().startsWith("Movie delete:")) {
				System.out.println("all movie delete message received");
				String movie_delete_message = message.getMessage().split(":")[1];
				if (movie_delete_message.equals("success")) {
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.INFORMATION);
						alert.setTitle("Success");
						alert.setHeaderText(null);
						alert.setContentText("Screening deleted successfully");
						alert.showAndWait();
					});
				} else {
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setTitle("Error");
						alert.setHeaderText(null);
						alert.setContentText("Failed to delete movie");
						alert.showAndWait();
					});
				}
			} else if (message.getMessage().startsWith("Movie add:")) {
				System.out.println("add movie message received");
				String movie_add_message = message.getMessage().split(":")[1];
				if (movie_add_message.equals("success")) {
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.INFORMATION);
						alert.setTitle("Success");
						alert.setHeaderText(null);
						alert.setContentText("Movie have been added successfully");
						alert.showAndWait();
					});
				} else {
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setTitle("Error");
						alert.setHeaderText(null);
						alert.setContentText("Failed to delete movie");
						alert.showAndWait();
					});
				}
			} else switch (message.getMessage()) {

				case "Price change request created":
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.INFORMATION);
						alert.setTitle("Success");
						alert.setHeaderText(null);
						alert.setContentText("Price change request created successfully");
						alert.showAndWait();
					});
					break;

				case "priceChangeRequests":
					try {
						List<PriceChangeRequest> requests = objectMapper.readValue(message.getData(),
								new TypeReference<List<PriceChangeRequest>>() {});
						EventBus.getDefault().post(new MessageEvent(new Message(0, "priceChangeRequests", objectMapper.writeValueAsString(requests))));
					} catch (IOException e) {
						e.printStackTrace();
						EventBus.getDefault().post(new FailureEvent("Failed to deserialize price change requests"));
					}
					break;

				case "Price change request approved":
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.INFORMATION);
						alert.setTitle("Success");
						alert.setHeaderText(null);
						alert.setContentText("Price change request approved and price updated successfully");
						alert.showAndWait();
					});
					EventBus.getDefault().post(new MessageEvent(message));
					break;

				case "Price change request denied":
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.INFORMATION);
						alert.setTitle("Information");
						alert.setHeaderText(null);
						alert.setContentText("Price change request denied");
						alert.showAndWait();
					});
					EventBus.getDefault().post(new MessageEvent(message));
					break;

				case "Price change request error":
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setTitle("Error");
						alert.setHeaderText(null);
						alert.setContentText(message.getData() != null ? message.getData() : "An error occurred with the price change request");
						alert.showAndWait();
					});
					break;

				default:
					System.out.println("Received unknown message: " + message.getMessage());
					break;
			}
		} else if (msg instanceof Warning) {
			Warning warning = (Warning) msg;
			EventBus.getDefault().post(new WarningEvent(warning));
		} else {
			System.out.println("Received unknown message type: " + msg.getClass().getName());
		}
	}

	public static SimpleClient getClient() {
		if (client == null) {
			client = new SimpleClient("localhost", 3000);
		}
		return client;
	}

	public void tryWorkerLogin(Worker worker)
	{
		try {
			sendToServer(new Message(0, "login:worker", serializeWorker(worker)));
		} catch (IOException e) {
			e.printStackTrace();
//			EventBus.getDefault().post(new UpdateFailureEvent("Failed to update movie show times"));
			System.out.println("Failed to update movie show times");
		}
	}

	public void tryCustomerLogin(Customer customer)
	{
		try {
			sendToServer(new Message(0, "login:customer", objectMapper.writeValueAsString(customer)));
		} catch (IOException e) {
			e.printStackTrace();
//			EventBus.getDefault().post(new UpdateFailureEvent("Failed to update movie show times"));
			System.out.println("Failed to recognize this customer");
		}
	}

	public String serializeWorker(Worker worker)
	{
		try {
			return objectMapper.writeValueAsString(worker);
		} catch (IOException e) {
			e.printStackTrace();
//			EventBus.getDefault().post(new UpdateFailureEvent("Failed to serialize movie"));
			System.out.println("couldn't serialize worker");
			return null;
		}
	}

	public void sendToServer(Object msg) throws IOException {
		System.out.println("Sending to server: " + msg);
		super.sendToServer(msg);
	}

	public void openConnection() throws IOException {
		System.out.println("Opening connection to server");
		super.openConnection();
	}

	public void getMovies() {
		try {
			sendToServer(new Message(0, "getMovies"));
		} catch (IOException e) {
			e.printStackTrace();
			EventBus.getDefault().post(new FailureEvent("Failed to request movies"));

		}
	}


	public void login(Person p)
	{
		this.connectedPerson = p;
	}

	public void logout()
	{
		this.connectedPerson = null;
	}

	public Person getConnectedPerson()
	{
		return connectedPerson;
	}
}




