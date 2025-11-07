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
import br.com.PhG22.sglib.model.Emprestimo
import com.bumptech.glide.Glide
import br.com.PhG22.sglib.controller.AdminController

class PendingLoanAdapter(
    private val context: Context,
    private var loanList: List<Emprestimo>,
    private val onApproveClick: (Emprestimo) -> Unit,
    private val onDenyClick: (Emprestimo) -> Unit
) : RecyclerView.Adapter<PendingLoanAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.ivLoanCover)
        val tvBookTitle: TextView = view.findViewById(R.id.tvLoanBookTitle)
        val tvUserName: TextView = view.findViewById(R.id.tvLoanUserName)
        val btnApprove: ImageButton = view.findViewById(R.id.btnApproveLoan)
        val btnDeny: ImageButton = view.findViewById(R.id.btnDenyLoan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_pending_loan, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = loanList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val loan = loanList[position]

        holder.tvBookTitle.text = loan.bookTitle
        holder.tvUserName.text = "Solicitado por: (Carregando...)" // Precisamos buscar o nome do usuário

        // 2. Busca o nome do usuário de forma assíncrona
        AdminController.getUserNameById(loan.userId) { nomeDoUsuario ->
            // 3. Verifica se a view ainda está vinculada a este item
            // (Importante para evitar que o nome apareça no item errado ao reciclar)
            if (holder.adapterPosition != RecyclerView.NO_POSITION && holder.adapterPosition < loanList.size) {
                // Verificação extra para garantir que o item não foi reciclado para outro empréstimo
                val currentLoan = loanList[holder.adapterPosition]
                if (currentLoan.id == loan.id) {
                    holder.tvUserName.text = "Solicitado por: $nomeDoUsuario"
                }
            }
        }

        Glide.with(context).load(loan.bookImageUrl).into(holder.ivCover)

        holder.btnApprove.setOnClickListener { onApproveClick(loan) }
        holder.btnDeny.setOnClickListener { onDenyClick(loan) }
    }

    fun updateData(newLoanList: List<Emprestimo>) {
        this.loanList = newLoanList
        notifyDataSetChanged()
    }
}