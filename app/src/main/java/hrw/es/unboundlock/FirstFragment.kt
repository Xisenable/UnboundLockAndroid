package hrw.es.unboundlock

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    lateinit var view2: View

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Storage.updateFirstFragment(this)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view2 = view
        Storage.updateFirstFragment(this)

        val spinner: Spinner = view.findViewById(R.id.spinnerArt)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(view.context, R.array.spinnerArtContent, android.R.layout.simple_spinner_item).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        view.findViewById<Button>(R.id.loginActivityConnectButton).setOnClickListener {
            login(view)
        }

        view.findViewById<CheckBox>(R.id.checkBoxSaveCredentials).setOnCheckedChangeListener { _, isChecked ->
            // The "Auto Login" Function is only available when the user gave permission to save the credentials
            view.findViewById<CheckBox>(R.id.checkBoxAutomaticLogin).isEnabled = isChecked
        }

        startupSetup(view)
    }

    /**
     *
     */
    private fun startupSetup(view: View) {
        // Load Shared Preferences
        val prefs: SharedPreferences = view.context.getSharedPreferences("UnboundLock", MODE_PRIVATE)
        val saveCredentials = prefs.getBoolean("saveCredentials", false)
        val autoLogin = prefs.getBoolean("automaticLogin", false)

        // User gave permission to save the credentials
        if (saveCredentials) {
            view.findViewById<CheckBox>(R.id.checkBoxSaveCredentials).isChecked = true
            view.findViewById<CheckBox>(R.id.checkBoxAutomaticLogin).isEnabled = true
            loadCredentials(view)

            // User gave permission to login automatically (only possible if the credentials were also saved)
            if (autoLogin) {
                view.findViewById<CheckBox>(R.id.checkBoxAutomaticLogin).isChecked = true
                login(view)
            }
        }
    }

    /**
     * Get the credentials from the TextViews and attempt to login to the server
     */
    private fun login(view: View) {
        changeConnectButtonState(false)
        changeProgressBarVisibility(VISIBLE)

        // Host data
        val prefix = view.findViewById<Spinner>(R.id.spinnerArt).selectedItem.toString()
        val ip = view.findViewById<TextView>(R.id.textViewHostIP).text
        val port = view.findViewById<TextView>(R.id.textViewPort).text
        val host = "$prefix$ip:$port"

        // Username & Password
        val username = view.findViewById<TextView>(R.id.textViewUsername).text.toString()
        val password = view.findViewById<TextView>(R.id.textViewPassword).text.toString()

        // Lock ID
        val lockID = view.findViewById<TextView>(R.id.textViewLockID).text.toString()

        println("used ip: $host")
        println("username: $username & password: $password")
        println("lock id: $lockID")

        // Connect
        val serverConnection = ServerConnection()
        Storage.updateServerConnection(serverConnection)

        // Thread
        val mainHandler = Handler(Looper.getMainLooper())
        val myRunnable = Runnable() {
            run {
                connect(serverConnection, prefix, ip.toString(), port.toString(), username, password, lockID, view)
            }
        }
        mainHandler.post(myRunnable);

        /*Thread {
            connect(serverConnection, prefix, ip.toString(), port.toString(), username, password, lockID, view)
        }.start()*/
    }

    /**
     * Connect to the server with the credentials
     */
    private fun connect(serverConnection: ServerConnection, prefix: String, ip: String, port: String, username: String, password: String, lockID: String, view: View) {
        val sharedPref = SharedPref()
        val saveCredentials = view.findViewById<CheckBox>(R.id.checkBoxSaveCredentials).isChecked
        if (!saveCredentials) {
            sharedPref.removeSharedPreferences(view)
        }

        val connectedToServer = serverConnection.connectToServer("$prefix$ip:$port", username, password, lockID, view)
        if (connectedToServer) {
            // Save the userData only if the connection to the server was successful
            if (saveCredentials) {
                // todo when the user checked the autoLogin in a previous login, it is unnecessary to save the credentials a second time
                val automaticLogin = view.findViewById<CheckBox>(R.id.checkBoxAutomaticLogin).isChecked
                sharedPref.saveCredentials(view, username, password, prefix, ip, port, lockID, automaticLogin)
            }

            Storage.firstFragment.displayText("Connection successful.")
            Storage.menu.findItem(R.id.menuLogout).isEnabled = true // the user can now logout
            Storage.firstFragment.changeFragment(R.id.action_FirstFragment_to_SecondFragment)
        } else {
            Storage.firstFragment.displayText("Couldn't connect to the server. Try again.")
        }
        Storage.firstFragment.changeConnectButtonState(true)
        changeProgressBarVisibility(GONE)
    }

    /**
     * Load the Credentials from the Shared Preferences and saved the data to the TextViews
     */
    private fun loadCredentials(view: View) {
        val sharedPref = SharedPref()
        val loginData = sharedPref.loadCredentials(view)

        println("used ip: ${loginData.host}")
        println("username: ${loginData.username} & password: ${loginData.password}")
        println("lock id: ${loginData.lockID}")

        // Set the values
        val spinnerPrefix = view.findViewById<Spinner>(R.id.spinnerArt)
        spinnerPrefix.setSelection(getSpinnerIndex(spinnerPrefix, loginData.prefix))

        view.findViewById<TextView>(R.id.textViewHostIP).text = loginData.ip
        view.findViewById<TextView>(R.id.textViewPort).text = loginData.port

        view.findViewById<TextView>(R.id.textViewUsername).text = loginData.username
        view.findViewById<TextView>(R.id.textViewPassword).text = loginData.password

        view.findViewById<TextView>(R.id.textViewLockID).text = loginData.lockID
    }

    private fun getSpinnerIndex(spinner: Spinner, myString: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(myString, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }

    fun displayText(displayText: String) {
        Snackbar.make(view2, displayText, Snackbar.LENGTH_LONG).show()
    }

    private fun changeFragment(fromFragmentToFragment: Int) {
        view2.findNavController().navigate(fromFragmentToFragment)
    }

    private fun changeConnectButtonState(state: Boolean) {
        view2.findViewById<Button>(R.id.loginActivityConnectButton).isEnabled = state
    }

    private fun changeProgressBarVisibility(visibility: Int) {
        view2.findViewById<ProgressBar>(R.id.progressBar).visibility = visibility
    }
}