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
import br.com.PhG22.sglib.view.adapters.PendingReturnAdapter

class PendingReturnsFragment : Fragment() {

    private lateinit var rvPendingReturns: RecyclerView
    private lateinit var adapter: PendingReturnAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pending_returns, container, false)
        rvPendingReturns = view.findViewById(R.id.rvPendingReturns)
        setupRecyclerView()
        return view
    }

    override fun onResume() {
        super.onResume()
        loadPendingReturns()
    }

    private fun setupRecyclerView() {
        adapter = PendingReturnAdapter(requireContext(), emptyList()) { loan ->
            // Ação de clique no botão "Confirmar"
            AdminController.confirmReturn(loan,
                onSuccess = { isLate ->
                    var message = "Devolução confirmada!"
                    if (isLate) {
                        message += " (Multa por atraso gerada.)"
                    }
                    showToast(message)
                    loadPendingReturns()
                },
                onError = { showToast("Erro: $it") }
            )
        }
        rvPendingReturns.layoutManager = LinearLayoutManager(context)
        rvPendingReturns.adapter = adapter
    }

    private fun loadPendingReturns() {
        AdminController.getPendingReturns(
            onSuccess = { loans ->
                adapter.updateData(loans)
            },
            onError = { showToast("Erro: $it") }
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}