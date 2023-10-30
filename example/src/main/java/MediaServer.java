import org.vitalii.vorobii.annotation.Component;

@Component(name = "Media server", elementDescription = "Class, responsible for creation of media conferences")
public class MediaServer {

	public void createConference(String conferenceId) {
		System.out.println("Creating conference " + conferenceId);
	}

	public void connectToConference(String sipURI, String conferenceId) {
		System.out.println("Connecting " + sipURI + " to conference " + conferenceId);
	}

}
