package hrw.es.unboundlock

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.view.View
import android.widget.CheckBox

class SharedPref {

    private val preferencesName: String = "UnboundLock"

    /**
     * Removes all Shared Preferences
     */
    fun removeSharedPreferences(view: View) {
        val prefs: SharedPreferences = view.context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    /**
     * Load the Credentials from the Shared Preferences
     */
    fun loadCredentials(view: View): LoginData {
        // Load Shared Preferences
        val prefs: SharedPreferences = view.context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

        // host data
        val prefix = prefs.getString("prefix","");
        val ip = prefs.getString("ip","");
        val port = prefs.getString("port","");
        val host = "$prefix$ip:$port"

        // Username & Password
        val usernameEncrypted = prefs.getString("username","");
        val usernameIV = prefs.getString("usernameIV", "")
        val passwordEncrypted = prefs.getString("password","");
        val passwordIV = prefs.getString("passwordIV", "")

        // Lock ID
        val lockID = prefs.getString("lockID","")

        // Decrypt
        val encryption = Encryption()
        val username = encryption.decrypt(Base64.decode(usernameEncrypted, Base64.NO_WRAP), Base64.decode(usernameIV, Base64.NO_WRAP))
        val password = encryption.decrypt(Base64.decode(passwordEncrypted, Base64.NO_WRAP), Base64.decode(passwordIV, Base64.NO_WRAP))

        return LoginData(prefix.toString(), ip.toString(), port.toString(), host, username, password, lockID.toString())
    }

    /**
     * Save the Credentials to the Shared Preferences
     */
    fun saveCredentials(view: View, username: String, password: String, prefix: String, ip: String, port: String, lockID: String, automaticLogin: Boolean) {
        // Encrypt
        val encryption = Encryption()
        val encryptedUsernamePair = encryption.encrypt(username)
        val encryptedPasswordPair = encryption.encrypt(password)

        // Shared Preferences
        val prefs: SharedPreferences = view.context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Convert the ByteArrays to String with Base64
        editor.putString("username", Base64.encodeToString(encryptedUsernamePair.first, Base64.NO_WRAP))
        editor.putString("usernameIV", Base64.encodeToString(encryptedUsernamePair.second, Base64.NO_WRAP))

        editor.putString("password", Base64.encodeToString(encryptedPasswordPair.first, Base64.NO_WRAP))
        editor.putString("passwordIV", Base64.encodeToString(encryptedPasswordPair.second, Base64.NO_WRAP))

        editor.putString("prefix", prefix)
        editor.putString("ip", ip)
        editor.putString("port", port)

        editor.putString("lockID", lockID)

        editor.putBoolean("automaticLogin", automaticLogin)
        editor.putBoolean("saveCredentials", true)
        editor.apply()
    }

    fun getUpdataDataInterval(view: View): Int {
        val prefs: SharedPreferences = view.context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        return prefs.getInt("updateDataInterval",60)
    }

    fun updateDataInterval(interval: Int, view: View) {
        val prefs: SharedPreferences = view.context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putInt("updateDataInterval", interval)
        editor.apply()
    }

}