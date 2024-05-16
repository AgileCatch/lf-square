import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ShakeDetector(private val context: Context, private val callback: () -> Unit) :
    SensorEventListener {

    companion object {
        private const val SHAKE_THRESHOLD = 2.7f
        private const val SHAKE_SLOP_TIME_MS = 500
        private const val SHAKE_COUNT_RESET_TIME_MS = 3000

        private var lastShakeTime: Long = 0
        private var shakeCount: Int = 0

        private lateinit var sensorManager: SensorManager
        private lateinit var accelerometer: Sensor
    }

    init {
        setupSensor()
    }

    private fun setupSensor() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastShakeTime > SHAKE_SLOP_TIME_MS) {
                    val acceleration =
                        Math.sqrt((it.values[0] * it.values[0] + it.values[1] * it.values[1] + it.values[2] * it.values[2]).toDouble()).toFloat() - SensorManager.GRAVITY_EARTH
                    if (acceleration > SHAKE_THRESHOLD) {
                        shakeCount++
                        lastShakeTime = currentTime
                        if (shakeCount >= 2) {
                            // 흔들림 팝업 표시
                            callback.invoke()
                            shakeCount = 0
                            lastShakeTime = 0
                        }
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 변경되지 않음
    }

    fun unregisterListener() {
        sensorManager.unregisterListener(this)
    }
}
