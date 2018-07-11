import java.io.File
import java.net.ServerSocket
import kotlin.system.exitProcess


class ServerController {
    private var running = true
    private val serverThread = Thread(Runnable { mainLoop() })
    private val commands = HashMap<String, String>()
    val channels = arrayListOf(Channel("Default",true))
    val properties = HashMap<String, String>()
    val su = HashMap<String, List<String>>()

    init {
        commands["Help"] = "Displays this message"
        commands["Exit"] = "Shutdowns server"
        commands["Kick <user>"] = "Kicks selected user"
        commands["Say <channel> <message>"] = "Says something on selected channel"
        commands["Su <channel> <user>"] = "Gives selected user access to all commands"
        commands["uSu <channel> <user>"] = "Removes selected user access to all commands"
        commands["Nc <name>"] = "New channel with name"
        commands["Rc <name>"] = "Remove channel with name"
        commands["RenChan <current name> <new name>"] = "Rename channel name"

        // Server thread
        if (loadProperties()) {
            loadSu()
            serverThread.start()
        }
    }

    private fun mainLoop() {
        val server: ServerSocket
        val port = properties["port"]?.toInt() ?: 9999
        try {
            server = ServerSocket(port)
        } catch (e: Exception) {
            if (e.toString().contains("Address already in use", true))
                window.display("<error>Exception while starting server!\nAddress already in use")
            else
                window.display("<error>Unknown exception while starting server!\n$e")
            return
        }

        if (headless)
            println("SERVER: Server started on port $port!")
        else
            window.display("<smess>Server started on port $port!")

        while (running) {
            val client = server.accept()
            var alreadyExists = false
            channels.forEach {
                if (it.users.containsKey(client.inetAddress.hostAddress))
                    alreadyExists = true
            }

            if (!alreadyExists) {
                if (headless)
                    println("SERVER: Client connected: ${client.inetAddress.hostAddress}")
                else
                    window.display("<smess>Client connected: ${client.inetAddress.hostAddress}")
                /*channels[0].users[client.inetAddress.hostAddress] = */User(client, "", channels[0])
            } else {
                ClientHandler(client).sendManager("joinReject", "jr")
                client.close()
            }
        }
    }

    private fun loadSu() {
        val suFile = File("su.txt")

        if (suFile.isFile) {
            loadSuFile()
        } else {
            generateSuFile()
        }
    }

    private fun loadSuFile() {
        val suFile = File("su.txt")
        if (suFile.length() > 0)
            suFile.useLines { lines ->
                lines.forEach {
                    if (it.isNotBlank()) {
                        val suIp = it.replace(" ", "").takeWhile { it != '=' }
                        println(suIp)
                        val suUsers = it.dropWhile { it != '=' }.drop(1).split("<nSu>").toList()
                        try {
                            su[suIp] = suUsers
                        } catch (e: Exception) {
                            if (headless)
                                println("ERROR: Exception while loading su.txt!\n" +
                                        "$e")
                            else
                                window.display("<error>Exception while loading su.txt!\n" +
                                        "$e")
                            return
                        }
                    }
                }
            }
        if (headless)
            println("SERVER: File su.txt loaded correctly!")
        else
            window.display("<smess>File su.txt loaded correctly!")
    }

    private fun updateSu() {
        channels.forEach {
            it.updateChannelsList()
        }
        var saveError = false
        val suFile = File("su.txt")
        suFile.printWriter().use { out ->
            su.forEach { t, u ->
                try {
                    out.print("$t=")
                    u.forEach {
                        if (u.lastIndex == u.lastIndexOf(it))
                            out.print(it)
                        else
                            out.print("$it<nSu>")
                    }
                } catch (e: Exception) {
                    if (headless)
                        println("ERROR: Exception while saving su.txt!\n" +
                                "$e")
                    else
                        window.display("<error>Exception while saving su.txt!\n" +
                                "$e")
                    saveError = true
                    return@forEach
                }
            }
        }
        if (saveError)
            return
        if (headless)
            println("SERVER: File su.txt saved correctly!")
        else
            window.display("<smess>File su.txt saved correctly!")
    }

    private fun generateSuFile() {
        File("su.txt").createNewFile()
        if (headless)
            println("SERVER: File su.txt generated correctly!")
        else
            window.display("<smess>File su.txt generated correctly!")
    }

    private fun loadProperties(): Boolean {
        val propFile = File("properties.txt")

        return if (propFile.isFile) {
            loadPropertiesFile()
        } else {
            generatePropertiesFile()
            loadPropertiesFile()
        }
    }

    private fun loadPropertiesFile(): Boolean {
        val propFile = File("properties.txt")
        propFile.useLines { lines ->
            lines.forEach {
                if (it.isNotBlank()) {
                    val property = it.replace(" ", "").split("=")
                    println(property)
                    try {
                        properties[property[0]] = property[1]
                    } catch (e: Exception) {
                        if (headless)
                            println("ERROR: Exception while loading properties.txt!\n" +
                                    "$e")
                        else
                            window.display("<error>Exception while loading properties.txt!\n" +
                                    "$e")
                        return false
                    }
                }
            }
        }
        if (headless)
            println("SERVER: File properties.txt loaded correctly!")
        else
            window.display("<smess>File properties.txt loaded correctly!")
        return true
    }

