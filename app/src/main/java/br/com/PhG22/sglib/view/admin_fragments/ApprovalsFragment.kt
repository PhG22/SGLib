package br.com.PhG22.sglib.view.admin_fragments
// No pacote: view.admin_fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.view.adapters.ApprovalTabsAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

// Esta classe agora é SÓ um contêiner para as abas
class ApprovalsFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ApprovalTabsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_approvals, container, false)

        tabLayout = view.findViewById(R.id.tab_layout_approvals)
        viewPager = view.findViewById(R.id.view_pager_approvals)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura o adapter para o ViewPager
        adapter = ApprovalTabsAdapter(this)
        viewPager.adapter = adapter

        // Conecta o TabLayout ao ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.tabTitles[position]
        }.attach()
    }
}
