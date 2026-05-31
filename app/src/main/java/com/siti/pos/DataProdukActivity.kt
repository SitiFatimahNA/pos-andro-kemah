package com.siti.pos

import Helper.DatabaseHelper
import adapter.ProdukAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.siti.pos.databinding.ActivityDataProdukBinding
import com.siti.pos.ModelProduk
import produk.Produk
import kotlin.jvm.java

class DataProdukActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataProdukBinding
    private lateinit var produkAdapter: ProdukAdapter
    private lateinit var dbHelper: DatabaseHelper

    private var listSemua: MutableList<Produk> = mutableListOf()

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        loadProduk()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDataProdukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        setupRecyclerView()
        setupActionButtons()
        setupChipFilter()
        setupSearch()

        loadProduk()
    }

    override fun onResume() {
        super.onResume()
        loadProduk()
    }

    private fun setupRecyclerView() {

        produkAdapter = ProdukAdapter(
            mutableListOf(),
            onItemClick = { produk ->
                showOptionsDialog(produk)
            }
        )

        binding.rvKategori.apply {
            layoutManager = LinearLayoutManager(this@DataProdukActivity)
            adapter = produkAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupActionButtons() {

        binding.ivkembali.setOnClickListener {
            finish()
        }

        binding.ivReload.setOnClickListener {

            binding.etSearch.setText("")
            binding.cpSemua.isChecked = true

            loadProduk()

            Toast.makeText(
                this,
                "Data diperbarui",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupSearch() {

        binding.etSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                filterData(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupChipFilter() {

        binding.cpSemua.setOnClickListener {
            filterByCategory("Semua")
        }

        binding.cpNyewa.setOnClickListener {
            filterByCategory("Nyewa")
        }

        binding.cpBeli.setOnClickListener {
            filterByCategory("Beli")
        }

        binding.fabTambah.setOnClickListener {
            val intent = Intent(this, model_produk::class.java)
            intent.putExtra("mode", "tambah")
            launcher.launch(intent)
        }
    }

    private fun filterData(query: String) {

        val filtered =
            if (query.isEmpty()) {

                listSemua

            } else {

                listSemua.filter {

                    it.nama.contains(
                        query,
                        ignoreCase = true
                    )

                }.toMutableList()
            }

        produkAdapter.updateData(filtered)
    }

    private fun filterByCategory(category: String) {

        val filtered =
            if (category == "Semua") {

                listSemua

            } else {

                listSemua.filter {

                    it.kategori == category

                }.toMutableList()
            }

        produkAdapter.updateData(filtered)
    }

    private fun showOptionsDialog(produk: Produk) {

        android.app.AlertDialog.Builder(this)
            .setTitle(produk.nama)
            .setItems(arrayOf("Edit", "Hapus")) { _, which ->

                when (which) {

                    0 -> bukaEdit(produk)

                    1 -> hapusProduk(produk)
                }
            }
            .show()
    }

    private fun bukaEdit(produk: Produk) {

        val intent =
            Intent(this, model_produk::class.java).apply {

                putExtra("mode", "edit")
                putExtra("produk_id", produk.id)
                putExtra("nama", produk.nama)
                putExtra("harga", produk.harga)
                putExtra("kategoriId", produk.kategoriId)
                putExtra("kategoriNama", produk.kategori)
                putExtra("stok", produk.stok)
                putExtra("cabang", produk.cabang)
                putExtra("foto", produk.foto)
            }

        launcher.launch(intent)
    }

    private fun hapusProduk(produk: Produk) {

        produk.id?.let { id ->

            dbHelper.deleteProduk(id)

            Toast.makeText(
                this,
                "Produk berhasil dihapus",
                Toast.LENGTH_SHORT
            ).show()

            loadProduk()
        }
    }

    private fun loadProduk() {

        listSemua.clear()

        listSemua.addAll(
            dbHelper.getAllProduk()
        )

        android.util.Log.d(
            "PRODUK_TEST",
            "Jumlah produk = ${listSemua.size}"
        )

        produkAdapter.updateData(listSemua)
    }
}