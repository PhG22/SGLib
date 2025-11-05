package br.com.PhG22.sglib.view.admin_fragments
// No pacote: view.admin_fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.controller.AdminController
import br.com.PhG22.sglib.model.Usuario
import br.com.PhG22.sglib.view.adapters.PendingUserAdapter

class PendingUsersFragment : Fragment() {

    private lateinit var rvPendingUsers: RecyclerView
    private lateinit var adapter: PendingUserAdapter
    private var userList = mutableListOf<Usuario>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Usa o novo layout "fragment_pending_users"
        val view = inflater.inflate(R.layout.fragment_pending_users, container, false)

        rvPendingUsers = view.findViewById(R.id.rvPendingUsers)
        setupRecyclerView()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadPendingUsers() // Recarrega a lista sempre que a aba fica visível
    }

    private fun setupRecyclerView() {
        adapter = PendingUserAdapter(userList,
            onApproveClick = { user ->
                // Ação de Aprovar
                AdminController.approveUser(user.uid,
                    onSuccess = {
                        showToast("${user.nome} aprovado.")
                        loadPendingUsers() // Atualiza a lista
                    },
                    onError = { showToast("Erro: $it") }
                )
            },
            onDenyClick = { user ->
                // Ação de Negar
                showDenyConfirmation(user)
            }
        )
        rvPendingUsers.layoutManager = LinearLayoutManager(context)
        rvPendingUsers.adapter = adapter
    }

    private fun loadPendingUsers() {
        AdminController.getPendingUsers(
            onSuccess = { users ->
                adapter.updateData(users)
            },
            onError = { showToast("Erro ao carregar lista: $it") }
        )
    }

    private fun showDenyConfirmation(user: Usuario) {
        AlertDialog.Builder(requireContext())
            .setTitle("Negar Cadastro")
            .setMessage("Tem certeza que deseja negar e excluir o cadastro de '${user.nome}'?")
            .setPositiveButton("Sim, Negar") { dialog, _ ->
                AdminController.denyUser(user.uid,
                    onSuccess = {
                        showToast("${user.nome} negado e excluído.")
                        loadPendingUsers()
                    },
                    onError = { showToast("Erro: $it") }
                )
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}