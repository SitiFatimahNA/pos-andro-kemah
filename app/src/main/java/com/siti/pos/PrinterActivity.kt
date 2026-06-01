package com.siti.pos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import adapter.BluetoothDeviceAdapter
import java.io.OutputStream
import java.util.UUID
import model.ModelPrinter
import android.annotation.SuppressLint

class PrinterActivity : AppCompatActivity() {

    private lateinit var ivKembali: ImageView
    private lateinit var tvNamaPrinter: TextView
    private lateinit var tvMacPrinter: TextView
    private lateinit var tvStatusKoneksi: TextView
    private lateinit var btnScanBluetooth: Button
    private lateinit var rvPerangkat: RecyclerView
    private lateinit var btn58mm: Button
    private lateinit var btn80mm: Button
    private lateinit var switchCetakOtomatis: Switch
    private lateinit var btnTesCetak: Button
    private lateinit var btnSimpanSetting: Button
    private lateinit var printerManager: PrinterManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceAdapter: BluetoothDeviceAdapter

    private var selectedDevice: BluetoothDevice? = null
    private var ukuranKertas: String = "58mm"
    private var bluetoothSocket: BluetoothSocket? = null

    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) scanBluetooth()
        else Toast.makeText(this, "Izin Bluetooth diperlukan", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printer)

        printerManager = PrinterManager(this)
        val btManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = btManager.adapter

        initViews()
        loadSetting()
        setupListeners()
    }

    private fun initViews() {
        ivKembali = findViewById(R.id.ivKembali)
        tvNamaPrinter = findViewById(R.id.tvNamaPrinter)
        tvMacPrinter = findViewById(R.id.tvMacPrinter)
        tvStatusKoneksi = findViewById(R.id.tvStatusKoneksi)
        btnScanBluetooth = findViewById(R.id.btnScanBluetooth)
        rvPerangkat = findViewById(R.id.rvPerangkat)
        btn58mm = findViewById(R.id.btn58mm)
        btn80mm = findViewById(R.id.btn80mm)
        switchCetakOtomatis = findViewById(R.id.switchCetakOtomatis)
        btnTesCetak = findViewById(R.id.btnTesCetak)
        btnSimpanSetting = findViewById(R.id.btnSimpanSetting)

        deviceAdapter = BluetoothDeviceAdapter(mutableListOf()) { device ->
            pilihPrinter(device)
        }
        rvPerangkat.layoutManager = LinearLayoutManager(this)
        rvPerangkat.adapter = deviceAdapter
    }

    private fun loadSetting() {
        val setting = printerManager.getSetting()
        if (setting.alamatMac.isNotEmpty()) {
            tvNamaPrinter.text = setting.namaPrinter.ifEmpty { "Printer Bluetooth" }
            tvMacPrinter.text = setting.alamatMac
        }
        ukuranKertas = setting.ukuranKertas
        switchCetakOtomatis.isChecked = setting.cetakOtomatis
        updateTombolUkuran()
    }

    private fun setupListeners() {
        ivKembali.setOnClickListener { finish() }

        btnScanBluetooth.setOnClickListener {
            checkPermissionThenScan()
        }

        btn58mm.setOnClickListener {
            ukuranKertas = "58mm"
            updateTombolUkuran()
        }

        btn80mm.setOnClickListener {
            ukuranKertas = "80mm"
            updateTombolUkuran()
        }

        btnTesCetak.setOnClickListener {
            val setting = printerManager.getSetting()
            if (setting.alamatMac.isEmpty()) {
                Toast.makeText(this, "Pilih printer dulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            tesCetak(setting.alamatMac)
        }

        btnSimpanSetting.setOnClickListener {
            simpanSetting()
        }
    }

    private fun checkPermissionThenScan() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) scanBluetooth()
        else requestPermissionLauncher.launch(permissions)
    }
    @SuppressLint("MissingPermission")
    private fun scanBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Aktifkan Bluetooth terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "Tidak ada perangkat Bluetooth yang dipasangkan", Toast.LENGTH_SHORT).show()
            return
        }

        deviceAdapter.updateData(pairedDevices.toList())
        rvPerangkat.visibility = View.VISIBLE
        Toast.makeText(this, "${pairedDevices.size} perangkat ditemukan", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun pilihPrinter(device: BluetoothDevice) {
        selectedDevice = device
        tvNamaPrinter.text = device.name ?: "Printer Bluetooth"
        tvMacPrinter.text = device.address
        tvStatusKoneksi.text = "Dipilih"
        tvStatusKoneksi.setTextColor(android.graphics.Color.parseColor("#6B3FA0"))
        rvPerangkat.visibility = View.GONE
        Toast.makeText(this, "${device.name} dipilih", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun updateTombolUkuran() {
        val goldColor = resources.getColor(R.color.accent_gold, theme)
        val goldTint = android.content.res.ColorStateList.valueOf(goldColor)

        // Gunakan warna bg_card bukan transparent agar button tetap terlihat
        val nonAktifBg = android.content.res.ColorStateList.valueOf(
            resources.getColor(R.color.bg_card, theme)
        )
        val goldStroke = android.content.res.ColorStateList.valueOf(goldColor)

        val btn58 = btn58mm as com.google.android.material.button.MaterialButton
        val btn80 = btn80mm as com.google.android.material.button.MaterialButton

        if (ukuranKertas == "58mm") {
            // 58mm AKTIF
            btn58.backgroundTintList = goldTint
            btn58.setTextColor(resources.getColor(R.color.text_primary, theme))
            btn58.strokeColor = goldTint
            btn58.strokeWidth = 0

            // 80mm NON-AKTIF
            btn80.backgroundTintList = nonAktifBg
            btn80.setTextColor(goldColor)
            btn80.strokeColor = goldStroke
            btn80.strokeWidth = 3

        } else {
            // 80mm AKTIF
            btn80.backgroundTintList = goldTint
            btn80.setTextColor(resources.getColor(R.color.text_primary, theme))
            btn80.strokeColor = goldTint
            btn80.strokeWidth = 0

            // 58mm NON-AKTIF
            btn58.backgroundTintList = nonAktifBg
            btn58.setTextColor(goldColor)
            btn58.strokeColor = goldStroke
            btn58.strokeWidth = 3
        }
    }

    @SuppressLint("MissingPermission")
    private fun simpanSetting() {
        val nama = selectedDevice?.name ?: tvNamaPrinter.text.toString()
        val mac = selectedDevice?.address ?: tvMacPrinter.text.toString()

        if (mac.isEmpty() || mac == "Hubungkan printer Bluetooth") {
            Toast.makeText(this, "Pilih printer terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        printerManager.simpanSetting(
            ModelPrinter(
                namaPrinter = nama,
                alamatMac = mac,
                ukuranKertas = ukuranKertas,
                cetakOtomatis = switchCetakOtomatis.isChecked
            )
        )
        Toast.makeText(this, "Pengaturan berhasil disimpan", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun tesCetak(macAddress: String) {
        Thread {
            try {
                val device = bluetoothAdapter.getRemoteDevice(macAddress)
                val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket.connect()
                bluetoothSocket = socket

                val outputStream: OutputStream = socket.outputStream
                val lebarKertas = if (ukuranKertas == "58mm") 32 else 48
                val garis = "-".repeat(lebarKertas)

                val teks = buildString {
                    appendLine("\u001B\u0061\u0001") // center
                    appendLine("POS Laundry")
                    appendLine("Tes Cetak Printer")
                    appendLine("\u001B\u0061\u0000") // left
                    appendLine(garis)
                    appendLine("Printer  : ${device.name}")
                    appendLine("Kertas   : $ukuranKertas")
                    appendLine("Status   : OK")
                    appendLine(garis)
                    appendLine("\u001B\u0061\u0001") // center
                    appendLine("Printer siap digunakan!")
                    appendLine("\n\n\n")
                }

                outputStream.write(teks.toByteArray(Charsets.UTF_8))
                outputStream.flush()
                socket.close()

                runOnUiThread {
                    tvStatusKoneksi.text = "Terhubung"
                    tvStatusKoneksi.setTextColor(
                        android.graphics.Color.parseColor("#4CAF50")
                    )
                    Toast.makeText(this, "Tes cetak berhasil!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    tvStatusKoneksi.text = "Gagal Terhubung"
                    tvStatusKoneksi.setTextColor(
                        android.graphics.Color.parseColor("#E53935")
                    )
                    Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        try { bluetoothSocket?.close() } catch (e: Exception) { }
    }
}