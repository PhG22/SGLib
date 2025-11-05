package br.com.PhG22.sglib.view.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.model.Livro
import br.com.PhG22.sglib.view.BookDetailActivity
import com.bumptech.glide.Glide

class BookAdapter(
    private val context: Context,
    private var bookList: List<Livro>,
    private val onItemClick: (Livro) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.ivBookCover)
        val tvTitle: TextView = view.findViewById(R.id.tvBookTitle)
        val tvAuthor: TextView = view.findViewById(R.id.tvBookAuthor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_book_grid, parent, false)
        return BookViewHolder(view)
    }

    override fun getItemCount(): Int = bookList.size

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]

        holder.tvTitle.text = book.titulo
        holder.tvAuthor.text = book.autor

        Glide.with(context)
            .load(book.imageUrl)
            .placeholder(R.drawable.ic_library_book)
            .into(holder.ivCover)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, BookDetailActivity::class.java)
            intent.putExtra("BOOK_ID", book.id)
            context.startActivity(intent)
        }
    }

    fun updateData(newBookList: List<Livro>) {
        this.bookList = newBookList
        notifyDataSetChanged()
    }
}