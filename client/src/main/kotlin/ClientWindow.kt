import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import tornadofx.*
import java.util.ArrayList

class ClientWindow : View("Čiat client") {
    override val root: SplitPane by fxml("/views/ServerWindow.fxml")
    private val controller: ClientWindowController
    private val commandsWindow: TextArea by fxid("serverCommands")
    private val sendButton: Button by fxid("serverSendButton")
    private val commandLine: TextField by fxid("serverCommandLine")
    private val channelsList: TreeView<Text> by fxid("channelsTree")

    init {
        window.rootWindow = this
        controller = window
        commandsWindow.bind(controller.history)
        sendButton.setOnAction { send() }
        controller.history.onChange {
            commandsWindow.positionCaret(commandsWindow.length)
        }
        commandLine.setOnKeyPressed {
            if (it.code == KeyCode.ENTER)
                send()
        }
        channelsList.onMouseClicked = object : EventHandler<MouseEvent> {
            override fun handle(event: MouseEvent?) {
                if(event!!.clickCount == 2){
                    if(event.source != null && event.source is TreeView<*>) {
                        try {
                            @Suppress("UNCHECKED_CAST")
                            if ((event.source as TreeView<Text>).selectedValue != null && (event.source as TreeView<Text>).selectedValue!!.isUnderline) {
                                server.sendMessage("sc${(event.source as TreeView<Text>).selectedValue!!.text}")
                            }
                        } catch (e : Exception) {}
                    }
                }
            }
        }
    }

    fun updateChannels(newChannels: HashMap<String, List<String>>, currentChannel: String) {
        runLater {
            println(newChannels)
            val root = TreeItem<Text>()
            newChannels.forEach { t, u ->
                val channel: TreeItem<Text>
                if (t == currentChannel) {
                    val text = Text(t)
                    text.font = Font.font("System", FontWeight.BOLD, 12.0)
                    text.fill = Paint.valueOf("#336600")
                    text.isUnderline = true
                    channel = TreeItem(text)
                    channel.isExpanded = true
                } else {
                    val text = Text(t)
                    text.font = Font.font("System", 12.0)
                    text.fill = Paint.valueOf("#336600")
                    text.isUnderline = true
                    channel = TreeItem(text)
                }

                u.forEach {
                    val user: TreeItem<Text>
                    if(it.isNotBlank()) {
                        if (it.drop(4) == server.nickname) {
                            val prefix = it.take(4)
                            val text = Text(it.drop(4))
                            text.font = Font.font("System", FontWeight.BOLD, 12.0)
                            if (prefix.contains("su"))
                                text.fill = Paint.valueOf("#FF0000")
                            user = TreeItem(text)
                            channel.children.add(user)
                        } else {
                            val prefix = it.take(4)
                            val text = Text(it.drop(4))
                            text.font = Font.font("System", 12.0)
                            if (prefix.contains("su"))
                                text.fill = Paint.valueOf("#FF0000")
                            user = TreeItem(text)
                            channel.children.add(user)
                        }
                    }
                }
                root.children.add(channel)
            }
            channelsList.root = root
        }
    }

    private fun send() {
        server.sendMessage(commandLine.text)
        commandLine.clear()
        controller.save()
        commandsWindow.scrollTop = Double.MAX_VALUE

    }

    override fun onDock() {
        currentWindow?.sizeToScene()
        super.onDock()
    }
}