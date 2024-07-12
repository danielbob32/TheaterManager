package il.cshaifasweng.OCSFMediatorExample.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import il.cshaifasweng.OCSFMediatorExample.client.events.FailureEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.MovieListEvent;
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;


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
		System.out.println("Received message from server: " + msg);
		if (msg instanceof Message){
			Message message = (Message) msg;
			System.out.println("Message type: " + message.getMessage());
			if (message.getMessage().startsWith("movieList")) {
				if (message.getMessage().equals("Cinema movie") || message.getMessage().equals("Home movie")) {
					System.out.println("Movies received: " + message.getData());
                    EventBus.getDefault().post(new MovieListEvent(message.getData()));
                }
				else {
					try {
						List<Movie> movies= objectMapper.readValue(message.getData(), new TypeReference<List<Movie>>(){});
						EventBus.getDefault().post(new MovieListEvent(movies));
					} catch (IOException e) {
						e.printStackTrace();
						EventBus.getDefault().post(new FailureEvent("Failed to deserialize movies"));
					}

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
						alert.setContentText("Failed to login as customer, check credentials");
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
							System.out.println("111 Worker type: " + workerType);
							login(current);
							App.setRoot("WorkerMenu", workerType);
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
				}
				else {
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setTitle("Error");
						alert.setHeaderText(null);
						alert.setContentText("Failed to login as worker, check credentials");
						alert.showAndWait();
					});
				}
			}
		}

//		if (msg instanceof Warning) {
//			Warning warning = (Warning) msg;
//			String message = warning.getMessage();
//			System.out.println("Warning received: " + message);
//			if (message.startsWith("Worker login successful")) {
//				String[] parts = message.split(":");
//				if (parts.length == 2) {
//					String workerType = parts[1].trim();
//					App.setWorkerType(workerType);
//					Platform.runLater(() -> {
//						try {
//							App.setRoot("WorkerMenu", workerType);
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					});
//				}
//			} else if (message.equals("Customer login successful")) {
//				Platform.runLater(() -> {
//					try {
//						App.setRoot("CustomerMenu", null);
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				});
//			} else if (message.equals("Movie has been added successfully") || message.equals("Home movie has been added successfully")) {
//				Platform.runLater(() -> {
//					Alert alert = new Alert(Alert.AlertType.INFORMATION);
//					alert.setTitle("Success");
//					alert.setHeaderText(null);
//					alert.setContentText(message);
//					alert.showAndWait();
//				});
//			} else {
//				Platform.runLater(() -> {
//					Alert alert = new Alert(Alert.AlertType.ERROR);
//					alert.setTitle("Error");
//					alert.setHeaderText(null);
//					alert.setContentText(message);
//					alert.showAndWait();
//				});
//			}
//		}
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