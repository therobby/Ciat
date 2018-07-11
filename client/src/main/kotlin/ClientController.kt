import java.net.Socket
import kotlin.system.exitProcess


class ClientController {
    private var client: ClientHandler? = null
    var nickname = "Test"
    var ip = "localhost"

    fun startClient() {
        val client: Socket?
        val port = 9999     // TODO
        try {
            client = Socket(ip, port)
        } catch (e: Exception) {
            if (e.toString().contains("Address already in use", true)) {
                window.display(">>>Exception while starting server!\nAddress already in use")
                login.error("Address already in use")
            } else if (e.toString().contains("UnknownHostException", true)) {
                window.display(">>>Exception while starting server!\nUnknownHostException")
                login.error("Unknown Host")
            } else if (e.toString().contains("Connection refused", true)) {
                window.display(">>>Exception while starting server!\nConnection refused")
                login.error("Connection refused")
            } else
                window.display(">>>Unknown exception while starting server!\n$e")
            println("Exception: $e")
            return
        }
        println("Client connected!")
        login.change()
        this.client = ClientHandler(client, nickname)
    }

    fun sendMessage(message: String) {
        if(message.take(1) == "/")
            client?.sendManager("sendCommand", message.drop(1))
        else if(message.take(2) == "sc")
            client?.sendManager("switchChannel", message.drop(2))
        else
            client?.sendManager("sendMessage", message)
    }

    fun stopClient(status: Int) {
        if(status == 4)
            client?.close(false)
        else
            client?.close()
        clientShutdown(status)
    }

    private fun clientShutdown(status: Int) {
        exitProcess(status)
    }
}