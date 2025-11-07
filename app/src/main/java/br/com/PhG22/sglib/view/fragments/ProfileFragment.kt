package br.com.PhG22.sglib.view.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.controller.AuthController
import br.com.PhG22.sglib.controller.ProfileController
import br.com.PhG22.sglib.model.Administrador
import br.com.PhG22.sglib.model.Usuario
import br.com.PhG22.sglib.view.LoginActivity
import com.bumptech.glide.Glide
import java.io.ByteArrayOutputStream
import java.io.File

class ProfileFragment : Fragment() {

    // Views
    private lateinit var ivProfilePic: ImageView
    private lateinit var btnChangeProfilePic: View
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSaveChanges: Button
    private lateinit var btnLogout: Button
    private lateinit var btnDeleteAccount: Button
    private lateinit var progressBar: ProgressBar

    // Estado
    private var imageUri: Uri? = null // Nova imagem selecionada
    private var newImageBase64: String? = null // Nova imagem já convertida
    private var isAdmin: Boolean = false

    // --- Launchers para Imagem (Câmera, Galeria, Permissão) ---

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) { launchCamera() } else {
                showToast("Permissão da câmera negada")
            }
        }

    private val pickMediaLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                imageUri = uri
                Glide.with(this).load(imageUri).into(ivProfilePic)
                convertImageToBase64() // Converte a imagem assim que selecionada
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                // O URI já está em 'imageUri'
                Glide.with(this).load(imageUri).into(ivProfilePic)
                convertImageToBase64() // Converte a imagem assim que tirada
            }
        }

    // --- Fim dos Launchers ---

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Mapeamento de Views
        ivProfilePic = view.findViewById(R.id.ivProfilePic)
        btnChangeProfilePic = view.findViewById(R.id.btnChangeProfilePic)
        etName = view.findViewById(R.id.etProfileName)
        etEmail = view.findViewById(R.id.etProfileEmail)
        etPhone = view.findViewById(R.id.etProfilePhone)
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount)
        progressBar = view.findViewById(R.id.profileProgressBar)

        // Configuração de Cliques
        btnChangeProfilePic.setOnClickListener { showImageSourceDialog() }
        btnSaveChanges.setOnClickListener { saveProfileChanges() }
        btnLogout.setOnClickListener { logout() }
        btnDeleteAccount.setOnClickListener { showDeleteAccountDialog() }

        loadProfileData()
        return view
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSaveChanges.isEnabled = !isLoading
        btnLogout.isEnabled = !isLoading
        btnDeleteAccount.isEnabled = !isLoading
    }

    /**
     * Carrega os dados do perfil (Admin ou Utilizador) do Firestore
     */
    private fun loadProfileData() {
        setLoading(true)
        ProfileController.loadProfile(
            onSuccess = { profile ->
                setLoading(false)
                when (profile) {
                    is Usuario -> {
                        isAdmin = false
                        etName.setText(profile.nome)
                        etEmail.setText(profile.email)
                        etPhone.setText(profile.telefone)
                        Glide.with(this).load(profile.fotoUrl)
                            .placeholder(R.drawable.ic_profile).into(ivProfilePic)
                    }
                    is Administrador -> {
                        isAdmin = true
                        etName.setText(profile.nome)
                        etEmail.setText(profile.email)
                        etPhone.setText(profile.telefone)
                        Glide.with(this).load(profile.fotoUrl)
                            .placeholder(R.drawable.ic_profile).into(ivProfilePic)
                    }
                }
            },
            onError = { errorMsg ->
                setLoading(false)
                showToast(errorMsg)
            }
        )
    }

    /**
     * Salva as alterações de nome, telefone e foto (se houver)
     */
    private fun saveProfileChanges() {
        val nome = etName.text.toString().trim()
        val telefone = etPhone.text.toString().trim()

        if (nome.isEmpty() || telefone.isEmpty()) {
            showToast("Nome e Telefone não podem estar vazios.")
            return
        }

        setLoading(true)

        // 'newImageBase64' só é preenchido se o utilizador selecionou uma nova imagem
        ProfileController.updateProfile(isAdmin, nome, telefone, newImageBase64,
            onSuccess = {
                setLoading(false)
                showToast("Perfil atualizado com sucesso!")
                newImageBase64 = null // Limpa a imagem para o próximo save
                imageUri = null
            },
            onError = {
                setLoading(false)
                showToast("Erro ao atualizar: $it")
            }
        )
    }

    /**
     * Faz logout e limpa as credenciais de biometria
     */
    private fun logout() {
        AuthController.logout(requireContext())
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    /**
     * Mostra o diálogo de confirmação de senha para excluir a conta
     */
    private fun showDeleteAccountDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reauthenticate, null)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPasswordConfirm)

        AlertDialog.Builder(requireContext())
            .setTitle("Excluir Conta Permanentemente")
            .setView(dialogView)
            .setPositiveButton("Excluir") { dialog, _ ->
                val password = etPassword.text.toString()
                if (password.isEmpty()) {
                    showToast("Por favor, insira a sua senha.")
                } else {
                    setLoading(true)
                    ProfileController.reauthenticateAndDeleteAccount(password,
                        onSuccess = {
                            setLoading(false)
                            showToast("Conta excluída com sucesso.")
                            logout() // Faz logout e vai para a tela de login
                        },
                        onError = {
                            setLoading(false)
                            showToast(it) // Ex: "Senha incorreta."
                        }
                    )
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // --- Lógica de Imagem (Copiada de AddEditBookActivity) ---

    private fun showImageSourceDialog() {
        val options = arrayOf("Tirar Foto", "Escolher da Galeria")
        AlertDialog.Builder(requireContext())
            .setTitle("Alterar Foto de Perfil")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch()
                    1 -> launchGalleryPicker()
                }
            }
            .show()
    }

    private fun launchGalleryPicker() {
        pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val photoFile = File(requireContext().externalCacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
        imageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )
        takePictureLauncher.launch(imageUri)
    }

    private fun convertImageToBase64() {
        if (imageUri == null) return

        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, imageUri!!)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri!!)
            }

            // Redimensiona (fotos de perfil são mais pequenas)
            val maxWidth = 300
            val originalWidth = bitmap.width
            val originalHeight = bitmap.height
            if (originalWidth == 0) throw Exception("Largura da imagem é zero.")

            val newHeight = (originalHeight.toFloat() / originalWidth.toFloat()) * maxWidth
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight.toInt(), false)

            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()

            // Guarda a string Base64 para ser usada no 'saveProfileChanges'
            newImageBase64 = "data:image/jpeg;base64,${Base64.encodeToString(byteArray, Base64.DEFAULT)}"

        } catch (e: Exception) {
            showToast("Erro ao processar imagem: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}