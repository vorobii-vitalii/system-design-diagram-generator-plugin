import org.vitalii.vorobii.annotation.Component;
import org.vitalii.vorobii.annotation.ComponentAction;

@Component(name = "SIP server", elementDescription = "Class, responsible for creation of dialogs")
public class DialogsServer {
	private final DialogRepository dialogRepository;

	public DialogsServer(DialogRepository dialogRepository) {
		this.dialogRepository = dialogRepository;
	}

	@ComponentAction(requestDescription = "Create dialog request", responseDescription = "ID of created dialog")
	public void createDialog() {
		dialogRepository.saveDialog();
	}

}
