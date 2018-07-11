import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import tornadofx.*

class ServerWindow : View("Čiat server") {
    override val root : BorderPane by fxml("/views/ServerWindow.fxml")
    private val controller = window
    private val commandsWindow : TextArea by fxid("serverCommands")
    private val sendButton : Button by fxid("serverSendButton")
    private val commandLine : TextField by fxid("serverCommandLine")
    private val usersList : TextArea by fxid("serverUsersList")

    init {
        usersList.bind(controller.users)
        commandsWindow.bind(controller.history)
        sendButton.setOnAction { send() }
        controller.history.onChange {
            try {
                commandsWindow.positionCaret(commandsWindow.length)
            }catch (e:Exception){}
        }
        commandLine.setOnKeyPressed {
            if(it.code == KeyCode.ENTER)
                send()
        }
    }

    private fun send(){
        controller.history += "${controller.getTime()} ${commandLine.text}\n"
        controller.history += server.processCommand(commandLine.text)

        commandLine.clear()
        controller.save()
        commandsWindow.scrollTop = Double.MAX_VALUE
    }

    override fun onUndock() {
        server.stopServer(0)
        super.onUndock()

    }

}