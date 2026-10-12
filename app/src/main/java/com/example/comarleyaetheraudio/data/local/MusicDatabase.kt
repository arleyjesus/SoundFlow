package com.example.comarleyaetheraudio.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.comarleyaetheraudio.data.local.dao.PlaylistDao
import com.example.comarleyaetheraudio.data.local.entity.FavoriteSongEntity
import com.example.comarleyaetheraudio.data.local.entity.FolderEntity
import com.example.comarleyaetheraudio.data.local.entity.PlaylistEntity
import com.example.comarleyaetheraudio.data.local.entity.PlaylistSongCrossRef
import com.example.comarleyaetheraudio.data.local.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        FolderEntity::class,
        FavoriteSongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getDatabase(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        // Una version o identidad incompatible debe conservar los datos y
        // rechazar la apertura hasta disponer de una migracion verificada.
        // Los tests usan esta misma configuracion con un nombre aislado.
        internal fun buildDatabase(
            context: Context,
            name: String = "soundflow_database"
        ): MusicDatabase = Room.databaseBuilder(
            context.applicationContext,
            MusicDatabase::class.java,
            name
        ).build()
    }
}
