package com.siti.pos

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import Helper.DatabaseHelper
import java.text.NumberFormat
import java.util.Locale

class RiwayatTransaksiActivity : AppCompatActivity() {

    private lateinit var ivKembali: ImageView
    private lateinit var rvRiwayat: RecyclerView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat_transaksi)

        dbHelper = DatabaseHelper(this)
        ivKembali = findViewById(R.id.ivKembali)
        rvRiwayat = findViewById(R.id.rvRiwayat)

        ivKembali.setOnClickListener { finish() }
        loadRiwayat()
    }

    private fun loadRiwayat() {
        val list = dbHelper.getAllTransaksi()
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int) =
                object : RecyclerView.ViewHolder(
                    android.view.LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_riwayat_transaksi, parent, false)
                ) {}

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val t = list[position]
                holder.itemView.findViewById<TextView>(R.id.tvNomorRiwayat).text = t.nomorTransaksi
                holder.itemView.findViewById<TextView>(R.id.tvTanggalRiwayat).text = t.tanggal
                holder.itemView.findViewById<TextView>(R.id.tvPelangganRiwayat).text =
                    t.pelangganNama.ifEmpty { "Umum" }
                holder.itemView.findViewById<TextView>(R.id.tvTotalRiwayat).text =
                    formatter.format(t.totalHarga).replace("Rp", "Rp ").replace(",00", "")
                holder.itemView.findViewById<TextView>(R.id.tvMetodeRiwayat).text = t.metodePembayaran
                holder.itemView.setOnClickListener {
                    DetailTransaksiBottomSheet(t).show(supportFragmentManager, "detail")
                }
            }

            override fun getItemCount() = list.size
        }

        rvRiwayat.layoutManager = LinearLayoutManager(this)
        rvRiwayat.adapter = adapter
    }
}