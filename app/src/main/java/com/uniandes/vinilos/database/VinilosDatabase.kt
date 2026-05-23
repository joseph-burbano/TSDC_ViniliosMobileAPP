package com.uniandes.vinilos.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.uniandes.vinilos.database.dao.AlbumDao
import com.uniandes.vinilos.database.dao.CollectorDao
import com.uniandes.vinilos.database.dao.PerformerDao
import com.uniandes.vinilos.database.dao.PrizeDao
import com.uniandes.vinilos.database.entities.AlbumEntity
import com.uniandes.vinilos.database.entities.CollectorEntity
import com.uniandes.vinilos.database.entities.PerformerEntity
import com.uniandes.vinilos.database.entities.PrizeEntity

@Database(
    entities = [AlbumEntity::class, PerformerEntity::class, CollectorEntity::class, PrizeEntity::class],
    version = 5
)
@TypeConverters(Converters::class)
abstract class VinilosDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
    abstract fun performerDao(): PerformerDao
    abstract fun collectorDao(): CollectorDao
    abstract fun prizeDao(): PrizeDao

    companion object {
        @Volatile
        private var INSTANCE: VinilosDatabase? = null

        fun getDatabase(context: Context): VinilosDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    VinilosDatabase::class.java,
                    "vinilos_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE performers ADD COLUMN albums TEXT NOT NULL DEFAULT '[]'"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS collectors (
                id INTEGER PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                telephone TEXT NOT NULL DEFAULT '',
                email TEXT NOT NULL DEFAULT '',
                birthDate TEXT,
                description TEXT NOT NULL DEFAULT '',
                image TEXT NOT NULL DEFAULT '',
                collectorAlbums TEXT NOT NULL DEFAULT '[]',
                favoritePerformers TEXT NOT NULL DEFAULT '[]'
            )"""
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS prizes (
                id INTEGER PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                organization TEXT NOT NULL DEFAULT ''
            )"""
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE performers ADD COLUMN type TEXT")
    }
}
