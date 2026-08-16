
package com.example.freelacecontroller

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.media.audiofx.BassBoost
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvBattery: TextView
    private lateinit var seekBarBass: SeekBar
    private lateinit var btnRefresh: Button
    
    private var bassBoost: BassBoost? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvBattery = findViewById(R.id.tvBattery)
        seekBarBass = findViewById(R.id.seekBarBass)
        btnRefresh = findViewById(R.id.btnRefresh)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        setupBassBoost()
        checkConnectedDevice()

        btnRefresh.setOnClickListener {
            checkConnectedDevice()
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkConnectedDevice() {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        var found = false

        pairedDevices?.forEach { device ->
            if (device.name?.contains("FreeLace", ignoreCase = true) == true || 
                device.name?.contains("CM70-C", ignoreCase = true) == true) {
                
                tvStatus.text = "متصلة: ${device.name}"
                tvStatus.setTextColor(getColor(android.R.color.holo_green_light))
                
                val batteryLevel = getBatteryLevel(device)
                tvBattery.text = if (batteryLevel >= 0) "$batteryLevel%" else "متصلة"
                found = true
            }
        }

        if (!found) {
            tvStatus.text = "السماعة غير متصلة بالبلوتوث"
            tvStatus.setTextColor(getColor(android.R.color.holo_red_light))
            tvBattery.text = "-- %"
        }
    }

    private fun getBatteryLevel(device: BluetoothDevice): Int {
        return try {
            val method = device.javaClass.getMethod("getBatteryLevel")
            method.invoke(device) as Int
        } catch (e: Exception) {
            -1
        }
    }

    private fun setupBassBoost() {
        try {
            bassBoost = BassBoost(0, 0)
            bassBoost?.enabled = true

            seekBarBass.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (bassBoost?.strengthSupported == true) {
                        bassBoost?.setStrength(progress.toShort())
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bassBoost?.release()
    }
}
