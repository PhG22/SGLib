package br.com.PhG22.sglib.view.admin_fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.view.AddEditBookActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ManageBooksFragment : Fragment() {

    private lateinit var rvManageBooks: RecyclerView
    private lateinit var fabAddBook: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manage_books, container, false)

        rvManageBooks = view.findViewById(R.id.rvManageBooks)
        fabAddBook = view.findViewById(R.id.fab_add_book)

        fabAddBook.setOnClickListener {
            val intent = Intent(activity, AddEditBookActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}