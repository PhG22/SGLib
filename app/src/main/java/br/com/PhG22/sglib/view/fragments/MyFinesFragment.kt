package br.com.PhG22.sglib.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.controller.FinesController
import br.com.PhG22.sglib.model.Fine

class MyFinesFragment : Fragment() {

    private lateinit var tvTotalAmount: TextView
    private lateinit var rvFines: RecyclerView
    private lateinit var noFinesLayout: LinearLayout
    private lateinit var finesLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_fines, container, false)

        tvTotalAmount = view.findViewById(R.id.tv_total_amount_due)
        rvFines = view.findViewById(R.id.rv_fines_list)
        noFinesLayout = view.findViewById(R.id.no_fines_layout)
        finesLayout = view.findViewById(R.id.fines_layout)

        loadFines()

        return view
    }

    private fun loadFines() {
        FinesController.getMyUnpaidFines(
            onSuccess = { finesList ->
                if (finesList.isEmpty()) {
                    noFinesLayout.visibility = View.VISIBLE
                    finesLayout.visibility = View.GONE
                } else {
                    noFinesLayout.visibility = View.GONE
                    finesLayout.visibility = View.VISIBLE

                    val total = finesList.sumOf { it.amount }
                    tvTotalAmount.text = "$${String.format("%.2f", total)}"

                    // TODO: Configurar o RecyclerView com um FinesAdapter
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