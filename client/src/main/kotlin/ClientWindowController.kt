import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.ArrayList
import kotlin.concurrent.thread


class ClientWindowController : Controller() {
    val history = SimpleStringProperty("")
    var rootWindow : ClientWindow? = null

    fun display(message : String, user : String = ""){
        history += if(message.take(3).contains(">>>"))
            "${getTime()} ERROR!\n${message.drop(3)}\n"
        else if(user.isNotBlank())
            "${getTime()} $user: $message\n"
        else
            "${getTime()} $message\n"
        save()
    }

    fun save(){
        val hCopy = history
        thread {
            File("LastLog.txt").printWriter().use { out ->
                hCopy.get().split("\n").forEach {
                    out.println(it)
                }
            }
        }
    }

    fun channelsUpdate(newChannels : HashMap<String, List<String>>, currentChannel : String) {
        this.rootWindow?.updateChannels(newChannels,currentChannel)
    }

    private fun getTime(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("'['dd-MM-yyyy HH:mm:ss']'"))
    }
}