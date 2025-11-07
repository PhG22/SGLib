package br.com.PhG22.sglib.controller

import br.com.PhG22.sglib.model.Administrador
import br.com.PhG22.sglib.model.Usuario
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

object ProfileController {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val adminsCollection = db.collection("admins")

    private fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Verifica se o utilizador logado é um Admin.
     */
    fun isUserAdmin(callback: (Boolean) -> Unit) {
        val uid = getCurrentUser()?.uid ?: run {
            callback(false)
            return
        }
        adminsCollection.document(uid).get()
            .addOnSuccessListener { document ->
                callback(document.exists())
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    /**
     * Carrega os dados do perfil, quer seja Admin ou Utilizador.
     */
    fun loadProfile(
        onSuccess: (profile: Any) -> Unit, // Retorna Usuario ou Administrador
        onError: (String) -> Unit
    ) {
        val uid = getCurrentUser()?.uid
        if (uid == null) {
            onError("Utilizador não logado.")
            return
        }

        // Primeiro, tenta carregar como Admin
        adminsCollection.document(uid).get()
            .addOnSuccessListener { adminDoc ->
                if (adminDoc.exists()) {
                    val admin = adminDoc.toObject<Administrador>()
                    if (admin != null) {
                        onSuccess(admin)
                    } else {
                        onError("Erro ao ler dados de Admin.")
                    }
                } else {
                    // Se não for Admin, tenta carregar como Utilizador
                    usersCollection.document(uid).get()
                        .addOnSuccessListener { userDoc ->
                            if (userDoc.exists()) {
                                val user = userDoc.toObject<Usuario>()
                                if (user != null) {
                                    onSuccess(user)
                                } else {
                                    onError("Erro ao ler dados de Utilizador.")
                                }
                            } else {
                                onError("Perfil não encontrado.")
                            }
                        }
                        .addOnFailureListener { e -> onError(e.message ?: "Erro") }
                }
            }
            .addOnFailureListener { e -> onError(e.message ?: "Erro") }
    }

    /**
     * Atualiza os dados do perfil no Firestore.
     */
    fun updateProfile(
        isAdmin: Boolean,
        nome: String,
        telefone: String,
        fotoBase64: String?, // String Base64 da nova foto, ou null se não mudou
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = getCurrentUser()?.uid ?: return

        val collection = if (isAdmin) adminsCollection else usersCollection

        val updates = mutableMapOf<String, Any>(
            "nome" to nome,
            "telefone" to telefone
        )

        // Se uma nova foto foi fornecida (fotoBase64 não é null),
        // adiciona-a ao mapa de atualização.
        if (fotoBase64 != null) {
            updates["fotoUrl"] = fotoBase64
        }

        collection.document(uid).update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Erro ao atualizar") }
    }

    /**
     * Reautentica o utilizador com a sua senha antes de o excluir.
     */
    fun reauthenticateAndDeleteAccount(
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = getCurrentUser()
        val email = user?.email
        if (user == null || email == null) {
            onError("Utilizador não encontrado.")
            return
        }

        val credential = EmailAuthProvider.getCredential(email, password)

        // 1. Reautenticar
        user.reauthenticate(credential)
            .addOnSuccessListener {
                // 2. Reautenticação OK. Agora, excluir.
                deleteAccountData(user, onSuccess, onError)
            }
            .addOnFailureListener { e ->
                onError("Senha incorreta. ${e.message}")
            }
    }

    /**
     * Exclui os dados do Firestore e, em seguida, a conta de Autenticação.
     */
    private fun deleteAccountData(
        user: FirebaseUser,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = user.uid

        // 3. Excluir do Firestore (tentar em ambas as coleções por segurança)
        usersCollection.document(uid).delete()
            .addOnCompleteListener { userDeleteTask ->
                adminsCollection.document(uid).delete()
                    .addOnCompleteListener { adminDeleteTask ->

                        // 4. Excluir do Auth
                        user.delete()
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { e -> onError("Falha ao excluir conta Auth: ${e.message}") }
                    }
            }
    }
}