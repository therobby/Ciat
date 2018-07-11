import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread


class ServerWindowController : Controller() {
    val history = SimpleStringProperty("")
    val users = SimpleStringProperty("Users (0):\n")
    var allowSave: Boolean? = null

    fun display(message: String, userNickName: String = "", userChannelName: String = "") {
        val prefix = message.take(7)
        val messageWithoutPrefix = message.drop(7)
        history += when (prefix) {
            "<error>" -> {
                "${getTime()} ERROR!\n$messageWithoutPrefix\n"
            }
            "<umess>" -> {
                "${getTime()} <$userChannelName>$userNickName: $messageWithoutPrefix\n"
            }
            "<smess>" -> {
                "${getTime()} SERVER: $messageWithoutPrefix\n"
            }
            else -> ""
        }
        save()
    }

    fun save() {
        if(server != null) {
            if (allowSave == null) {
                val lastLog = server.properties["saveLastLog"]?.trim()?.toLowerCase() ?: "false"
                allowSave = lastLog == "true"
            } else if (allowSave!!) {
                val hCopy = history.valueSafe
                thread {
                    try {
                        File("LastLog.txt").printWriter().use { out ->
                            hCopy.split("\n").forEach {
                                out.println(it)
                            }
                        }
                    } catch (e: Exception) {
                        println("Save exception!\n$e")
                    }
                }
            }
        }
    }

    fun usersListUpdate() {
        var usersCount = 0
        server.channels.forEach {
            usersCount += it.users.size
        }
        var newPlayersList = "Users ($usersCount):\n"
        server.channels.forEach {
            newPlayersList += " ${it.name} (${it.users.size}):\n"
            it.users.forEach { _, u ->
                newPlayersList += "\t${u.nickname}\n"
            }
        }
        users.set(newPlayersList)
    }

    fun getTime(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("'['dd-MM-yyyy HH:mm:ss']'"))
    }
}