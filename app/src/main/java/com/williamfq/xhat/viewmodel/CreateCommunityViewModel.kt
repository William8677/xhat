package com.williamfq.xhat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.williamfq.domain.model.Community
import com.williamfq.domain.model.Visibility
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.time.Instant

@HiltViewModel
class CreateCommunityViewModel @Inject constructor() : ViewModel() {

    companion object {
        private const val CURRENT_USER = "William8677"
        private val CURRENT_TIME = Instant.parse("2025-01-05T02:16:39Z").toEpochMilli()
    }

    private val _formFields = MutableLiveData(CreateCommunityFormState())
    val formFields: LiveData<CreateCommunityFormState> = _formFields

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    data class CreateCommunityFormState(
        var name: String = "",
        var description: String = "",
        var categories: String = "",
        var isPrivate: Boolean = false,
        var isRestricted: Boolean = false
    )

    fun updateName(name: String) {
        val current = _formFields.value ?: CreateCommunityFormState()
        current.name = name
        _formFields.value = current
        validateForm()
    }

    fun updateDescription(description: String) {
        val current = _formFields.value ?: CreateCommunityFormState()
        current.description = description
        _formFields.value = current
        validateForm()
    }

    fun updateCategories(categories: String) {
        val current = _formFields.value ?: CreateCommunityFormState()
        current.categories = categories
        _formFields.value = current
    }

    fun updatePrivate(isPrivate: Boolean) {
        val current = _formFields.value ?: CreateCommunityFormState()
        current.isPrivate = isPrivate
        if (isPrivate) {
            current.isRestricted = false
        }
        _formFields.value = current
    }

    fun updateRestricted(isRestricted: Boolean) {
        val current = _formFields.value ?: CreateCommunityFormState()
        current.isRestricted = isRestricted
        _formFields.value = current
    }

    private fun validateForm(): Boolean {
        val currentState = _formFields.value ?: return false

        return when {
            currentState.name.isBlank() -> showError("El nombre es obligatorio")
            currentState.name.length > Community.MAX_NAME_LENGTH -> showError(
                "El nombre no puede tener más de ${Community.MAX_NAME_LENGTH} caracteres"
            )
            currentState.description.length > Community.MAX_DESCRIPTION_LENGTH -> showError(
                "La descripción no puede tener más de ${Community.MAX_DESCRIPTION_LENGTH} caracteres"
            )
            else -> {
                _errorMessage.value = null
                true
            }
        }
    }

    private fun showError(message: String): Boolean {
        _errorMessage.value = message
        return false
    }

    fun createCommunity(): Community? {
        if (!validateForm()) return null

        val currentState = _formFields.value ?: return null

        return try {
            _isLoading.value = true
            Community(
                name = currentState.name,
                description = currentState.description.ifBlank { null },
                createdBy = CURRENT_USER,
                createdAt = CURRENT_TIME,
                isPrivate = currentState.isPrivate,
                categories = currentState.categories
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .take(Community.MAX_CATEGORIES),
                visibility = when {
                    currentState.isPrivate -> Visibility.PRIVATE
                    currentState.isRestricted -> Visibility.RESTRICTED
                    else -> Visibility.PUBLIC
                }
            ).also {
                _isLoading.value = false
                resetForm()
            }
        } catch (e: Exception) {
            showError(e.message ?: "Error al crear la comunidad")
            _isLoading.value = false
            null
        }
    }

    fun resetForm() {
        _formFields.value = CreateCommunityFormState()
        _errorMessage.value = null
        _isLoading.value = false
    }
}