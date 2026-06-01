package com.smaarig.nlinktagger.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.smaarig.nlinktagger.data.local.entities.LinkEntity
import com.smaarig.nlinktagger.data.local.entities.LinkTagCrossRef
import com.smaarig.nlinktagger.data.local.entities.TagEntity

@Database(
    entities = [TagEntity::class, LinkEntity::class, LinkTagCrossRef::class],
    version = 2,
    exportSchema = false
)
abstract class NLinkDatabase : RoomDatabase() {
    abstract fun nlinkDao(): NLinkDao

    companion object {
        @Volatile
        private var INSTANCE: NLinkDatabase? = null

        fun getDatabase(context: Context): NLinkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NLinkDatabase::class.java,
                    "nlink_database"
                )
                .fallbackToDestructiveMigration() // Since we changed the schema
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
