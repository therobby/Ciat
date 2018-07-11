import java.io.OutputStream
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread

open class ClientHandler(
        private val client: Socket) {
    private val reader = Scanner(client.getInputStream())
    private val writer: OutputStream = client.getOutputStream()
    private var running = true
    var user: User? = null


    fun start() {
        user?.ip = client.inetAddress.hostAddress
        thread {
            try {
                while (running) {    // client loop
                    if (reader.hasNextLine()) {
                        val text = reader.nextLine()
                        if (handler(text)) {
                            client.close()
                        }
                        println("Received: $text")
                    }
                    if (client.isClosed || !client.isConnected)
                        running = false
                }
            } catch (e: Exception) {
                if (!(e.toString().contains("Socket is closed", true) ||
                                e.toString().contains("Socket closed", true)))
                    if (headless) {
                        println("ERROR: Exception while ${client.inetAddress.hostAddress} client receiving data:\n" +
                                "$e")
                    } else {
                        window.display("<error>Exception while ${client.inetAddress.hostAddress} client receiving data:\n" +
                                "$e")
                    }
                e.stackTrace
            }
            close()
        }
    }

    private fun send(message: ByteArray) {
        println("Send: $message")
        try {
            writer.write(message)
        } catch (e: Exception) {
            if (!(e.toString().contains("Socket is closed", true) ||
                            e.toString().contains("Socket closed", true) ||
                            e.toString().contains("Socket write error", true)))
                if (headless) {
                    println("ERROR: Exception while sending ${client.inetAddress.hostAddress} client data:\n" +
                            "$e")
                } else {
                    window.display("<error>Exception while sending ${client.inetAddress.hostAddress} client data:\n" +
                            "$e")
                }
            e.stackTrace
        }
    }

    fun close() {
        running = false
        send("dc".toByteArray())
        if (!client.isClosed)
            client.close()
        if(headless)
            println("SERVER: Connection closed for ${client.inetAddress.hostAddress}")
        else
            window.display("<smess>Connection closed for ${client.inetAddress.hostAddress}")
        server.channels.forEach {
            it.users.remove(client.inetAddress.hostAddress)
        }
    }

    fun sendManager(module: String, info: Any = "") {
        when (module) {
            "joinReject" -> {
                send("$info".toByteArray())
            }
            "userJoin" -> {
                send("jo${info as String}\n".toByteArray())
            }
            "channelsUpdate" -> {
                send("cu${(info as String).replace("\n", "<nl>")}\n".toByteArray())
            }
            "channelJoin" -> {
                send("muChannel switched to $info\n".toByteArray())
            }
            "sendMessage" -> {
                val output = (info as String).replace("\n", "<nl>")
                send("mu$output\n".toByteArray())
            }
            "ping" -> {
                send("pi$info\n".toByteArray())
            }
            "kick" -> {
                send("dc".toByteArray())
                user?.currentChannel?.users?.remove(client.inetAddress.hostAddress)
                if(!headless)
                    window.usersListUpdate()
            }
            "disconnect" -> {
                send("dc".toByteArray())
                if(headless){
                    println("SERVER: ${user?.nickname} disconnected!")
                }
                else {
                    window.display("<smess>${user?.nickname} disconnected!")
                    window.usersListUpdate()
                }
                user?.currentChannel?.updateMessages("${user?.nickname} disconnected!")
            }
        }
    }

    private fun handler(message: String): Boolean {
        val modifier = message.take(2)
        val onlyData = message.drop(2)
        println("Handler: $message")

        when (modifier) {

            "uj" -> {  // user join
                when (user!!.currentChannel.join(client.inetAddress.hostAddress, onlyData)) {
                    "confirmed" -> {
                        user!!.currentChannel.users[client.inetAddress.hostAddress] = user!!
                        user!!.nickname = onlyData
                        user!!.currentChannel.nickToIp[user!!.nickname] = client.inetAddress.hostAddress
                        sendManager("userJoin", "confirmed")
                        user!!.connected = true
                        user!!.checkPermission()
                        server.channels.forEach {
                            it.updateChannelsList()
                        }
                        if(headless){
                            println("SERVER: $onlyData joined!")
                        }
                        else {
                            window.usersListUpdate()
                            window.display("<smess>$onlyData joined!")
                        }
                    }
                    else -> {
                        sendManager("userJoin", "User already exists")
                    }
                }
            }

            "cj" -> {  // channel join
                user!!.joinChannel(onlyData)
            }

            "mr" -> {  // message received
                if (user!!.connected) {
                    user!!.receivedMessage(onlyData)
                }
            }

            "cr" -> {   // command received
                if(headless)
                    println("<${user!!.currentChannel.name}> ${user!!.nickname}: $message")
                else
                    window.display("<umess> /${message.drop(1)}", user!!.nickname, user!!.currentChannel.name)

                if(server.su[client.inetAddress.hostAddress]?.contains(user!!.nickname) == true) {
                    val output = server.processCommand(onlyData)
                    if(headless)
                        println(output.dropLast(1))
                    else
                        window.display("<smess>${output.dropLast(1)}")
                    sendManager("sendMessage", output)
                }
                else
                    sendManager("sendMessage","Only su can use that!")
            }

            "pi" -> {   // ping
                if (user!!.connected) {
                    sendManager("ping", "pong")
                }
            }

            "dc" -> {   //disconnect
                user!!.connected = false
                user!!.currentChannel.users.remove(client.inetAddress.hostAddress)
                if(headless) {
                    println("SERVER: ${user!!.nickname} disconnected!")
                }
                else {
                    window.display("<smess>${user!!.nickname} disconnected!")
                    window.usersListUpdate()
                }
                running = false
            }
        }

        return false
    }
}