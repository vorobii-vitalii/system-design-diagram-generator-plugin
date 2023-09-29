import org.vitalii.vorobii.annotation.Component;

@Component(name = "SIP server")
public class RegistrationServer {

	public void registerNewClient(String sipURI) {
		System.out.println("Registering new client by url = " + sipURI);
	}

}
