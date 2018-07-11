import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import tornadofx.*

class LoginWindow : View("Login") {
    override val root = VBox()
    private var nickname = ""
    private var ip = ""

    private var nicknameField: TextField by singleAssign()
    private var ipField: TextField by singleAssign()
    private var infoLabel: Label by singleAssign()

    init {
        login.window = this
        with(root) {
            setPrefSize(200.0, 75.0)
            ipField = textfield {
                promptText = "IP"
                prefWidth = 40000.0
                prefHeight = 40000.0
            }
            nicknameField = textfield {
                promptText = "Nickname"
                prefWidth = 40000.0
                prefHeight = 40000.0
            }
            infoLabel = label("Enter IP and Nickname")
            button("Login") {
                prefWidth = 40000.0
                prefHeight = 40000.0
                action {
                    nickname = nicknameField.text
                    ip = ipField.text
                    println(nickname)
                    println(ip)
                    if (ip.isNotBlank() && nickname.isNotBlank()) {
                        server.ip = ip
                        server.nickname = nickname
                        server.startClient()
                    } else {
                        if (ip.isBlank()) {
                            ipField.style = "-fx-border-color: #ff0000;" +
                                    "-fx-control-inner-background: #ffe6e6;"
                        }
                        else {
                            ipField.style = "-fx-border-color: #ffffff"
                            ipField.style += "-fx-control-inner-background: #ffffff"
                        }
//-fx-control-inner-background:
                        if (nickname.isBlank()) {
                            nicknameField.style = "-fx-border-color: #ff0000;" +
                                    "-fx-control-inner-background: #ffe6e6;"
                        }
                        else {
                            nicknameField.style = "-fx-border-color: #ffffff"
                            nicknameField.style += "-fx-control-inner-background: #ffffff"
                        }
                    }
                }
            }
        }
    }

    fun changeToMainView(){
        replaceWith<ClientWindow>()
    }

    fun error(error : String){
        infoLabel.text = error
        infoLabel.style = "-fx-fill: #ff0000"
        ipField.style = "-fx-border-color: #ff0000;" +
                "-fx-control-inner-background: #ffe6e6;"
    }
}