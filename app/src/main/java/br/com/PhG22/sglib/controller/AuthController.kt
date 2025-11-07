package br.com.PhG22.sglib.controller

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import br.com.PhG22.sglib.model.Administrador
import br.com.PhG22.sglib.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

object AuthController {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val adminsCollection = db.collection("admins")
    private val inviteCodesCollection = db.collection("admin_invite_codes")

    // Constantes para SharedPreferences Encriptadas
    private const val PREF_FILE_NAME = "sglib_secure_prefs"
    private const val PREF_EMAIL_KEY = "biometric_email"
    private const val PREF_PASSWORD_KEY = "biometric_password"

    // --- LÓGICA DE LOGOUT (NOVA) ---

    fun logout(context: Context) {
        // Limpa as credenciais de biometria guardadas
        clearBiometricCredentials(context)
        // Faz logout do Firebase
        auth.signOut()
    }

    // --- LÓGICA DE GESTÃO DE CREDENCIAIS (EXISTENTE) ---
    private fun getEncryptedPrefs(context: Context): EncryptedSharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            PREF_FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun saveBiometricCredentials(context: Context, email: String, pass: String) {
        val prefs = getEncryptedPrefs(context)
        with(prefs.edit()) {
            putString(PREF_EMAIL_KEY, email)
            putString(PREF_PASSWORD_KEY, pass)
            apply()
        }
    }

    fun getBiometricCredentials(context: Context): Pair<String, String>? {
        val prefs = getEncryptedPrefs(context)
        val email = prefs.getString(PREF_EMAIL_KEY, null)
        val password = prefs.getString(PREF_PASSWORD_KEY, null)

        return if (email != null && password != null) {
            Pair(email, password)
        } else {
            null
        }
    }

    fun clearBiometricCredentials(context: Context) {
        val prefs = getEncryptedPrefs(context)
        with(prefs.edit()) {
            remove(PREF_EMAIL_KEY)
            remove(PREF_PASSWORD_KEY)
            apply()
        }
    }

    // --- LÓGICA DE AUTENTICAÇÃO BIOMÉTRICA (EXISTENTE) ---

    fun canAuthenticateWithBiometrics(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("AUTH_BIOMETRIC", "App pode autenticar com biometria.")
                return true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e("AUTH_BIOMETRIC", "Hardware de biometria não disponível.")
                return false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.e("AUTH_BIOMETRIC", "Hardware de biometria indisponível no momento.")
                return false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.e("AUTH_BIOMETRIC", "Nenhuma biometria registada no dispositivo.")
                return false
            }
            else -> {
                Log.e("AUTH_BIOMETRIC", "Erro biométrico desconhecido.")
                return false
            }
        }
    }

    fun showBiometricPrompt(
        activity: AppCompatActivity, // Precisa da Activity para mostrar o diálogo
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login Biométrico")
            .setSubtitle("Use a sua impressão digital ou rosto para entrar")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e("AUTH_BIOMETRIC", "Erro de autenticação: $errString ($errorCode)")
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                        onError("Falha na autenticação: $errString")
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d("AUTH_BIOMETRIC", "Autenticação bem-sucedida!")
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w("AUTH_BIOMETRIC", "Biometria não reconhecida.")
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }


    // --- LÓGICA DE LOGIN/REGISTO EXISTENTE (sem alterações) ---

    fun loginUser(
        email: String,
        pass: String,
        onAdminLogin: () -> Unit,
        onUserLogin: () -> Unit,
        onPendingApproval: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                if (firebaseUser == null) {
                    onError("Falha ao obter dados do usuário.")
                    return@addOnSuccessListener
                }

                val uid = firebaseUser.uid

                adminsCollection.document(uid).get()
                    .addOnSuccessListener { adminDoc ->
                        if (adminDoc.exists()) {
                            onAdminLogin()
                        } else {
                            checkIfUser(uid, onUserLogin, onPendingApproval, onError)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("AuthController", "Erro ao checar admin: ${e.message}")
                        checkIfUser(uid, onUserLogin, onPendingApproval, onError)
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Email ou senha inválidos.")
            }
    }

    private fun checkIfUser(
        uid: String,
        onUserLogin: () -> Unit,
        onPendingApproval: () -> Unit,
        onError: (String) -> Unit
    ) {
        usersCollection.document(uid).get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists()) {
                    val usuario = userDoc.toObject(Usuario::class.java)
                    if (usuario?.cadastroAprovado == true) {
                        onUserLogin()
                    } else {
                        onPendingApproval()
                    }
                } else {
                    onError("Perfil não encontrado. Contate o suporte.")
                    auth.signOut()
                }
            }
            .addOnFailureListener { e ->
                onError("Erro ao verificar dados do perfil: ${e.message}")
            }
    }

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
                    val usuario = Usuario(
                        uid = firebaseUser.uid,
                        nome = nome,
                        email = email,
                        telefone = telefone,
                        cadastroAprovado = false,
                        fotoUrl = "" // Foto começa vazia
                    )

                    usersCollection.document(firebaseUser.uid).set(usuario)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onError(e.message ?: "Error saving profile") }
                }
            }
            .addOnFailureListener { e -> onError(e.message ?: "Registration failed") }
    }

    fun registerAdmin(
        email: String,
        pass: String,
        nome: String,
        inviteCode: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val codeRef = inviteCodesCollection.document(inviteCode)
        codeRef.get()
            .addOnSuccessListener { codeDoc ->
                if (!codeDoc.exists()) {
                    onError("Código de convite inválido.")
                    return@addOnSuccessListener
                }
                if (codeDoc.getBoolean("isUsed") == true) {
                    onError("Código de convite já utilizado.")
                    return@addOnSuccessListener
                }

                auth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener { authResult ->
                        val firebaseUser = authResult.user
                        if (firebaseUser == null) {
                            onError("Erro ao criar usuário, UID nulo.")
                            return@addOnSuccessListener
                        }

                        val newAdmin = Administrador(
                            uid = firebaseUser.uid,
                            nome = nome,
                            email = email,
                            telefone = "", // Telefone começa vazio
                            fotoUrl = ""  // Foto começa vazia
                        )

                        db.runTransaction { transaction ->
                            val adminRef = adminsCollection.document(firebaseUser.uid)

                            transaction.set(adminRef, newAdmin)
                            transaction.update(codeRef, "isUsed", true)

                            null
                        }
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { e -> onError("Erro na transação: ${e.message}") }

                    }
                    .addOnFailureListener { e -> onError("Erro ao criar Auth: ${e.message}") }
            }
            .addOnFailureListener { e ->
                onError("Erro ao verificar código: ${e.message}")
            }
    }
}