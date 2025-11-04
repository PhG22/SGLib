package br.com.PhG22.sglib.view.adapters

// No pacote: view.adapters
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.model.Livro
import com.bumptech.glide.Glide
import android.content.Intent
import br.com.PhG22.sglib.view.BookDetailActivity

class BookAdapter(
    private val context: Context,
    private var bookList: List<Livro>,
    private val onItemClick: (Livro) -> Unit // Callback de clique
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    // 1. ViewHolder: Mapeia as Views do item_book_grid.xml
    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.ivBookCover)
        val tvTitle: TextView = view.findViewById(R.id.tvBookTitle)
        val tvAuthor: TextView = view.findViewById(R.id.tvBookAuthor)
    }

    // 2. Infla (cria) o layout do item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_book_grid, parent, false)
        return BookViewHolder(view)
    }

    // 3. Retorna a contagem de itens
    override fun getItemCount(): Int = bookList.size

    // 4. Conecta os dados (Livro) à View (ViewHolder)
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]

        holder.tvTitle.text = book.titulo
        holder.tvAuthor.text = book.autor

        // Usa o Glide para carregar a imagem da URL
        Glide.with(context)
            .load(book.imageUrl)
            .placeholder(R.drawable.ic_library_book) // Imagem enquanto carrega
            .into(holder.ivCover)

        // 5. Configura o clique no item
        holder.itemView.setOnClickListener {
            // Ação de clique: Ir para a tela de detalhes
            val intent = Intent(context, BookDetailActivity::class.java)
            // Passa o ID do livro para a próxima tela
            intent.putExtra("BOOK_ID", book.id)
            context.startActivity(intent)

            // Remove o callback antigo
            // onItemClick(book)
        }
    }

    // Função para atualizar a lista do adapter quando o Firestore responder
    fun updateData(newBookList: List<Livro>) {
        this.bookList = newBookList
        notifyDataSetChanged()
    }
}