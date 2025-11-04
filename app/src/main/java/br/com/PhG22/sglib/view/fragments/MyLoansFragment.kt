package br.com.PhG22.sglib.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.controller.LoanController
import br.com.PhG22.sglib.model.Emprestimo
import br.com.PhG22.sglib.view.adapters.LoanAdapter

class MyLoansFragment : Fragment() {

    private lateinit var rvLoans: RecyclerView
    private lateinit var adapter: LoanAdapter
    private var loanList = mutableListOf<Emprestimo>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_loans, container, false)

        rvLoans = view.findViewById(R.id.rv_my_loans)
        setupRecyclerView()
        loadLoans()

        return view
    }

    private fun setupRecyclerView() {
        adapter = LoanAdapter(requireContext(), loanList) { selectedLoan ->
            LoanController.requestReturn(selectedLoan.id,
                onSuccess = {
                    Toast.makeText(context, "Solicitação de devolução enviada.", Toast.LENGTH_SHORT).show()
                    loadLoans()
                },
                onError = {
                    Toast.makeText(context, "Erro: $it", Toast.LENGTH_SHORT).show()
                }
            )
        }
        rvLoans.layoutManager = LinearLayoutManager(context)
        rvLoans.adapter = adapter
    }

    private fun loadLoans() {
        LoanController.getMyLoans(
            onSuccess = {
                adapter.updateData(it)
            },
            onError = {
                Toast.makeText(context, "Erro: $it", Toast.LENGTH_LONG).show()
            }
        )
    }
}