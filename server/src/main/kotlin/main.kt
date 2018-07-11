import tornadofx.*
import kotlin.collections.HashMap

val window = ServerWindowController()
val server = ServerController()
var headless = false

fun main(args: Array<String>) {
    args.forEach { println(it) }
    if(args.isEmpty() || args[0] != "cmd") {
        launch<ServerApp>(args)
    }
    else{
        headless = true
    }
}