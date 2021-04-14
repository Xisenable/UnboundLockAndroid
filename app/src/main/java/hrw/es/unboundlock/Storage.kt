package hrw.es.unboundlock

import android.view.Menu

object Storage {

    lateinit var serverConnection: ServerConnection
    lateinit var firstFragment: FirstFragment
    lateinit var secondFragment: SecondFragment
    lateinit var menu: Menu
    lateinit var notification: Notification

    fun gSecondFragment(): SecondFragment {
        if (this::secondFragment.isInitialized)
            return secondFragment
        return SecondFragment()
    }

    fun updateServerConnection(serverConnection: ServerConnection) {
        this.serverConnection = serverConnection
    }

    fun updateSecondFragment(secondFragment: SecondFragment) {
        this.secondFragment = secondFragment
    }

    fun updateFirstFragment(firstFragment: FirstFragment) {
        this.firstFragment = firstFragment
    }

    fun updateMenu(menu: Menu) {
        this.menu = menu
    }

    fun updateNotification(notification: Notification) {
        this.notification = notification
    }



}