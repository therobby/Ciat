import java.io.OutputStream
import java.net.Socket
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.thread

class ClientHandler(
        private val client: Socket,
        private val nickname: String) {
    private val reader = Scanner(client.getInputStream())
    private val writer: OutputStream = client.getOutputStream()
    private var running = true
    private var currentChannelName = ""

    init {
        thread {
            start()
        }
    }

    private fun start() {
        try {
            sendManager("join", nickname)
            while (running) {    // client loop
                if (reader.hasNextLine()) {
                    val text = reader.nextLine()
                    thread {
                        if (handler(text)) {
                            server.stopClient(0)
                        }
                    }
                    println("Received: $text")
                }
                if (client.isClosed || !client.isConnected)
                    running = false
            }
        } catch (e: Exception) {
            if (!(e.toString().contains("Socket is closed", true) || e.toString().contains("Socket closed", true)))
                window.display(">>>Exception while ${client.inetAddress.hostAddress} client receiving data:\n$e")
            else
                window.display(">>>Unknown exception while ${client.inetAddress.hostAddress} client receiving data:\n$e")

            server.stopClient(1)
        }
        server.stopClient(0)
    }

    fun close(sendDisconnect : Boolean = true) {    //0 - normal    1 - reader exception    2 - join rejected   3 - connect exception   4 - disconnect received
        if(sendDisconnect)
            sendManager("disconnect", "")
        client.close()
    }

    fun sendManager(module: String, info: Any) {
        when (module) {
            "sendMessage" -> {
                val output = (info as String).replace("\n", "<nl>")
                if (output.isNotBlank())
                    send("mr$output\n".toByteArray())
            }
            "sendCommand" -> {
                val output = (info as String).replace("\n", "<nl>")
                if (output.isNotBlank())
                    send("cr$output\n".toByteArray())
            }
            "switchChannel" -> {
                if(currentChannelName != info)
                    send("cj$info\n".toByteArray())
            }
            "ping" -> {
                send("pi\n".toByteArray())
            }
            "join" -> {
                send("uj$info\n".toByteArray())
            }
            "disconnect" -> {
                send("dc\n".toByteArray())
            }
        }
    }

    private fun send(message: ByteArray) {
        println("Send: $message")
        try {
            writer.write(message)
        } catch (e: Exception) {
            if (!(e.toString().contains("Socket is closed", true) || e.toString().contains("Socket closed", true)))
                window.display(">>>Exception while sending ${client.inetAddress.hostAddress} client data:\n$e")
        }
    }

    private fun handler(message: String): Boolean {
        val modifier = message.take(2)
        val onlyData = message.drop(2)
        println("Handler: $message")

        when (modifier) {
            "jr" -> {   // join rejected
                window.display(">>>Join rejected!")
                // TODO
                server.stopClient(2)
            }
            "pi" -> {   // ping
                sendManager("ping", "")
            }
            "cu" -> {   // channels update
                val output = onlyData.replace("<nl>", "\n").trim().split("\n")
                println(output)
                val channelsMap = HashMap<String,List<String>>()
                val channels = output[0].drop(1).dropLast(1).split(",")
                channels.forEach {
                    val users = it.dropWhile { it != '=' }.drop(2).dropLast(1).split(",")
                    channelsMap[it.trim().takeWhile { it != '=' }] = users
                }
                println(channelsMap)
                currentChannelName = output[1]
                window.channelsUpdate(channelsMap, currentChannelName)
                /*val channelsMap = HashMap<String, ArrayList<String>>()
                val currentChannel = output.takeLast(1).toString().drop(1).dropLast(1)
                output = output.dropLast(2)
                var lastChannel = ""
                var controlSwitch = false

                output.forEach {
                    println(it)
                    when {
                        it == "<channel>" -> {
                            controlSwitch = true
                        }
                        it == "<users>" -> {
                            controlSwitch = false
                        }
                        controlSwitch -> {
                            lastChannel = it
                        }
                        !controlSwitch -> {
                            if (channelsMap[lastChannel] == null)
                                channelsMap[lastChannel] = arrayListOf(it)
                            else
                                channelsMap[lastChannel]!!.add(it)
                        }
                    }
                }
                currentChannelName = currentChannel
                window.channelsUpdate(channelsMap, currentChannel)*/
            }
            "jo" -> {   // join
                println(onlyData)
                if (onlyData != "confirmed") {
                    window.display(">>>$onlyData")
                    server.stopClient(2)
                }
            }
            "mu" -> {   // messages update
                var message = ""
                onlyData.split("<nl>").forEach {
                    message += "$it\n"
                }
                window.display(message.dropLastWhile { it == ' ' || it == '\n' })
            }
            "dc" -> {   // disconnect
                server.stopClient(4)
            }
        }

        return false
    }
}