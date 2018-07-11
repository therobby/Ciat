import java.net.Socket

class User(
        //val handler : ClientHandler,
        client : Socket,
        var nickname: String,
        var currentChannel: Channel) : ClientHandler(client) {
    var ip = ""
    var connected = false
    var su = false

    init {
        super.user = this
        super.start()
    }

    fun sendMessage(message : String){
        super.sendManager("sendMessage", message)
    }

    fun receivedMessage(message: String){
        currentChannel.updateMessages("$nickname: $message")
        if(headless){
            println("<${currentChannel.name}> $nickname: $message")
        }
        else {
            window.display("<umess>$message", nickname, currentChannel.name)
        }
    }

    fun joinChannel(name : String){
        if (user!!.connected) {
            var channel : Channel? = null
            server.channels.forEach {
                if(it.name == name){
                    channel = it
                    return@forEach
                }
            }
            if(channel != null) {
                user!!.currentChannel.users.remove(ip, user!!)
                user!!.currentChannel = channel!!
                channel!!.users[ip] = user!!
                server.channels.forEach {
                    it.updateChannelsList()
                }
                if(!headless)
                    window.usersListUpdate()
                sendManager("channelJoin",name)
            }
            else
                sendManager("sendMessage", "Channel doesn't exist!")
        }
    }

    fun checkPermission(){
        if(server.su.contains(ip)){
            if(server.su[ip]!!.contains(nickname))
                su = true
        }
    }

    fun updateChannels(){
        var output = ""
        val channels = HashMap<String,ArrayList<String>>()
        server.channels.forEach {
            val users = ArrayList<String>()
            it.users.forEach { _, u ->
                users += if(u.su)
                    "<su>${u.nickname}"
                else
                    "<nu>${u.nickname}"
            }
            channels[it.name] = users
        }
        output += channels
        output += "\n${user?.currentChannel?.name}"

        super.sendManager("channelsUpdate", output)
    }
}