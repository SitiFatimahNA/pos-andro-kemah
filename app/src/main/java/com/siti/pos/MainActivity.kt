package com.siti.pos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvGreetingSub: TextView
    private lateinit var tvGreetingName: TextView
    private lateinit var tvDate: TextView

    private lateinit var menuTransaksi: View
    private lateinit var menuPelanggan: View
    private lateinit var menuLaporan: View

    private lateinit var cardAkun: CardView
    private lateinit var cardLayanan: CardView
    private lateinit var cardTambahan: CardView
    private lateinit var cardPegawai: CardView
    private lateinit var cardCabang: CardView
    private lateinit var cardPrinter: CardView
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setGreetingAndDate()
        setupListeners()
    }
    override fun onResume() {
        super.onResume()
        setGreetingAndDate() // refresh nama setiap kembali ke MainActivity
    }

    private fun initViews() {
        session = SessionManager(this)

        tvGreetingSub = findViewById(R.id.tvGreetingSub)
        tvGreetingName = findViewById(R.id.tvGreetingName)
        tvDate = findViewById(R.id.tvDate)

        menuTransaksi = findViewById(R.id.menuTransaksi)
        menuLaporan = findViewById(R.id.menuLaporan)
        menuPelanggan = findViewById(R.id.menuPelanggan)

        cardAkun = findViewById(R.id.cardAkun)
        cardLayanan = findViewById(R.id.cardLayanan)
        cardTambahan = findViewById(R.id.cardTambahan)
        cardPegawai = findViewById(R.id.cardPegawai)
        cardCabang = findViewById(R.id.cardCabang)
        cardPrinter = findViewById(R.id.cardPrinter)
    }

    private fun setGreetingAndDate() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when {
            hour < 11 -> "SELAMAT PAGI"
            hour < 15 -> "SELAMAT SIANG"
            hour < 18 -> "SELAMAT SORE"
            else -> "SELAMAT MALAM"
        }

        tvGreetingSub.text = greeting

        // ← ambil nama dari session, bukan hardcode
        val akunLogin = session.getAkunLogin()
        tvGreetingName.text = akunLogin.nama.ifEmpty { "Pengguna" }

        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.Builder().setLanguage("id").setRegion("ID").build())
        tvDate.text = dateFormat.format(calendar.time)
    }
    private fun setupListeners() {

        menuTransaksi.setOnClickListener {
            startActivity(Intent(this, TransaksiActivity::class.java))
        }

        menuPelanggan.setOnClickListener {
            startActivity(Intent(this, DataPelangganActivity::class.java))
        }

        menuLaporan.setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }

        cardAkun.setOnClickListener {
            startActivity(Intent(this, AkunActivity::class.java))
        }

        cardLayanan.setOnClickListener {
            val intent = Intent(this, data_kategori::class.java)
            startActivity(intent)
        }

        cardTambahan.setOnClickListener {
            val intent = Intent(this, DataProdukActivity::class.java)
            startActivity(intent)
        }

        cardPegawai.setOnClickListener {
            startActivity(Intent(this, DataPegawaiActivity::class.java))
        }

        cardCabang.setOnClickListener {
            startActivity(Intent(this, DataCabangActivity::class.java))
        }

        cardPrinter.setOnClickListener {
            startActivity(Intent(this, PrinterActivity::class.java))
        }
    }
}