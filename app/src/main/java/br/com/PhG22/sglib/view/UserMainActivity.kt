package br.com.PhG22.sglib.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.view.fragments.HomeFragment
import br.com.PhG22.sglib.view.fragments.MyFinesFragment
import br.com.PhG22.sglib.view.fragments.MyLoansFragment
import br.com.PhG22.sglib.view.fragments.ProfileFragment // <-- ADICIONAR IMPORT
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener(navListener)

        // Carrega o fragmento inicial (Home)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                HomeFragment()
            ).commit()
        }
    }

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        var selectedFragment: Fragment? = null

        when (item.itemId) {
            R.id.nav_home -> selectedFragment = HomeFragment()
            R.id.nav_loans -> selectedFragment = MyLoansFragment()
            R.id.nav_fines -> selectedFragment = MyFinesFragment()
            R.id.nav_profile -> selectedFragment = ProfileFragment() // <-- ADICIONAR LINHA
        }

        if (selectedFragment != null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                selectedFragment
            ).commit()
        }
        true
    }
}