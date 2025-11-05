package br.com.PhG22.sglib.view
// No pacote: view
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.controller.AuthController

class RegisterAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_admin)

        val etName = findViewById<EditText>(R.id.etAdminName)
        val etEmail = findViewById<EditText>(R.id.etAdminEmail)
        val etPassword = findViewById<EditText>(R.id.etAdminPassword)
        val etCode = findViewById<EditText>(R.id.etAdminInviteCode)
        val btnRegister = findViewById<Button>(R.id.btnRegisterAdmin)

        btnRegister.setOnClickListener {
            val nome = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val senha = etPassword.text.toString().trim()
            val code = etCode.text.toString().trim()

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || code.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // (Opcional: mostrar um ProgressBar)
            btnRegister.isEnabled = false

            AuthController.registerAdmin(email, senha, nome, code,
                onSuccess = {
                    // Sucesso!
                    Toast.makeText(this, "Administrador registrado! Faça o login.", Toast.LENGTH_LONG).show()
                    // Envia de volta para a tela de login
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                },
                onError = { errorMessage ->
                    // (Opcional: esconder ProgressBar)
                    btnRegister.isEnabled = true
                    Toast.makeText(this, "Erro: $errorMessage", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}