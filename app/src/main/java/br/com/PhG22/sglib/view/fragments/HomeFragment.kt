package br.com.PhG22.sglib.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.controller.BookController
import br.com.PhG22.sglib.model.Livro
import br.com.PhG22.sglib.view.adapters.BookAdapter

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private var bookList = mutableListOf<Livro>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewBooks)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        bookAdapter = BookAdapter(requireContext(), bookList) { livroClicado ->
            Toast.makeText(requireContext(), "Clicou em: ${livroClicado.titulo}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = bookAdapter

        loadBooks()
    }

    private fun loadBooks() {
        BookController.getAllBooks(
            onSuccess = { livrosDoFirestore ->
                bookAdapter.updateData(livrosDoFirestore)
            },
            onError = { errorMessage ->
                Toast.makeText(requireContext(), "Erro: $errorMessage", Toast.LENGTH_LONG).show()
            }
        )
    }
}