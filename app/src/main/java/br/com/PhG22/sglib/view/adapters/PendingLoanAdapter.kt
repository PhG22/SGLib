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

        // TODO: Buscar o nome do usuário (loan.userId) no 'usersCollection'
        // e atualizar o 'tvUserName'

        Glide.with(context).load(loan.bookImageUrl).into(holder.ivCover)

        holder.btnApprove.setOnClickListener { onApproveClick(loan) }
        holder.btnDeny.setOnClickListener { onDenyClick(loan) }
    }

    fun updateData(newLoanList: List<Emprestimo>) {
        this.loanList = newLoanList
        notifyDataSetChanged()
    }
}