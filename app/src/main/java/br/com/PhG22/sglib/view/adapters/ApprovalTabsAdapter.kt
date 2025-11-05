package br.com.PhG22.sglib.view.adapters
// No pacote: view.adapters
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import br.com.PhG22.sglib.view.admin_fragments.PendingLoansFragment
import br.com.PhG22.sglib.view.admin_fragments.PendingReturnsFragment
import br.com.PhG22.sglib.view.admin_fragments.PendingReviewsFragment
import br.com.PhG22.sglib.view.admin_fragments.PendingUsersFragment

// Este adapter controla quais fragmentos aparecem em quais abas
class ApprovalTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragments = listOf(
        PendingUsersFragment(),
        PendingLoansFragment(),
        PendingReturnsFragment(),
        PendingReviewsFragment() // <-- Adicionar Novo Fragmento
    )

    val tabTitles = listOf(
        "Usuários",
        "Empréstimos",
        "Devoluções",
        "Resenhas" // <-- Adicionar Novo Título
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}