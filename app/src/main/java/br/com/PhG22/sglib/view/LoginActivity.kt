package br.com.PhG22.sglib.view

// No pacote: view
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText // Importar
import android.widget.TextView
import android.widget.Toast // Importar
import androidx.appcompat.app.AppCompatActivity
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.controller.AuthController

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Referências da UI
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        val tvRegisterAdmin = findViewById<TextView>(R.id.tvRegisterAdmin)

        // Navegação para Cadastro de Usuário
        tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterUserActivity::class.java))
        }

        tvRegisterAdmin.setOnClickListener {
            // TODO: Implementar a tela de registro de administrador
        }

        // --- LÓGICA DE LOGIN ---
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha o email e a senha.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // (Opcional: mostrar um ProgressBar de loading aqui)

            // Chama o Controller para fazer a lógica
            AuthController.loginUser(email, password,
                onAdminLogin = {
                    // (Opcional: esconder o ProgressBar)
                    Toast.makeText(this, "Login como Admin!", Toast.LENGTH_SHORT).show()
                    navigateTo(AdminMainActivity::class.java)
                },
                onUserLogin = {
                    // (Opcional: esconder o ProgressBar)
                    Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                    navigateTo(UserMainActivity::class.java)
                },
                onPendingApproval = {
                    // (Opcional: esconder o ProgressBar)
                    Toast.makeText(this, "Cadastro pendente de aprovação.", Toast.LENGTH_LONG).show()
                    navigateTo(PendingApprovalActivity::class.java)
                },
                onError = { errorMessage ->
                    // (Opcional: esconder o ProgressBar)
                    Toast.makeText(this, "Erro: $errorMessage", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    /**
     * Função auxiliar para navegar para uma nova Activity e limpar a pilha anterior
     * (para que o usuário não possa "voltar" para a tela de login)
     */
    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finaliza a LoginActivity
    }
}