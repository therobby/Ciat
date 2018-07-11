import tornadofx.*

val window = ClientWindowController()
var server = ClientController()
val login = LoginController()

fun main(args: Array<String>) {
    launch<ServerApp>(args)
}