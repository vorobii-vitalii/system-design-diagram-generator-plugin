import org.vitalii.vorobii.annotation.Component;

@Component(name = "SIP server", elementDescription = "Class is responsible for registration of clients")
public class RegistrationServer {

	public void registerNewClient(String sipURI) {
		System.out.println("Registering new client by url = " + sipURI);
	}

}
