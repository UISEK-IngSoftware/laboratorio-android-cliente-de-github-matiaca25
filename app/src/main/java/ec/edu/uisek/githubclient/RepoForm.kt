package ec.edu.uisek.githubclient

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityRepoFormBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import ec.edu.uisek.githubclient.services.GithubApiService
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoForm : AppCompatActivity() {
    private lateinit var binding: ActivityRepoFormBinding
    private var repo: Repo? = null
    private val apiService: GithubApiService by lazy {
        RetrofitClient.gitHubApiService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRepoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = intent.getParcelableExtra("repo")

        if (repo != null) {
            binding.repoNameInput.setText(repo!!.name)
            binding.repoDescriptionInput.setText(repo!!.description)
            binding.saveButton.text = "Actualizar"
            binding.saveButton.setOnClickListener { updateRepo() }
        } else {
            binding.saveButton.setOnClickListener { createRepo() }
        }

        binding.cancelButton.setOnClickListener { finish() }
    }

    private fun validationForm(): Boolean {
        val repoName = binding.repoNameInput.text.toString()
        if (repoName.isBlank()) {
            binding.repoNameInput.error = "El nombre del repositorio no puede contener espacios"
            return false
        }
        binding.repoNameInput.error = null
        return true
    }

    private fun createRepo() {
        if (!validationForm()) return

        val repoRequest = RepoRequest(
            binding.repoNameInput.text.toString().trim(),
            binding.repoDescriptionInput.text.toString().trim()
        )

        apiService.addRepo(repoRequest).enqueue(object : Callback<Repo> {
            override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio creado exitosamente")
                    finish()
                } else {
                    handleApiError(response.code())
                }
            }

            override fun onFailure(call: Call<Repo>, t: Throwable) {
                Log.d("RepoForm", "Error al crear el repositorio", t)
                showMessage("Error al crear el repositorio")
            }
        })
    }

    private fun updateRepo() {
        if (!validationForm()) return

        val repoRequest = RepoRequest(
            binding.repoNameInput.text.toString().trim(),
            binding.repoDescriptionInput.text.toString().trim()
        )

        apiService.updateRepo(repo!!.owner.login, repo!!.name, repoRequest).enqueue(object : Callback<Repo> {
            override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio actualizado exitosamente")
                    finish()
                } else {
                    handleApiError(response.code())
                }
            }

            override fun onFailure(call: Call<Repo>, t: Throwable) {
                Log.d("RepoForm", "Error al actualizar el repositorio", t)
                showMessage("Error al actualizar el repositorio")
            }
        })
    }

    private fun handleApiError(code: Int) {
        val errorMessage = when (code) {
            401 -> "No autorizado"
            403 -> "Prohibido"
            404 -> "No encontrado"
            else -> "Error $code"
        }
        showMessage("Error: $errorMessage")
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}