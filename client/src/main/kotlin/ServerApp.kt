import tornadofx.*

class ServerApp : App(LoginWindow::class){
    override fun stop() {
        server.stopClient(0)
        super.stop()
    }
}
//class ServerApp : App(ClientWindow::class)