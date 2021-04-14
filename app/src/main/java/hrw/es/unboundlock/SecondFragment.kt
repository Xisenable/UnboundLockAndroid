package hrw.es.unboundlock

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDateTime

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    lateinit var view2: View
    lateinit var mainHandler: Handler
    private lateinit var lastUpdateIntervalTask : Runnable

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Storage.updateSecondFragment(this)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view2 = view
        Storage.updateSecondFragment(this)
        Storage.serverConnection.subscribeToTopics()

        view.findViewById<Button>(R.id.buttonPublishMonitorText).setOnClickListener {
            publishMonitorText()
        }

        view.findViewById<ToggleButton>(R.id.toggleButton).setOnCheckedChangeListener { _, isChecked ->
            publishStatus(isChecked)
        }

        view.findViewById<Button>(R.id.buttonLastUpdateIntervalUpdate).setOnClickListener {
            updateInterval()
        }

        // LastUpdateInterval
        val sharedPref = SharedPref()
        val updataDataInterval = sharedPref.getUpdataDataInterval(view2)
        view.findViewById<EditText>(R.id.editTextNumberLastUpdateInterval).setText(updataDataInterval.toString())
        startLastUpdateIntervalLoop(updataDataInterval)
    }

    /**
     * 
     */
    private fun publishMonitorText() {
        val text = view2.findViewById<TextView>(R.id.editTextStatusText).text.toString()
        Storage.serverConnection.publishMonitorText(text)
    }

    /**
     * Publish the Status: 1 for open, 0 for closed
     */
    private fun publishStatus(isChecked: Boolean) {
        Storage.serverConnection.publishStatus(if (isChecked) "0" else "1")
    }

    fun displayText(displayText: String) {
        Snackbar.make(view2, displayText, Snackbar.LENGTH_LONG).show()
    }

    fun updateLastUpdate(time: String) {
        view2.findViewById<TextView>(R.id.textViewLastUpdate).text = time
    }

    /**
     *
     */
    fun updateStatus(status: String) {
        val toggleButton = view2.findViewById<ToggleButton>(R.id.toggleButton)
        val lockStatusTextView = view2.findViewById<TextView>(R.id.textViewLockStatus)

        // todo display the notification only when the status is different from the previous
        when (status) {
            "1" -> {
                displayText("Lock Status was changed to Closed.")
                lockStatusTextView.text = "Lock is closed."
                toggleButton.isChecked = false
                Storage.notification.displayNotification("Lock Status was changed.", "Lock Status was changed to Closed.", view2)
            }
            "0" -> {
                displayText("Lock Status was changed to Open.")
                lockStatusTextView.text = "Lock is open."
                toggleButton.isChecked = true
                Storage.notification.displayNotification("Lock Status was changed.", "Lock Status was changed to Open.", view2)
            }
            else -> {
                displayText("An error occurred in the Lock Status.")
                lockStatusTextView.text = "Lock has error." // todo better
                toggleButton.isChecked = true
                Storage.notification.displayNotification("Lock Status was changed.", "An error occurred in the Lock Status.", view2)
            }
        }
    }

    fun updateIsClosed(status: String) {
        when (status) {
            "1" -> {
                displayText("The door was successfully closed.")
                Storage.notification.displayNotification("Door closed status.", "The door was successfully closed.", view2)
            }
            "0" -> {
                //displayText("Currently there are no actions near door handle.")
                //Storage.notification.displayNotification("Door closed status.", "Currently there are no actions near door handle.", view2)
            }
            else -> {
                displayText("An error occurred in the door closed status.")
                Storage.notification.displayNotification("Door closed status.", "An error occurred in the door closed status.", view2)
            }
        }
    }

    fun updateActionsNearDoorHandle(status: String) {
        when (status) {
            "1" -> {
                displayText("Attention! There are actions near door handle.")
                Storage.notification.displayNotification("Actions near door handle.", "Attention! There are actions near door handle.", view2)
            }
            "0" -> {
                //displayText("Currently there are no actions near door handle.")
                //Storage.notification.displayNotification("Actions near door handle.", "Currently there are no actions near door handle.", view2)
            }
            else -> {
                displayText("An error occurred in the actionsNearDoorHandle.")
                Storage.notification.displayNotification("Actions near door handle.", "An error occurred in the actionsNearDoorHandle.", view2)
            }
        }
    }

    private fun startLastUpdateIntervalLoop(intervalInSeconds : Int) {
        // LastUpdateInterval
        mainHandler = Handler(Looper.getMainLooper())
        lastUpdateIntervalTask = object : Runnable {
            override fun run() {
                mainHandler.postDelayed(this, intervalInSeconds.toLong()*1000)
                checkLastUpdateInterval(intervalInSeconds.toLong())
            }
        }
        lastUpdateIntervalTask.run()
    }

    fun resumeLastUpdateIntervalLoop() {
        mainHandler.post(lastUpdateIntervalTask)
    }

    private fun pauseLastUpdateIntervalLoop() {
        mainHandler.removeCallbacks(lastUpdateIntervalTask)
    }

    private fun updateInterval() {
        val interval = view2.findViewById<EditText>(R.id.editTextNumberLastUpdateInterval).text.toString()
        val sharedPref = SharedPref()
        sharedPref.updateDataInterval(interval.toInt(), view2)
        pauseLastUpdateIntervalLoop()
        startLastUpdateIntervalLoop(interval.toInt())
    }

    private fun checkLastUpdateInterval(lastUpdateIntervalInSeconds: Long) {
        println(LocalDateTime.now().toString())

        val currentTimeInMill = System.currentTimeMillis()
        val lastUpdateInMill = Storage.serverConnection.lastUpdate

        if (lastUpdateInMill + lastUpdateIntervalInSeconds*1000 <= currentTimeInMill) {
            // oh noes
            Storage.secondFragment.displayText("Last Update from the lock is more than ${lastUpdateIntervalInSeconds.toInt()} seconds ago.")
            Storage.notification.displayNotification("Last Update", "Last Update from the lock is more than ${lastUpdateIntervalInSeconds.toInt()} seconds ago.", view2)
        }
    }
}