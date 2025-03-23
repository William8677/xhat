package com.williamfq.data.dao

import androidx.room.*
import com.williamfq.data.entities.Poll
import com.williamfq.data.entities.PollComment
import com.williamfq.data.entities.PollReward

@Dao
interface PollDao {

    /**
     * Inserta una nueva encuesta en la base de datos.
     * Si ya existe una encuesta con el mismo ID, la reemplaza.
     *
     * @param poll La entidad Poll a insertar.
     * @return El ID de la encuesta insertada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoll(poll: Poll): Long

    /**
     * Obtiene una encuesta específica por su ID.
     *
     * @param pollId El ID de la encuesta a buscar.
     * @return La encuesta encontrada o null si no existe.
     */
    @Query("SELECT * FROM polls WHERE id = :pollId")
    suspend fun getPollById(pollId: Int): Poll?

    /**
     * Elimina una encuesta específica por su ID.
     *
     * @param pollId El ID de la encuesta a eliminar.
     */
    @Query("DELETE FROM polls WHERE id = :pollId")
    suspend fun deletePollById(pollId: Int)

    /**
     * Inserta un comentario asociado a una encuesta.
     *
     * @param comment La entidad PollComment a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: PollComment)

    /**
     * Obtiene todos los comentarios asociados a una encuesta específica.
     *
     * @param pollId El ID de la encuesta.
     * @return Lista de comentarios asociados a la encuesta.
     */
    @Query("SELECT * FROM poll_comments WHERE pollid = :pollId")
    suspend fun getCommentsByPollId(pollId: Int): List<PollComment>

    /**
     * Elimina todos los comentarios asociados a una encuesta específica.
     *
     * @param pollId El ID de la encuesta.
     */
    @Query("DELETE FROM poll_comments WHERE pollid = :pollId")
    suspend fun deleteCommentsByPollId(pollId: Int)

    /**
     * Obtiene el número total de comentarios para una encuesta específica.
     *
     * @param pollId El ID de la encuesta.
     * @return Número de comentarios asociados.
     */
    @Query("SELECT COUNT(*) FROM poll_comments WHERE pollid = :pollId")
    suspend fun getCommentCountByPollId(pollId: Int): Int

    /**
     * Inserta una recompensa asociada a una encuesta.
     *
     * @param reward La entidad PollReward a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: PollReward)

    /**
     * Obtiene todas las recompensas asociadas a una encuesta específica.
     *
     * @param pollId El ID de la encuesta.
     * @return Lista de recompensas asociadas a la encuesta.
     */
    @Query("SELECT * FROM poll_rewards WHERE pollid = :pollId")
    suspend fun getRewardsByPollId(pollId: Int): List<PollReward>

    /**
     * Elimina todas las recompensas asociadas a una encuesta específica.
     *
     * @param pollId El ID de la encuesta.
     */
    @Query("DELETE FROM poll_rewards WHERE pollid = :pollId")
    suspend fun deleteRewardsByPollId(pollId: Int)

    /**
     * Obtiene el número total de recompensas para una encuesta específica.
     *
     * @param pollId El ID de la encuesta.
     * @return Número de recompensas asociadas.
     */
    @Query("SELECT COUNT(*) FROM poll_rewards WHERE pollid = :pollId")
    suspend fun getRewardCountByPollId(pollId: Int): Int
}
