package com.williamfq.data.dao

import androidx.room.*
import com.williamfq.data.entities.UserEntity

@Dao
interface UserDao {

    /**
     * Inserta un nuevo usuario en la tabla.
     * Si ya existe, lo reemplaza.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    /**
     * Inserta múltiples usuarios en la tabla.
     * Si ya existen, los reemplaza.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    /**
     * Actualiza los datos de un usuario existente.
     */
    @Update
    suspend fun updateUser(user: UserEntity)

    /**
     * Elimina un usuario específico.
     */
    @Delete
    suspend fun deleteUser(user: UserEntity)

    /**
     * Obtiene todos los usuarios de la tabla con todas las columnas.
     */
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    /**
     * Obtiene un usuario por su ID con todas las columnas.
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?

    /**
     * Busca usuarios por nombre de usuario.
     */
    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%'")
    suspend fun searchUsersByUsername(query: String): List<UserEntity>

    /**
     * Obtiene el número total de usuarios en la tabla.
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    /**
     * Elimina todos los usuarios de la tabla.
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
