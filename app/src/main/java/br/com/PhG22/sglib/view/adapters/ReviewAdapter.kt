package br.com.PhG22.sglib.view.adapters
// No pacote: view.adapters
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.model.Resenha


class ReviewAdapter(
    private var reviewList: List<Resenha>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvReviewUserName)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBarReview)
        val tvComment: TextView = view.findViewById(R.id.tvReviewComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun getItemCount(): Int = reviewList.size

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.tvUserName.text = review.userName
        holder.ratingBar.rating = review.rating
        holder.tvComment.text = review.comment
    }

    fun updateData(newReviewList: List<Resenha>) {
        this.reviewList = newReviewList
        notifyDataSetChanged()
    }
}