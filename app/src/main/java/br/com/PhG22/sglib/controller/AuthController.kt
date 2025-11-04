package br.com.PhG22.sglib.controller

// No pacote: controller
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import br.com.PhG22.sglib.model.Usuario
import android.util.Log

object AuthController {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val adminsCollection = db.collection("admins")

    // Função para registrar um novo usuário
    fun registerUser(
        email: String,
        pass: String,
        nome: String,
        telefone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    // Cria o objeto Usuario
                    val usuario = Usuario(
                        uid = firebaseUser.uid,
                        nome = nome,
                        email = email,
                        telefone = telefone,
                        cadastroAprovado = false // Cadastro precisa de aprovação
                    )

                    // Salva no Firestore
                    usersCollection.document(firebaseUser.uid).set(usuario)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onError(e.message ?: "Error saving profile") }
                }
            }
            .addOnFailureListener { e -> onError(e.message ?: "Registration failed") }
    }
    fun loginUser(
        email: String,
        pass: String,
        onAdminLogin: () -> Unit,      // Callback para login de Admin
        onUserLogin: () -> Unit,       // Callback para login de Usuário aprovado
        onPendingApproval: () -> Unit, // Callback para Usuário pendente
        onError: (String) -> Unit       // Callback para qualquer erro
    ) {
        // 1. Tenta fazer login com o Firebase Auth
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                if (firebaseUser == null) {
                    onError("Falha ao obter dados do usuário.")
                    return@addOnSuccessListener
                }

                val uid = firebaseUser.uid

                // 2. Login OK. Agora, vamos verificar o TIPO de usuário no Firestore
                // Primeiro, checa se é um Administrador
                adminsCollection.document(uid).get()
                    .addOnSuccessListener { adminDoc ->
                        if (adminDoc.exists()) {
                            // É UM ADMINISTRADOR
                            onAdminLogin()
                        } else {
                            // Não é admin, checa se é um Usuário
                            checkIfUser(uid, onUserLogin, onPendingApproval, onError)
                        }
                    }
                    .addOnFailureListener { e ->
                        // Erro ao checar admins, tenta checar usuários mesmo assim
                        Log.e("AuthController", "Erro ao checar admin: ${e.message}")
                        checkIfUser(uid, onUserLogin, onPendingApproval, onError)
                    }
            }
            .addOnFailureListener { e ->
                // 4. Falha no login (senha errada, usuário não existe, etc)
                onError(e.message ?: "Email ou senha inválidos.")
            }
    }

    /**
     * Função auxiliar para checar a coleção 'users'
     */
    private fun checkIfUser(
        uid: String,
        onUserLogin: () -> Unit,
        onPendingApproval: () -> Unit,
        onError: (String) -> Unit
    ) {
        usersCollection.document(uid).get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists()) {
                    // É UM USUÁRIO. Agora, checa se está aprovado.
                    val usuario = userDoc.toObject(Usuario::class.java)
                    if (usuario?.cadastroAprovado == true) {
                        // USUÁRIO APROVADO
                        onUserLogin()
                    } else {
                        // USUÁRIO PENDENTE DE APROVAÇÃO
                        onPendingApproval()
                    }
                } else {
                    // Problema: Autenticado mas sem registro no DB
                    onError("Perfil não encontrado. Contate o suporte.")
                    auth.signOut() // Desloga para segurança
                }
            }
            .addOnFailureListener { e ->
                onError("Erro ao verificar dados do perfil: ${e.message}")
            }
    }
    // TODO: Implementar registerAdmin (com código)
    // TODO: Implementar loginUser (com verificação de aprovação e tipo de usuário)
}