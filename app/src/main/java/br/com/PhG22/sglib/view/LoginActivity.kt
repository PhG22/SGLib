package br.com.PhG22.sglib.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.controller.AuthController

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignUp: TextView
    private lateinit var tvRegisterAdmin: TextView
    private lateinit var btnBiometricLogin: ImageButton
    private lateinit var tvBiometricLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Referências da UI
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignUp = findViewById(R.id.tvSignUp)
        tvRegisterAdmin = findViewById(R.id.tvRegisterAdmin)
        btnBiometricLogin = findViewById(R.id.btnBiometricLogin)
        tvBiometricLogin = findViewById(R.id.tvBiometricLogin)

        // Navegação para Cadastro de Usuário (UC2)
        tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterUserActivity::class.java))
        }

        // Navegação para Cadastro de Admin (UC1)
        tvRegisterAdmin.setOnClickListener {
            startActivity(Intent(this, RegisterAdminActivity::class.java))
        }

        // --- LÓGICA DE LOGIN MANUAL ---
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha o email e a senha.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // (Opcional: mostrar um ProgressBar de loading aqui)
            setLoading(true)

            // Chama o Controller, que faz toda a verificação
            AuthController.loginUser(email, password,
                onAdminLogin = {
                    setLoading(false)
                    // Pergunta se quer guardar a biometria
                    promptToSaveBiometrics(email, password) {
                        navigateTo(AdminMainActivity::class.java)
                    }
                },
                onUserLogin = {
                    setLoading(false)
                    // Pergunta se quer guardar a biometria
                    promptToSaveBiometrics(email, password) {
                        navigateTo(UserMainActivity::class.java)
                    }
                },
                onPendingApproval = {
                    setLoading(false)
                    navigateTo(PendingApprovalActivity::class.java)
                },
                onError = { errorMessage ->
                    setLoading(false)
                    Toast.makeText(this, "Erro: $errorMessage", Toast.LENGTH_LONG).show()
                }
            )
        }

        // --- LÓGICA DE LOGIN BIOMÉTRICO ---
        btnBiometricLogin.setOnClickListener {
            performBiometricLogin()
        }

        checkBiometricSupport()
    }

    /**
     * Verifica se o botão de biometria deve ser mostrado.
     */
    private fun checkBiometricSupport() {
        val canAuth = AuthController.canAuthenticateWithBiometrics(this)
        val hasCredentials = AuthController.getBiometricCredentials(this) != null

        if (canAuth && hasCredentials) {
            // Se o hardware existe E tem credenciais guardadas, mostra o botão
            btnBiometricLogin.visibility = View.VISIBLE
            tvBiometricLogin.visibility = View.VISIBLE
        } else {
            btnBiometricLogin.visibility = View.GONE
            tvBiometricLogin.visibility = View.GONE
        }
    }

    /**
     * Tenta fazer o login com biometria.
     */
    private fun performBiometricLogin() {
        // 1. Pede a impressão digital/rosto
        AuthController.showBiometricPrompt(this,
            onSuccess = {
                // 2. Biometria OK! Busca as credenciais guardadas
                val credentials = AuthController.getBiometricCredentials(this)
                if (credentials != null) {
                    val email = credentials.first
                    val password = credentials.second

                    // 3. Tenta fazer o login no Firebase com as credenciais
                    setLoading(true)
                    AuthController.loginUser(email, password,
                        onAdminLogin = {
                            setLoading(false)
                            navigateTo(AdminMainActivity::class.java)
                        },
                        onUserLogin = {
                            setLoading(false)
                            navigateTo(UserMainActivity::class.java)
                        },
                        onPendingApproval = {
                            setLoading(false)
                            navigateTo(PendingApprovalActivity::class.java)
                        },
                        onError = {
                            // Erro! (ex: senha mudou no Firebase mas estava guardada)
                            setLoading(false)
                            Toast.makeText(this, "Erro de login biométrico: $it", Toast.LENGTH_LONG).show()
                            // Limpa as credenciais inválidas
                            AuthController.clearBiometricCredentials(this)
                            checkBiometricSupport() // Esconde o botão
                        }
                    )
                }
            },
            onError = { errorMsg ->
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    /**
     * Pergunta ao utilizador se quer guardar as credenciais para login biométrico.
     */
    private fun promptToSaveBiometrics(email: String, pass: String, onComplete: () -> Unit) {
        // Se o dispositivo suporta biometria E as credenciais ainda não foram guardadas
        if (AuthController.canAuthenticateWithBiometrics(this) && AuthController.getBiometricCredentials(this) == null) {
            AlertDialog.Builder(this)
                .setTitle("Ativar Login Biométrico?")
                .setMessage("Deseja usar a sua impressão digital ou rosto para entrar mais rápido da próxima vez?")
                .setPositiveButton("Sim, Ativar") { dialog, _ ->
                    AuthController.saveBiometricCredentials(this, email, pass)
                    Toast.makeText(this, "Login biométrico ativado!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    onComplete()
                }
                .setNegativeButton("Agora não") { dialog, _ ->
                    dialog.dismiss()
                    onComplete()
                }
                .setOnCancelListener {
                    onComplete() // Garante que a navegação acontece mesmo se o diálogo for cancelado
                }
                .show()
        } else {
            // Se não suporta biometria ou já está guardado, apenas navega
            onComplete()
        }
    }

    private fun setLoading(isLoading: Boolean) {
        btnLogin.isEnabled = !isLoading
        btnBiometricLogin.isEnabled = !isLoading
        // (Adicione um ProgressBar se desejar)
    }

    /**
     * Função auxiliar para navegar e limpar a pilha.
     */
    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finaliza a LoginActivity
    }
}