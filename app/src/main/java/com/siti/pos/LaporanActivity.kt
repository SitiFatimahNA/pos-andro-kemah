package com.siti.pos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import Helper.DatabaseHelper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import model.ModelTransaksi
import com.siti.pos.DetailTransaksiBottomSheet

class LaporanActivity : AppCompatActivity() {

    private lateinit var ivKembali: ImageView
    private lateinit var ivExport: ImageView
    private lateinit var btnHarian: Button
    private lateinit var btnBulanan: Button
    private lateinit var ivPrev: ImageView
    private lateinit var ivNext: ImageView
    private lateinit var tvPeriode: TextView
    private lateinit var tvTotalPendapatan: TextView
    private lateinit var tvJumlahTransaksi: TextView
    private lateinit var tvRataRata: TextView
    private lateinit var llGrafik: LinearLayout
    private lateinit var llProdukTerlaris: LinearLayout
    private lateinit var rvTransaksi: RecyclerView
    private lateinit var tvKosong: TextView
    private lateinit var dbHelper: DatabaseHelper

    private val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private var isHarian = true
    private var currentDate = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan)

        dbHelper = DatabaseHelper(this)
        initViews()
        updateTabStyle()
        setupListeners()
        loadLaporan()
    }

    private fun initViews() {
        ivKembali = findViewById(R.id.ivKembali)
        ivExport = findViewById(R.id.ivExport)
        btnHarian = findViewById(R.id.btnHarian)
        btnBulanan = findViewById(R.id.btnBulanan)
        ivPrev = findViewById(R.id.ivPrev)
        ivNext = findViewById(R.id.ivNext)
        tvPeriode = findViewById(R.id.tvPeriode)
        tvTotalPendapatan = findViewById(R.id.tvTotalPendapatan)
        tvJumlahTransaksi = findViewById(R.id.tvJumlahTransaksi)
        tvRataRata = findViewById(R.id.tvRataRata)
        llGrafik = findViewById(R.id.llGrafik)
        llProdukTerlaris = findViewById(R.id.llProdukTerlaris)
        rvTransaksi = findViewById(R.id.rvTransaksi)
        tvKosong = findViewById(R.id.tvKosong)
    }

    private fun setupListeners() {
        ivKembali.setOnClickListener { finish() }

        btnHarian.setOnClickListener {
            isHarian = true
            updateTabStyle()
            loadLaporan()
        }

        btnBulanan.setOnClickListener {
            isHarian = false
            updateTabStyle()
            loadLaporan()
        }

        ivPrev.setOnClickListener {
            if (isHarian) currentDate.add(Calendar.DAY_OF_MONTH, -1)
            else currentDate.add(Calendar.MONTH, -1)
            loadLaporan()
        }

        ivNext.setOnClickListener {
            if (isHarian) currentDate.add(Calendar.DAY_OF_MONTH, 1)
            else currentDate.add(Calendar.MONTH, 1)
            loadLaporan()
        }

        ivExport.setOnClickListener {
            exportLaporan()
        }
    }

    private fun updateTabStyle() {
        val aktifBg = android.content.res.ColorStateList.valueOf(
            resources.getColor(R.color.accent_gold, theme))
        val nonAktifBg = android.content.res.ColorStateList.valueOf(
            resources.getColor(R.color.accent_gold, theme)
        )
        if (isHarian) {
            btnHarian.backgroundTintList = aktifBg
            btnHarian.setTextColor(Color.WHITE)
            btnBulanan.backgroundTintList = nonAktifBg
            btnBulanan.setTextColor(resources.getColor(R.color.text_hint, theme))
        } else {
            btnBulanan.backgroundTintList = aktifBg
            btnBulanan.setTextColor(Color.WHITE)
            btnHarian.backgroundTintList = nonAktifBg
            btnHarian.setTextColor(resources.getColor(R.color.text_hint, theme))
        }
    }

    private fun loadLaporan() {
        if (isHarian) loadHarian() else loadBulanan()
        loadProdukTerlaris()
    }

    private fun loadHarian() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val tanggal = sdf.format(currentDate.time)

        val sdfDisplay = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        tvPeriode.text = sdfDisplay.format(currentDate.time)

        val total = dbHelper.getTotalPendapatanHarian(tanggal)
        val transaksiList = dbHelper.getTransaksiHarian(tanggal)

        updateSummary(total, transaksiList.size)
        loadGrafikHarian(tanggal)
        loadDaftarTransaksi(transaksiList)
    }

    private fun loadBulanan() {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val bulan = sdf.format(currentDate.time)

        val sdfDisplay = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        tvPeriode.text = sdfDisplay.format(currentDate.time)

        val total = dbHelper.getTotalPendapatanBulanan(bulan)
        val transaksiList = dbHelper.getTransaksiBulanan(bulan)

        updateSummary(total, transaksiList.size)
        loadGrafikBulanan(bulan)
        loadDaftarTransaksi(transaksiList)
    }

    private fun updateSummary(total: Long, jumlah: Int) {
        tvTotalPendapatan.text = formatter.format(total)
            .replace("Rp", "Rp ").replace(",00", "")
        tvJumlahTransaksi.text = jumlah.toString()
        val rata = if (jumlah > 0) total / jumlah else 0
        tvRataRata.text = formatter.format(rata)
            .replace("Rp", "Rp ").replace(",00", "")
    }

    private fun loadGrafikHarian(tanggal: String) {
        // Grafik per jam untuk harian — tampilkan bar sederhana
        llGrafik.removeAllViews()
        val bulan = tanggal.substring(0, 7)
        val dataGrafik = dbHelper.getPendapatanPerHariDalamBulan(bulan)
        renderGrafik(dataGrafik)
    }

    private fun loadGrafikBulanan(bulan: String) {
        llGrafik.removeAllViews()
        val dataGrafik = dbHelper.getPendapatanPerHariDalamBulan(bulan)
        renderGrafik(dataGrafik)
    }

    private fun renderGrafik(data: List<Pair<String, Long>>) {
        llGrafik.removeAllViews()
        if (data.isEmpty()) return

        val maxVal = data.maxOf { it.second }.takeIf { it > 0 } ?: 1L
        val maxHeight = 120 // dp

        data.forEach { (tgl, nilai) ->
            val col = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(28), LinearLayout.LayoutParams.MATCH_PARENT
                ).apply { marginEnd = dpToPx(4) }
            }

            val barHeight = ((nilai.toFloat() / maxVal) * maxHeight).toInt().coerceAtLeast(4)
            val bar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(16), dpToPx(barHeight))
                setBackgroundColor(resources.getColor(R.color.accent_gold, theme))
                alpha = 0.8f
            }

            val label = TextView(this).apply {
                text = tgl.substring(8) // ambil tanggal saja
                textSize = 8f
                setTextColor(resources.getColor(R.color.text_hint, theme))
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dpToPx(4) }
            }

            col.addView(bar)
            col.addView(label)
            llGrafik.addView(col)
        }
    }

    private fun loadProdukTerlaris() {
        llProdukTerlaris.removeAllViews()
        val produkList = dbHelper.getProdukTerlaris(5)

        if (produkList.isEmpty()) {
            val tv = TextView(this).apply {
                text = "Belum ada data produk"
                textSize = 13f
                setTextColor(resources.getColor(R.color.text_hint, theme))
            }
            llProdukTerlaris.addView(tv)
            return
        }

        val maxTerjual = produkList.maxOf { it.second }.takeIf { it > 0 } ?: 1

        produkList.forEachIndexed { index, (nama, terjual) ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dpToPx(10) }
            }

            val infoRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dpToPx(4) }
            }

            val rank = TextView(this).apply {
                text = "${index + 1}"
                textSize = 12f
                setTextColor(resources.getColor(R.color.accent_gold, theme))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(dpToPx(20), LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            val namaTv = TextView(this).apply {
                text = nama
                textSize = 13f
                setTextColor(resources.getColor(R.color.text_primary, theme))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val jumlahTv = TextView(this).apply {
                text = "$terjual terjual"
                textSize = 12f
                setTextColor(resources.getColor(R.color.text_hint, theme))
            }

            infoRow.addView(rank)
            infoRow.addView(namaTv)
            infoRow.addView(jumlahTv)

            val colorGold = resources.getColor(R.color.accent_gold, theme)

            // Progress bar
            val progressBg = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(6)
                )
                val bgColor = android.graphics.Color.argb(40,   // alpha ~15%
                    android.graphics.Color.red(colorGold),
                    android.graphics.Color.green(colorGold),
                    android.graphics.Color.blue(colorGold)
                )
                setBackgroundColor(bgColor)
            }

            val progressFill = View(this).apply {
                val ratio = terjual.toFloat() / maxTerjual
                layoutParams = FrameLayout.LayoutParams(
                    (resources.displayMetrics.widthPixels * ratio * 0.7f).toInt(), dpToPx(6)
                )
                setBackgroundColor(colorGold)
            }

            progressBg.addView(progressFill)
            row.addView(infoRow)
            row.addView(progressBg)
            llProdukTerlaris.addView(row)
        }
    }

    private fun loadDaftarTransaksi(list: List<ModelTransaksi>) {
        if (list.isEmpty()) {
            tvKosong.visibility = View.VISIBLE
            rvTransaksi.visibility = View.GONE
            return
        }

        tvKosong.visibility = View.GONE
        rvTransaksi.visibility = View.VISIBLE

        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int) =
                object : RecyclerView.ViewHolder(
                    LayoutInflater.from(parent.context)
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

        rvTransaksi.layoutManager = LinearLayoutManager(this)
        rvTransaksi.adapter = adapter

        val divider = androidx.recyclerview.widget.DividerItemDecoration(
            this, LinearLayoutManager.VERTICAL
        )
        androidx.core.content.ContextCompat.getDrawable(
            this, R.drawable.divider_item
        )?.let { divider.setDrawable(it) }
        rvTransaksi.addItemDecoration(divider)
    }

    private fun exportLaporan() {
        val periode = tvPeriode.text.toString()
        val total = tvTotalPendapatan.text.toString()
        val jumlah = tvJumlahTransaksi.text.toString()

        val sb = StringBuilder()
        sb.appendLine("LAPORAN POS LAUNDRY")
        sb.appendLine("Periode : $periode")
        sb.appendLine("=".repeat(40))
        sb.appendLine("Total Pendapatan : $total")
        sb.appendLine("Jumlah Transaksi : $jumlah")
        sb.appendLine("=".repeat(40))
        sb.appendLine()
        sb.appendLine("PRODUK TERLARIS")
        dbHelper.getProdukTerlaris(5).forEachIndexed { i, (nama, terjual) ->
            sb.appendLine("${i + 1}. $nama - $terjual terjual")
        }

        // Simpan ke file
        try {
            val fileName = "Laporan_${periode.replace(" ", "_")}.txt"
            val file = java.io.File(getExternalFilesDir(null), fileName)
            file.writeText(sb.toString())

            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Laporan $periode")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Export Laporan"))
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal export: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}