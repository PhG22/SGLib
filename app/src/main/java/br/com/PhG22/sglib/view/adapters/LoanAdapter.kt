package br.com.PhG22.sglib.view.adapters
// No pacote: view.adapters
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.model.Emprestimo
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class LoanAdapter(
    private val context: Context,
    private var loanList: List<Emprestimo>,
    private val onReturnClick: (Emprestimo) -> Unit
) : RecyclerView.Adapter<LoanAdapter.LoanViewHolder>() {

    class LoanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.ivLoanBookCover)
        val tvTitle: TextView = view.findViewById(R.id.tvLoanBookTitle)
        val tvDueDate: TextView = view.findViewById(R.id.tvLoanDueDate)
        val tvStatus: TextView = view.findViewById(R.id.tvLoanStatus)
        val btnReturn: Button = view.findViewById(R.id.btnRequestReturn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_loan, parent, false)
        return LoanViewHolder(view)
    }

    override fun getItemCount(): Int = loanList.size

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {
        val loan = loanList[position]

        holder.tvTitle.text = loan.bookTitle
        Glide.with(context).load(loan.bookImageUrl).into(holder.ivCover)

        // Formata a Data
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dueDateStr = loan.dueDate?.let { sdf.format(it) } ?: "N/A"

        // Lógica de Status
        when (loan.status) {
            "pending" -> {
                holder.tvStatus.text = "Pendente de Aprovação"
                holder.tvDueDate.visibility = View.GONE
                holder.btnReturn.visibility = View.GONE
            }
            "approved" -> {
                holder.tvStatus.text = "Emprestado"
                holder.tvDueDate.text = "Devolver até: $dueDateStr"
                holder.tvDueDate.visibility = View.VISIBLE
                holder.btnReturn.visibility = View.VISIBLE // UC5: Mostrar botão de devolver
            }
            "return_pending" -> {
                holder.tvStatus.text = "Aguardando Confirmação de Devolução"
                holder.tvDueDate.visibility = View.GONE
                holder.btnReturn.visibility = View.GONE
            }
            "returned" -> {
                holder.tvStatus.text = "Devolvido"
                holder.tvDueDate.visibility = View.GONE
                holder.btnReturn.visibility = View.GONE
            }
        }

        holder.btnReturn.setOnClickListener {
            onReturnClick(loan)
        }
    }

    fun updateData(newLoanList: List<Emprestimo>) {
        this.loanList = newLoanList
        notifyDataSetChanged()
    }
}