import org.vitalii.vorobii.annotation.Component;
import org.vitalii.vorobii.annotation.ComponentAction;

@Component(name = "Dialog Repository", elementDescription = "Class responsible for saving dialogs")
public class DialogRepository {

    @ComponentAction(requestDescription = "Save dialog", responseDescription = "Number of rows added")
    public int saveDialog() {
        someAction();
        int x = someAction();
        return 1;
    }

    public int someAction() {
        return 0;
    }

}
