package hrw.es.unboundlock

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.ContextMenu
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Notifications
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = Notification()
        notification.create(notificationManager)
        Storage.updateNotification(notification)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        Storage.updateMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (item.itemId == R.id.menuLogout) {
            if (Storage.serverConnection != null) {
                val logout = Storage.serverConnection.logout()
                // The user has logged out
                if (logout) {
                    // Change fragment back to the first fragment (the login fragment)
                    Storage.secondFragment.view2.findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
                }
            }

        }

        /*return when (item.itemId) {
            else -> super.onOptionsItemSelected(item)
        }*/

        return super.onOptionsItemSelected(item)
    }
}