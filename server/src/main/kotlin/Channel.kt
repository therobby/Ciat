

class Channel(
        var name : String,
        val default : Boolean = false
) {
    val users = HashMap<String, User>()
    val nickToIp = HashMap<String,String>()

    fun join(ip: String, nickname : String): String {
        var user : User? = null
        server.channels.forEach {
            it.users.forEach { t, u ->
                if (t == ip) {
                    user = u
                    return@forEach
                }
            }
            if (user != null)
                return@forEach
        }
        if(user == null)
            return "confirmed"
        return "rejected"
    }

    fun updateMessages(message : String) {
        users.forEach { _, u ->
            u.sendMessage(message)
        }
    }

    fun updateChannelsList(){
        users.forEach { _, u ->
            u.updateChannels()
        }
    }

}