import org.vitalii.vorobii.annotation.Component;
import org.vitalii.vorobii.annotation.ComponentAction;

@Component(name = "SIP server", elementDescription = "Class is responsible for registration of clients")
public class RegistrationServer {

	@ComponentAction(requestDescription = "Register new client by SIP URI", responseDescription = "ID")
	public String registerNewClient(String sipURI) {
		System.out.println("Registering new client by url = " + sipURI);
		return "id";
	}

}
