import org.vitalii.vorobii.annotation.Component;

@Component(name = "Websocket server", elementDescription = "Class is responsible for processing of incoming WebSocket requests")
public class WebSocketServer {
	private final MediaServer mediaServer;
	private final RegistrationServer registrationServer;
	private final DialogsServer dialogsServer;

	public WebSocketServer(MediaServer mediaServer, RegistrationServer registrationServer, DialogsServer dialogsServer) {
		this.mediaServer = mediaServer;
		this.registrationServer = registrationServer;
		this.dialogsServer = dialogsServer;
	}

	public void connect(String sipURI) {
		registrationServer.registerNewClient(sipURI);
	}

	public void createNewConference(String conferenceId) {
		dialogsServer.createDialog();
		mediaServer.createConference(conferenceId);
	}

	// generate puml instead

	public void connectToConference(String sipURI, String conferenceId) {
		dialogsServer.createDialog();
		mediaServer.connectToConference(sipURI, conferenceId);
	}

}
