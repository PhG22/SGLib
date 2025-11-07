package br.com.PhG22.sglib.view.adapters
// No pacote: view.adapters
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.model.Emprestimo
import com.bumptech.glide.Glide
import br.com.PhG22.sglib.controller.AdminController

class PendingReturnAdapter(
    private val context: Context,
    private var loanList: List<Emprestimo>,
    private val onConfirmClick: (Emprestimo) -> Unit
) : RecyclerView.Adapter<PendingReturnAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.ivReturnCover)
        val tvBookTitle: TextView = view.findViewById(R.id.tvReturnBookTitle)
        val tvUserName: TextView = view.findViewById(R.id.tvReturnUserName)
        val btnConfirm: Button = view.findViewById(R.id.btnConfirmReturn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_pending_return, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = loanList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val loan = loanList[position]

        holder.tvBookTitle.text = loan.bookTitle
        holder.tvUserName.text = "Devolvido por: (Carregando...)"

        AdminController.getUserNameById(loan.userId) { nomeDoUsuario ->
            if (holder.adapterPosition != RecyclerView.NO_POSITION && holder.adapterPosition < loanList.size) {
                val currentLoan = loanList[holder.adapterPosition]
                if (currentLoan.id == loan.id) {
                    holder.tvUserName.text = "Devolvido por: $nomeDoUsuario"
                }
            }
        }

        Glide.with(context).load(loan.bookImageUrl).into(holder.ivCover)

        holder.btnConfirm.setOnClickListener { onConfirmClick(loan) }
    }

    fun updateData(newLoanList: List<Emprestimo>) {
        this.loanList = newLoanList
        notifyDataSetChanged()
    }
}