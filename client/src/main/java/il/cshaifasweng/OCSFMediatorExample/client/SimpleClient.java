package il.cshaifasweng.OCSFMediatorExample.client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.greenrobot.eventbus.EventBus;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
import il.cshaifasweng.OCSFMediatorExample.client.events.*;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class SimpleClient extends AbstractClient {
	
	private static SimpleClient client = null;
	private ObjectMapper objectMapper = new ObjectMapper();
	private Person connectedPerson = null;


	private SimpleClient(String host, int port) {
		super(host, port);
	}

	@Override
	protected void handleMessageFromServer(Object msg) {
		Message message = (Message) msg;
		if (message.getMessage().equals("movieList")) {
			try {
				List<Movie> movies= objectMapper.readValue(message.getData(), new TypeReference<List<Movie>>(){});
				EventBus.getDefault().post(new MovieListEvent(movies));
			} catch (IOException e) {
				e.printStackTrace();
				EventBus.getDefault().post(new FailureEvent("Failed to deserialize movies"));
			}

		}
		else {
			System.out.println("Got an unknown message: " + message.getMessage());
		}
	}

	public void getMovies() {
		try {
			sendToServer(new Message(0, "getMovies"));
		} catch (IOException e) {
			e.printStackTrace();
			EventBus.getDefault().post(new FailureEvent("Failed to request movies"));
		}
	}

	public static SimpleClient getClient() {
		if (client == null) {
			client = new SimpleClient("localhost", 3000);
		}
		return client;
	}

	public void Login(Person p)
	{
		this.connectedPerson = p;
	}

	public void Logout()
	{
		this.connectedPerson = null;
	}

}
