package com.siti.pos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import model.ModelTransaksi
import java.text.NumberFormat
import java.util.Locale

class DetailTransaksiBottomSheet(
    private val transaksi: ModelTransaksi
) : BottomSheetDialogFragment() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bs_detail_transaksi, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvBsNomor).text    = transaksi.nomorTransaksi
        view.findViewById<TextView>(R.id.tvBsTanggal).text  = transaksi.tanggal
        view.findViewById<TextView>(R.id.tvBsPelanggan).text = transaksi.pelangganNama.ifEmpty { "Umum" }
        view.findViewById<TextView>(R.id.tvBsMetode).text   = transaksi.metodePembayaran
        view.findViewById<TextView>(R.id.tvBsCatatan).text  = transaksi.catatan.ifEmpty { "-" }
        view.findViewById<TextView>(R.id.tvBsTotal).text    = formatRp(transaksi.totalHarga)

        val llItems = view.findViewById<LinearLayout>(R.id.llItemBelanja)

        if (transaksi.items.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "Tidak ada detail item"
                textSize = 13f
                setTextColor(resources.getColor(R.color.text_hint, null))
            }
            llItems.addView(tv)
            return
        }

        transaksi.items.forEach { item ->
            val row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_belanja_detail, llItems, false)
            row.findViewById<TextView>(R.id.tvNamaItem).text  = item.namaProduk
            row.findViewById<TextView>(R.id.tvQty).text       = "x${item.jumlah}"
            row.findViewById<TextView>(R.id.tvHargaItem).text = formatRp(item.subtotal)
            llItems.addView(row)
        }
    }

    private fun formatRp(value: Long) =
        formatter.format(value).replace("Rp", "Rp ").replace(",00", "")
}