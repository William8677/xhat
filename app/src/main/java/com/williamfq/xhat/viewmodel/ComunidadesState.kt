// ComunidadesState.kt (puede ir en el mismo archivo o uno nuevo dentro de tu paquete)
package com.williamfq.xhat.viewmodel

import com.williamfq.domain.model.Community

sealed class CommunitiesState {
    object Loading : CommunitiesState() // Estado de carga
    data class Success(val communities: List<Community>) : CommunitiesState() // Listado de comunidades
    data class Error(val message: String) : CommunitiesState() // Error al cargar las comunidades
}
