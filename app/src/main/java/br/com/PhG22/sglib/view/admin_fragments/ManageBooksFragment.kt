package br.com.PhG22.sglib.view.admin_fragments
// No pacote: view.admin_fragments
import android.content.Intent
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
import br.com.PhG22.sglib.controller.BookController
import br.com.PhG22.sglib.model.Livro
import br.com.PhG22.sglib.view.AddEditBookActivity
import br.com.PhG22.sglib.view.adapters.ManageBookAdapter // Importar o novo adapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ManageBooksFragment : Fragment() {

    private lateinit var rvManageBooks: RecyclerView
    private lateinit var fabAddBook: FloatingActionButton
    private lateinit var adapter: ManageBookAdapter
    private var bookList = mutableListOf<Livro>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manage_books, container, false)

        rvManageBooks = view.findViewById(R.id.rvManageBooks)
        fabAddBook = view.findViewById(R.id.fab_add_book)

        setupRecyclerView() // Configurar o RecyclerView

        fabAddBook.setOnClickListener {
            val intent = Intent(activity, AddEditBookActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Recarrega os livros sempre que o fragmento ficar visível
        loadBooks()
    }

    private fun setupRecyclerView() {
        adapter = ManageBookAdapter(requireContext(), bookList,
            onEditClick = { livro ->
                // --- AQUI ESTÁ A MUDANÇA ---
                // Inicia a AddEditBookActivity e passa o ID do livro
                val intent = Intent(activity, AddEditBookActivity::class.java)
                intent.putExtra("EXTRA_BOOK_ID", livro.id)
                startActivity(intent)
                // --- FIM DA MUDANÇA ---
            },
            onDeleteClick = { livro ->
                showDeleteConfirmation(livro)
            }
        )
        rvManageBooks.layoutManager = LinearLayoutManager(context)
        rvManageBooks.adapter = adapter
    }

    private fun loadBooks() {
        // A função getAllBooks já existe no BookController
        BookController.getAllBooks(
            onSuccess = { livros ->
                adapter.updateData(livros)
            },
            onError = { errorMsg ->
                Toast.makeText(context, "Erro ao carregar livros: $errorMsg", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun showDeleteConfirmation(livro: Livro) {
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir Livro")
            .setMessage("Tem certeza que deseja excluir '${livro.titulo}'? Esta ação não pode ser desfeita.")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Excluir") { dialog, _ ->
                deleteBook(livro)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteBook(livro: Livro) {
        BookController.deleteBook(livro,
            onSuccess = {
                Toast.makeText(context, "Livro excluído com sucesso.", Toast.LENGTH_SHORT).show()
                loadBooks() // Recarrega a lista
            },
            onError = { errorMsg ->
                Toast.makeText(context, "Erro ao excluir: $errorMsg", Toast.LENGTH_LONG).show()
            }
        )
    }
}