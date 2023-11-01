import org.vitalii.vorobii.annotation.Component;
import org.vitalii.vorobii.annotation.ComponentAction;
import org.vitalii.vorobii.annotation.GenerateSequence;

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

	@ComponentAction(requestDescription = "Connect to registration server", responseDescription = "ID")
	public void connect(String sipURI) {
		registrationServer.registerNewClient(sipURI);
	}

	@GenerateSequence(sequenceName = "create_new_conferece", sequenceDescription = "Create new conference")
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
