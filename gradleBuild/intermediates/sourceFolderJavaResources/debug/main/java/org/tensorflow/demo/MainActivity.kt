package org.tensorflow.demo

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import com.example.henry.optimizer2.utils.MemoryInfo
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var button: Button? = null
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                var scale = -1
                var level = -1
                var voltage = -1
                var temp = -1
                val MemoryInfomemoryInfo = ActivityManager.MemoryInfo()

                level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
                val mBatteryManager = application.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val energy = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
                val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                val speedMbps = wifiManager.connectionInfo.linkSpeed as Int
                var memInfo = MemoryInfo()
                message.setText("Manufacturer "+ Build.MANUFACTURER + "Brand " + Build.BRAND + "Type" + Build.TYPE)
                button = findViewById(R.id.tf_button) as Button
                button!!.setOnClickListener(View.OnClickListener {
                    val intent = Intent(this@MainActivity, ClassifierActivity::class.java)
                    startActivity(intent)
                })
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
