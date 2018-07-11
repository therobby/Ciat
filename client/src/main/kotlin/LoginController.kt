class LoginController {
    var window : LoginWindow? = null

    fun change(){
        window?.changeToMainView()
    }
    fun error(error : String){
        window?.error(error)
    }
}