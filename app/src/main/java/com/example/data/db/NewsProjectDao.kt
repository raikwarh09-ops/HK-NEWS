package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.NewsProject
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsProjectDao {
    @Query("SELECT * FROM news_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<NewsProject>>

    @Query("SELECT * FROM news_projects WHERE id = :id")
    suspend fun getProjectById(id: Long): NewsProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: NewsProject): Long

    @Update
    suspend fun updateProject(project: NewsProject)

    @Delete
    suspend fun deleteProject(project: NewsProject)

    @Query("DELETE FROM news_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)
}
