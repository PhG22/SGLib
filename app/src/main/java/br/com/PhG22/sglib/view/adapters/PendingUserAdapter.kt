package br.com.PhG22.sglib.view.adapters
// No pacote: view.adapters
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.model.Usuario

class PendingUserAdapter(
    private var userList: List<Usuario>,
    private val onApproveClick: (Usuario) -> Unit,
    private val onDenyClick: (Usuario) -> Unit
) : RecyclerView.Adapter<PendingUserAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvUserNamePending)
        val tvEmail: TextView = view.findViewById(R.id.tvUserEmailPending)
        val btnApprove: Button = view.findViewById(R.id.btnApproveUser)
        val btnDeny: Button = view.findViewById(R.id.btnDenyUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_user, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]

        holder.tvName.text = user.nome
        holder.tvEmail.text = user.email

        holder.btnApprove.setOnClickListener {
            onApproveClick(user)
        }

        holder.btnDeny.setOnClickListener {
            onDenyClick(user)
        }
    }

    fun updateData(newUserList: List<Usuario>) {
        this.userList = newUserList
        notifyDataSetChanged()
    }
}