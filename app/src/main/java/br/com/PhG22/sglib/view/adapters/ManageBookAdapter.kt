package br.com.PhG22.sglib.view.adapters
// No pacote: view.adapters
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.model.Livro
import com.bumptech.glide.Glide

class ManageBookAdapter(
    private val context: Context,
    private var bookList: List<Livro>,
    private val onEditClick: (Livro) -> Unit,
    private val onDeleteClick: (Livro) -> Unit
) : RecyclerView.Adapter<ManageBookAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.ivBookCoverAdmin)
        val tvTitle: TextView = view.findViewById(R.id.tvBookTitleAdmin)
        val tvAuthor: TextView = view.findViewById(R.id.tvBookAuthorAdmin)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditBook)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteBook)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_manage_book, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = bookList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val livro = bookList[position]

        holder.tvTitle.text = livro.titulo
        holder.tvAuthor.text = livro.autor
        Glide.with(context).load(livro.imageUrl).into(holder.ivCover)

        holder.btnEdit.setOnClickListener {
            onEditClick(livro)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(livro)
        }
    }

    fun updateData(newBookList: List<Livro>) {
        this.bookList = newBookList
        notifyDataSetChanged()
    }
}