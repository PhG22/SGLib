package br.com.PhG22.sglib.view.admin_fragments
// No pacote: view.admin_fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.controller.AdminController
import br.com.PhG22.sglib.model.Emprestimo
import br.com.PhG22.sglib.view.adapters.PendingLoanAdapter

class PendingLoansFragment : Fragment() {

    private lateinit var rvPendingLoans: RecyclerView
    private lateinit var adapter: PendingLoanAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pending_loans, container, false)
        rvPendingLoans = view.findViewById(R.id.rvPendingLoans)
        setupRecyclerView()
        return view
    }

    override fun onResume() {
        super.onResume()
        loadPendingLoans()
    }

    private fun setupRecyclerView() {
        adapter = PendingLoanAdapter(requireContext(), emptyList(),
            onApproveClick = { loan ->
                AdminController.approveLoan(loan,
                    onSuccess = {
                        showToast("Empréstimo aprovado!")
                        loadPendingLoans()
                    },
                    onError = { showToast("Erro: $it") }
                )
            },
            onDenyClick = { loan ->
                AdminController.denyLoan(loan.id,
                    onSuccess = {
                        showToast("Empréstimo negado.")
                        loadPendingLoans()
                    },
                    onError = { showToast("Erro: $it") }
                )
            }
        )
        rvPendingLoans.layoutManager = LinearLayoutManager(context)
        rvPendingLoans.adapter = adapter
    }

    private fun loadPendingLoans() {
        AdminController.getPendingLoans(
            onSuccess = { loans ->
                adapter.updateData(loans)
            },
            onError = { showToast("Erro: $it") }
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}