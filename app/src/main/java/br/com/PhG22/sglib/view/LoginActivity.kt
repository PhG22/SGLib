package br.com.PhG22.sglib.view
// No pacote: br.com.PhG22.sglib.view
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.PhG22.sglib.R // Use o seu R
import br.com.PhG22.sglib.controller.AuthController // Use o seu AuthController

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Referências da UI
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)

        // Referência para o link de registro de admin (UC1)
        val tvRegisterAdmin = findViewById<TextView>(R.id.tvRegisterAdmin)

        // Navegação para Cadastro de Usuário (UC2)
        tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterUserActivity::class.java))
        }

        // Navegação para Cadastro de Admin (UC1)
        tvRegisterAdmin.setOnClickListener {
            startActivity(Intent(this, RegisterAdminActivity::class.java))
        }

        // Lógica de Login
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha o email e a senha.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // (Opcional: mostrar um ProgressBar de loading aqui)

            // Chama o Controller, que faz toda a verificação
            AuthController.loginUser(email, password,
                onAdminLogin = {
                    // Usuário é um Admin
                    Toast.makeText(this, "Login como Admin!", Toast.LENGTH_SHORT).show()
                    navigateTo(AdminMainActivity::class.java)
                },
                onUserLogin = {
                    // Usuário é um usuário comum APROVADO
                    Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                    navigateTo(UserMainActivity::class.java)
                },
                onPendingApproval = {
                    // Usuário é um usuário comum PENDENTE
                    Toast.makeText(this, "Cadastro pendente de aprovação.", Toast.LENGTH_LONG).show()
                    navigateTo(PendingApprovalActivity::class.java)
                },
                onError = { errorMessage ->
                    // Falha no login (senha errada, etc)
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
        // Limpa todas as activities anteriores da pilha
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finaliza a LoginActivity
    }
}