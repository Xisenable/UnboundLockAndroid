package hrw.es.unboundlock

import android.util.Log
import android.view.View
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.properties.Delegates

class ServerConnection {

    private lateinit var topicMotorDirection: String
    private lateinit var topicCustomText: String
    private lateinit var topicSensor: String
    private lateinit var topicDoorSensorsIsClosed: String // Boolean
    private lateinit var topicDoorSensorsAreActionsNearHandle: String // Boolean

    private lateinit var mqttClient: MqttClient
    private lateinit var view: View

    var lastUpdate by Delegates.notNull<Long>()

    fun connectToServer(serverUrl: String, username: String, password: String, lockID: String, view: View): Boolean {
        lastUpdate = System.currentTimeMillis()
        this.view = view

        // Options
        val options = MqttConnectOptions()
        options.userName = username
        options.password = password.toCharArray()
        options.isAutomaticReconnect = true
        options.isCleanSession = false
        options.connectionTimeout = 10

        topicMotorDirection = "ES/WS20/gruppe8/$lockID/Motor/direction"
        topicCustomText = "ES/WS20/gruppe8/$lockID/CustomUserText"
        topicDoorSensorsIsClosed = "ES/WS20/gruppe8/$lockID/DoorSensors/isClosed"
        topicDoorSensorsAreActionsNearHandle = "ES/WS20/gruppe8/$lockID/DoorSensors/actionsNearDoorHandle"

        topicSensor = "ES/WS20/gruppe8/$lockID/sensor"


        /*
          snprintf(topic_doorSensors, TOPIC_BUFFER_SIZE, "%s%lu%s", topic_root, unitId, "/DoorSensors/#");
          snprintf(topic_motor, TOPIC_BUFFER_SIZE, "%s%lu%s", topic_root, unitId, "/Motor/#");


          snprintf(topic_doorSensors_isClosed, TOPIC_BUFFER_SIZE, "%s%lu%s", topic_root, unitId, "/DoorSensors/isClosed");
          snprintf(topic_doorSensors_areActionsNearHandle, TOPIC_BUFFER_SIZE, "%s%lu%s", topic_root, unitId, "/DoorSensors/actionsNearDoorHandle");
          snprintf(topic_motor_direction, TOPIC_BUFFER_SIZE, "%s%lu%s", topic_root, unitId, "/Motor/direction");
          snprintf(topic_customText, TOPIC_BUFFER_SIZE, "%s%lu%s", topic_root, unitId, "/CustomUserText");
         */

        // Connect
        return connect(serverUrl, options);
    }

    private fun connect(serverUrl: String, options: MqttConnectOptions): Boolean {
        try {
            mqttClient = MqttClient(serverUrl, MqttClient.generateClientId(), MemoryPersistence())

            println("connecting to $serverUrl")
            mqttClient.connect(options)
            mqttClient.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    println("reconnected to $serverUrl")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    Log.d("TAG", "Incoming message from $topic: " + message.toString())
                    //messageCallBack?.invoke(topic, message)
                }

                override fun connectionLost(cause: Throwable?) {
                    Log.d("TAG", "The Connection was lost.")
                    // Toast.makeText(mainActivity.applicationContext, "The Connection was lost.", Toast.LENGTH_SHORT).show()
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {

                }
            })

            while (!mqttClient.isConnected) {
                println("connecting... to $serverUrl")
            }
            println("connected to $serverUrl")

            return true
        } catch (e: MqttException) {
            e.printStackTrace()
            return false
        }
    }

    fun subscribeToTopics() {
        println("subscribe to topics")
        subscribeTopic(topicMotorDirection)
        subscribeTopic(topicSensor)
        subscribeTopic(topicDoorSensorsIsClosed)
        subscribeTopic(topicDoorSensorsIsClosed)
    }

    fun publishStatus(status: String) {
        if (::mqttClient.isInitialized && !mqttClient.isConnected) {
            return
        }

        val message = MqttMessage()
        message.payload = status.toByteArray()

        mqttClient.publish(topicMotorDirection, message)
    }

    fun publishMonitorText(text: String) {
        if (::mqttClient.isInitialized && !mqttClient.isConnected) {
            return
        }

        val message = MqttMessage()
        message.payload = text.toByteArray()

        mqttClient.publish(topicCustomText, message)
    }

    private fun subscribeTopic(topicPath: String) {
        if (!mqttClient.isConnected) {
            println("client not connected")
            return
        }

        mqttClient.subscribe(topicPath, object : IMqttMessageListener {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                time()
                Log.d("TAG", "Incoming message from $topic: " + message.toString())
                Storage.secondFragment.displayText("Incoming message from $topic: " + message.toString())

                if (topic != null) {
                    processTopic(topic, message.toString())
                }
            }
        })
    }

    /**
     *
     */
    private fun processTopic(topic: String, message: String) {
        if (topic == topicMotorDirection) {
            println("received status")
            Storage.secondFragment.updateStatus(message)
        } else if (topic == topicSensor) {
            println("received sensor")
        } else if (topic == topicDoorSensorsIsClosed) {
            Storage.secondFragment.updateIsClosed(message)
            println("received topicDoorSensorsIsClosed")
        } else if (topic == topicDoorSensorsAreActionsNearHandle) {
            Storage.secondFragment.updateActionsNearDoorHandle(message)
            println("received topicDoorSensorsAreActionsNearHandle")
        }
    }

    fun logout(): Boolean {
        mqttClient.disconnect()
        return true
    }

    private fun time() {
        lastUpdate = System.currentTimeMillis()

        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss") // yyyy-MM-dd HH:mm:ss.SSS
        val formatted = current.format(formatter)

        Storage.secondFragment.updateLastUpdate(formatted)
    }

}