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
import br.com.PhG22.sglib.model.Fine
import br.com.PhG22.sglib.view.adapters.ManageFinesAdapter

class ManageFinesFragment : Fragment() {

    private lateinit var rvManageFines: RecyclerView
    private lateinit var adapter: ManageFinesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manage_fines, container, false)
        rvManageFines = view.findViewById(R.id.rvManageFines)
        setupRecyclerView()
        return view
    }

    override fun onResume() {
        super.onResume()
        loadUnpaidFines()
    }

    private fun setupRecyclerView() {
        adapter = ManageFinesAdapter(emptyList()) { fine ->
            // Ação de clique no botão "Pago"
            AdminController.markFineAsPaid(fine.id,
                onSuccess = {
                    showToast("Multa de ${fine.reason} marcada como paga.")
                    loadUnpaidFines() // Atualiza a lista
                },
                onError = { showToast("Erro: $it") }
            )
        }
        rvManageFines.layoutManager = LinearLayoutManager(context)
        rvManageFines.adapter = adapter
    }

    private fun loadUnpaidFines() {
        AdminController.getAllUnpaidFines(
            onSuccess = { fines ->
                adapter.updateData(fines)
            },
            onError = { showToast("Erro: $it") }
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
