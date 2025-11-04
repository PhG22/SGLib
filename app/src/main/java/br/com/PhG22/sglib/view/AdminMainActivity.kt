package br.com.PhG22.sglib.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.view.admin_fragments.ApprovalsFragment
import br.com.PhG22.sglib.view.admin_fragments.ManageBooksFragment
import br.com.PhG22.sglib.view.admin_fragments.ManageFinesFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.admin_bottom_navigation)
        bottomNav.setOnItemSelectedListener(navListener)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.admin_fragment_container,
                ManageBooksFragment()
            ).commit()
        }
    }

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        var selectedFragment: Fragment? = null

        when (item.itemId) {
            R.id.nav_admin_books -> selectedFragment = ManageBooksFragment()
            R.id.nav_admin_approvals -> selectedFragment = ApprovalsFragment()
            R.id.nav_admin_fines -> selectedFragment = ManageFinesFragment()
        }

        if (selectedFragment != null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.admin_fragment_container,
                selectedFragment
            ).commit()
        }
        true
    }
}