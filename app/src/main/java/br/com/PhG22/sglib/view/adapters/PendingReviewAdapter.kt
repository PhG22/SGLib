package br.com.PhG22.sglib.view.adapters
// No pacote: view.adapters
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.controller.BookController
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.model.Resenha

class PendingReviewAdapter(
    private var reviewList: List<Resenha>,
    private val onApproveClick: (Resenha) -> Unit,
    private val onDeleteClick: (Resenha) -> Unit
) : RecyclerView.Adapter<PendingReviewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBookTitle: TextView = view.findViewById(R.id.tvReviewBookTitle)
        val tvUserName: TextView = view.findViewById(R.id.tvReviewUserName)
        val ratingBar: RatingBar = view.findViewById(R.id.reviewRatingBar)
        val tvComment: TextView = view.findViewById(R.id.tvReviewComment)
        val btnApprove: Button = view.findViewById(R.id.btnApproveReview)
        val btnDelete: Button = view.findViewById(R.id.btnDeleteReview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_review, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = reviewList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviewList[position]

        holder.tvUserName.text = "por ${review.userName}"
        holder.ratingBar.rating = review.rating
        holder.tvComment.text = review.comment

        // 1. Define o texto padrão
        holder.tvBookTitle.text = "(Carregando Título...)"

        // 2. Busca o nome do livro de forma assíncrona
        BookController.getBookById(review.bookId,
            onSuccess = { livro ->
                // 3. Verifica se a view ainda está vinculada a este item
                if (holder.adapterPosition != RecyclerView.NO_POSITION && holder.adapterPosition < reviewList.size) {
                    val currentReview = reviewList[holder.adapterPosition]
                    if (currentReview.id == review.id) {
                        holder.tvBookTitle.text = livro.titulo
                    }
                }
            },
            onError = {
                // Em caso de erro (ex: livro foi apagado mas a resenha não)
                if (holder.adapterPosition != RecyclerView.NO_POSITION && holder.adapterPosition < reviewList.size) {
                    val currentReview = reviewList[holder.adapterPosition]
                    if (currentReview.id == review.id) {
                        holder.tvBookTitle.text = "Livro não encontrado"
                    }
                }
            }
        )

        holder.btnApprove.setOnClickListener { onApproveClick(review) }
        holder.btnDelete.setOnClickListener { onDeleteClick(review) }
    }

    fun updateData(newReviewList: List<Resenha>) {
        this.reviewList = newReviewList
        notifyDataSetChanged()
    }
}