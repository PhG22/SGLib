package br.com.PhG22.sglib.view
// No pacote: view
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.view.fragments.HomeFragment
import br.com.PhG22.sglib.view.fragments.MyFinesFragment
import br.com.PhG22.sglib.view.fragments.MyLoansFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Esta Activity agora é a principal "anfitriã" (host) da navegação do usuário.
 * Ela não exibe mais a lista de livros diretamente; em vez disso,
 * ela gerencia a troca entre HomeFragment, MyLoansFragment e MyFinesFragment.
 */
class UserMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Define o NOVO layout, que contém o FragmentContainerView e a BottomNavigationView
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

    // Listener para os cliques na barra de navegação
    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        var selectedFragment: Fragment? = null

        // Escolhe qual fragmento carregar com base no item clicado
        when (item.itemId) {
            R.id.nav_home -> selectedFragment = HomeFragment()
            R.id.nav_loans -> selectedFragment = MyLoansFragment()
            R.id.nav_fines -> selectedFragment = MyFinesFragment()
            // case R.id.nav_profile -> selectedFragment = ProfileFragment() // (Futuro)
        }

        // Troca o fragmento no contêiner
        if (selectedFragment != null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                selectedFragment
            ).commit()
        }
        true
    }
}