    private fun generatePropertiesFile() {
        val prop = HashMap<String, String>()
        val propFile = File("properties.txt")

        prop["port"] = "9999"
        prop["saveLastLog"] = "true"

        propFile.printWriter().use { out ->
            prop.forEach {
                out.println(it)
            }
        }
        if (headless)
            println("SERVER: File properties.txt generated correctly!")
        else
            window.display("<smess>File properties.txt generated correctly!")
    }

    fun stopServer(status: Int) {
        running = false
        try {
            channels.forEach {
                it.users.forEach { _, u ->
                    u.close()
                }
            }
        } catch (e: Exception) {
            if (headless)
                println("ERROR: Exception while stopping server!\n" +
                        "$e")
            else
                window.display("<error>Exception while stopping server!\n" +
                        "$e")
        }
        exitProcess(status)
    }

    fun processCommand(command_: String): String {
        val command = command_.split(" ")
        if (command.isEmpty())
            return "Invalid Command\n"
        val transformCommands = ArrayList<String>()
        commands.forEach { transformCommands += it.key.toLowerCase().takeWhile { it.isLetterOrDigit() } }
        if (transformCommands.contains(command[0].toLowerCase())) {
            var output = ""
            when (command[0].toLowerCase()) {

                "help" -> {
                    output += "Commands:\n"
                    commands.forEach { t, u ->
                        output += "$t = $u\n"
                    }
                    return output
                }

                "kick" -> {
                    println(command)
                    try {
                        channels.forEach {
                            println(it.nickToIp[command[1]])
                            if (it.users.containsKey(it.nickToIp[command[1]])) {
                                it.users[it.nickToIp[command[1]]]!!.sendManager("kick", "")
                                return ""
                            }
                        }
                    } catch (e: Exception) {
                        return "User ${command[1]} doesn't exist!\n"
                    }
                    return "User ${command[1]} doesn't exist!\n"
                }

                "exit" -> {
                    server.stopServer(0)
                    return "Server is shutting down!\n"
                }

                "say" -> {
                    channels.forEach {
                        if (it.name == command[1]) {
                            var message = ""
                            for (i in 2 until command.size)
                                message += "${command[i]}\n"
                            it.updateMessages("SERVER\n${command[2]}")
                            if (headless)
                                println("<${command[1]}> SERVER: $message")
                            else
                                window.display(message, "SERVER")
                            return ""
                        }
                    }
                    return "Invalid channel\n"
                }

                "su" -> {
                    channels.forEach {
                        output = "Invalid user\n"
                        it.users.forEach { _, u ->
                            if (u.nickname == command[1]) {
                                if (su.containsKey(u.ip)) {
                                    if (su[u.ip]!!.contains(u.nickname)) {
                                        output = "This user already is su!"
                                        return@forEach
                                    }
                                    su[u.ip] = su[u.ip]!!.plus(u.nickname)
                                } else {
                                    su[u.ip] = listOf(u.nickname)
                                }
                                u.su = true
                            } else {
                                output = "Invalid user\n"
                                return@forEach
                            }
                            updateSu()
                            output = "${command[1]} is su!\n"
                            return@forEach
                        }
                        if(output != "Invalid user\n")
                            return@forEach
                    }
                    return output
                }

                "usu" -> {
                    channels.forEach {
                        output = "Invalid user\n"
                        it.users.forEach { _, u ->
                            if (u.nickname == command[1]) {
                                if (su.containsKey(u.ip)) {
                                    if (su[u.ip]!!.contains(u.nickname))
                                        su[u.ip] = su[u.ip]!!.minusElement(u.nickname)
                                    else {
                                        output = "${command[1]} already isn't su!\n"
                                        return@forEach
                                    }
                                } else {
                                    output = "${command[1]} already isn't su!\n"
                                    return@forEach
                                }
                                if (su[u.ip]!!.isEmpty())
                                    su.remove(u.ip)
                                u.su = false
                                updateSu()
                                output = "uSued ${command[1]}!\n"
                                return@forEach
                            }
                        }
                        if(output != "Invalid user\n")
                            return@forEach
                    }
                    return output
                }

                "nc" -> {
                    var exists = false
                    channels.forEach {
                        if(it.name == command[1]){
                            exists = true
                            return@forEach
                        }
                    }
                    return if(!exists) {
                        channels.add(Channel(command[1]))
                        channels.forEach {
                            it.updateChannelsList()
                        }
                        if(!headless)
                            window.usersListUpdate()
                        "Channel ${command[1]} has been created!\n"
                    }
                    else
                        "Channel already exists!\n"
                }

                "rc" -> {
                    var channel : Channel? = null
                    channels.forEach {
                        if(it.name == command[1]){
                            channel = it
                            return@forEach
                        }
                    }
                    return if(channel != null) {
                        if(channel!!.default)
                            "Cannot remove default channel!\n"
                        else {
                            val defaultChannel = channels[0]
                            channel!!.users.values.forEach {
                                it.joinChannel(defaultChannel.name)
                            }
                            channels.remove(channel!!)
                            channels.forEach {
                                it.updateChannelsList()
                            }
                            if (!headless)
                                window.usersListUpdate()
                            "Channel ${command[1]} has been removed!\n"
                        }
                    }
                    else
                        "Channel doesn't exists!\n"
                }

                "renchan" -> {
                    channels.forEach {
                        if(it.name == command[1]) {
                            it.name = command[2]
                            return@forEach
                        }
                    }
                    channels.forEach {
                        it.updateChannelsList()
                    }
                    window.usersListUpdate()
                    return "Changed ${command[1]} to ${command[2]}"
                }
            }
        }
        return "Invalid Command, type help for commands list!\n"
    }
}