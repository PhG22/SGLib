package br.com.PhG22.sglib.view.admin_fragments
// No pacote: view.admin_fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.controller.AdminController
import br.com.PhG22.sglib.model.Resenha
import br.com.PhG22.sglib.view.adapters.PendingReviewAdapter

class PendingReviewsFragment : Fragment() {

    private lateinit var rvPendingReviews: RecyclerView
    private lateinit var adapter: PendingReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pending_reviews, container, false)
        rvPendingReviews = view.findViewById(R.id.rvPendingReviews)
        setupRecyclerView()
        return view
    }

    override fun onResume() {
        super.onResume()
        loadPendingReviews()
    }

    private fun setupRecyclerView() {
        adapter = PendingReviewAdapter(emptyList(),
            onApproveClick = { review ->
                AdminController.approveReview(review.id,
                    onSuccess = {
                        showToast("Resenha aprovada.")
                        loadPendingReviews()
                    },
                    onError = { showToast("Erro: $it") }
                )
            },
            onDeleteClick = { review ->
                AdminController.denyReview(review.id,
                    onSuccess = {
                        showToast("Resenha excluída.")
                        loadPendingReviews()
                    },
                    onError = { showToast("Erro: $it") }
                )
            }
        )
        rvPendingReviews.layoutManager = LinearLayoutManager(context)
        rvPendingReviews.adapter = adapter
    }

    private fun loadPendingReviews() {
        AdminController.getPendingReviews(
            onSuccess = { reviews ->
                adapter.updateData(reviews)
            },
            onError = { showToast("Erro: $it") }
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}