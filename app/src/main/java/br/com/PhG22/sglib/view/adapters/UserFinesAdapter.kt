package br.com.PhG22.sglib.view.adapters

// Este é um NOVO arquivo de Adapter. Crie-o em app/src/main/java/br/com/PhG22/sglib/view/adapters/
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.model.Fine
import java.text.NumberFormat
import java.util.Locale

class UserFinesAdapter(
    private var finesList: List<Fine>
) : RecyclerView.Adapter<UserFinesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvReason: TextView = view.findViewById(R.id.tvFineReasonUser)
        val tvAmount: TextView = view.findViewById(R.id.tvFineAmountUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_fine, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = finesList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fine = finesList[position]
        val format = NumberFormat.getCurrencyInstance(Locale.US) // Altere para sua localidade

        holder.tvReason.text = fine.reason
        holder.tvAmount.text = format.format(fine.amount)
    }

    fun updateData(newFinesList: List<Fine>) {
        this.finesList = newFinesList
        notifyDataSetChanged()
    }
}