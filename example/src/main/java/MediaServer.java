import org.vitalii.vorobii.annotation.Component;
import org.vitalii.vorobii.annotation.ComponentAction;

@Component(name = "Media server", elementDescription = "Class, responsible for creation of media conferences")
public class MediaServer {

	@ComponentAction(requestDescription = "Create conference by given id")
	public void createConference(String conferenceId) {
		System.out.println("Creating conference " + conferenceId);
	}

	public void connectToConference(String sipURI, String conferenceId) {
		System.out.println("Connecting " + sipURI + " to conference " + conferenceId);
	}

}
