package br.com.PhG22.sglib.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.controller.AdminController // <-- Importar o AdminController
import br.com.PhG22.sglib.model.Fine
import java.text.NumberFormat
import java.util.Locale

class ManageFinesAdapter(
    private var finesList: List<Fine>,
    private val onConfirmPaymentClick: (Fine) -> Unit
) : RecyclerView.Adapter<ManageFinesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvFineUserName)
        val tvReason: TextView = view.findViewById(R.id.tvFineReason)
        val tvAmount: TextView = view.findViewById(R.id.tvFineAmount)
        val btnConfirm: Button = view.findViewById(R.id.btnConfirmPayment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_fine, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = finesList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fine = finesList[position]
        val format = NumberFormat.getCurrencyInstance(Locale.US)

        holder.tvReason.text = fine.reason
        holder.tvAmount.text = format.format(fine.amount)

        // --- INÍCIO DA CORREÇÃO DO "(Carregando...)" ---
        holder.tvUserName.text = "Usuário: (Carregando...)"

        AdminController.getUserNameById(fine.userId) { nomeDoUsuario ->
            if (holder.adapterPosition != RecyclerView.NO_POSITION && holder.adapterPosition < finesList.size) {
                val currentFine = finesList[holder.adapterPosition]
                if (currentFine.id == fine.id) {
                    holder.tvUserName.text = "Usuário: $nomeDoUsuario"
                }
            }
        }
        // --- FIM DA CORREÇÃO ---

        holder.btnConfirm.setOnClickListener {
            onConfirmPaymentClick(fine)
        }
    }

    fun updateData(newFinesList: List<Fine>) {
        this.finesList = newFinesList
        notifyDataSetChanged()
    }
}