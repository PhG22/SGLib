package br.com.PhG22.sglib.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.controller.UserFinesController // <-- MUDADO para FinesController
import br.com.PhG22.sglib.model.Fine
import br.com.PhG22.sglib.view.adapters.UserFinesAdapter // <-- MUDADO para MyFinesAdapter
import java.text.NumberFormat
import java.util.Locale

class UserFinesFragment : Fragment() {

    private lateinit var tvTotalAmount: TextView
    private lateinit var rvFines: RecyclerView
    private lateinit var noFinesLayout: LinearLayout // Layout para "You have no outstanding fines"
    private lateinit var finesLayout: LinearLayout // Layout que contém o total e a lista
    private lateinit var adapter: UserFinesAdapter // <-- ADICIONADO

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_fines, container, false)

        tvTotalAmount = view.findViewById(R.id.tv_total_amount_due)
        rvFines = view.findViewById(R.id.rv_fines_list)
        noFinesLayout = view.findViewById(R.id.no_fines_layout)
        finesLayout = view.findViewById(R.id.fines_layout)

        setupRecyclerView() // <-- ADICIONADO

        return view
    }

    override fun onResume() {
        super.onResume()
        loadFines() // Carrega as multas quando a aba fica visível
    }

    private fun setupRecyclerView() {
        adapter = UserFinesAdapter(emptyList())
        rvFines.layoutManager = LinearLayoutManager(context)
        rvFines.adapter = adapter
    }

    private fun loadFines() {
        UserFinesController.getMyUnpaidFines( // <-- MUDADO para FinesController
            onSuccess = { finesList ->
                if (finesList.isEmpty()) {
                    // Mostra "You have no outstanding fines"
                    noFinesLayout.visibility = View.VISIBLE
                    finesLayout.visibility = View.GONE
                } else {
                    // Mostra a lista de multas
                    noFinesLayout.visibility = View.GONE
                    finesLayout.visibility = View.VISIBLE

                    // Calcula o total
                    val total = finesList.sumOf { it.amount }
                    val format = NumberFormat.getCurrencyInstance(Locale.US) // Formata como moeda
                    tvTotalAmount.text = format.format(total)

                    // Atualiza o adapter com a lista
                    adapter.updateData(finesList)
                }
            },
            onError = {
                Toast.makeText(context, "Erro: $it", Toast.LENGTH_LONG).show()
                noFinesLayout.visibility = View.VISIBLE
                finesLayout.visibility = View.GONE
            }
        )
    }
}