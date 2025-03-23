package com.williamfq.data.local.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.williamfq.data.converters.*
import com.williamfq.data.dao.*
import com.williamfq.data.entities.*

@Database(
    entities = [
        MessageEntity::class,
        StoryEntity::class,
        CommunityEntity::class,
        Notification::class,
        UserEntity::class,
        Settings::class,
        Reaction::class,
        Channel::class,
        CallHistory::class,
        Media::class,
        PanicAlertEntity::class,
        Session::class,
        WalkieTalkieAudioEntity::class,
        ContactEntity::class,
        LocationEntity::class,
        LocationHistoryEntity::class,
        ChatEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(
    DateConverter::class,
    ListConverter::class,
    PanicAlertConverters::class,
    ChatMessageConverters::class,
    StoryConverters::class
)
abstract class XhatDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun storyDao(): StoryDao
    abstract fun userDao(): UserDao
    abstract fun communityDao(): CommunityDao
    abstract fun notificationDao(): NotificationDao
    abstract fun settingsDao(): SettingsDao
    abstract fun reactionDao(): ReactionDao
    abstract fun channelDao(): ChannelDao
    abstract fun callHistoryDao(): CallHistoryDao
    abstract fun mediaDao(): MediaDao
    abstract fun panicDao(): PanicDao
    abstract fun sessionDao(): SessionDao
    abstract fun chatDao(): ChatDao
    abstract fun walkieTalkieAudioDao(): WalkieTalkieAudioDao
    abstract fun contactDao(): ContactDao
    abstract fun locationDao(): LocationDao

    companion object {
        const val DATABASE_NAME = "xhat_database"

        @Volatile
        private var INSTANCE: XhatDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS panic_alerts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        message TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        latitude REAL,
                        longitude REAL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS call_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        callType TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        duration INTEGER NOT NULL,
                        isMissed INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS media (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        messageId TEXT NOT NULL,
                        mediaType TEXT NOT NULL,
                        filePath TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS walkie_talkie_audio (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        chatId TEXT NOT NULL,
                        audioData BLOB NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS messages_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        message_id TEXT NOT NULL,
                        chat_id TEXT NOT NULL,
                        sender_id TEXT NOT NULL,
                        recipient_id TEXT NOT NULL,
                        message_content TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        is_read INTEGER NOT NULL DEFAULT 0,
                        is_sent INTEGER NOT NULL DEFAULT 1,
                        is_deleted INTEGER NOT NULL DEFAULT 0,
                        message_type TEXT NOT NULL,
                        message_status TEXT NOT NULL,
                        message_attachments TEXT NOT NULL DEFAULT '[]',
                        message_extra_data TEXT NOT NULL DEFAULT '{}',
                        message_mentions TEXT NOT NULL DEFAULT '[]',
                        message_reply_to TEXT,
                        message_room_id TEXT,
                        message_username TEXT,
                        message_auto_destruct_at INTEGER,
                        message_is_edited INTEGER NOT NULL DEFAULT 0,
                        message_edited_at INTEGER,
                        message_deletion_type TEXT NOT NULL DEFAULT 'NONE',
                        message_is_media_message INTEGER NOT NULL DEFAULT 0,
                        message_can_be_edited INTEGER NOT NULL DEFAULT 1,
                        message_attachment_urls TEXT NOT NULL DEFAULT '[]'
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT OR IGNORE INTO messages_new (
                        id, message_id, chat_id, sender_id, recipient_id, 
                        message_content, timestamp, is_read, is_sent, is_deleted,
                        message_type, message_status
                    )
                    SELECT 
                        id, messageId, chatId, senderId, recipientId,
                        messageContent, timestamp, isRead, isSent, isDeleted,
                        messageType, messageStatus
                    FROM messages
                """.trimIndent())

                db.execSQL("DROP TABLE IF EXISTS messages")
                db.execSQL("ALTER TABLE messages_new RENAME TO messages")

                db.execSQL("CREATE INDEX IF NOT EXISTS index_messages_chat_id ON messages(chat_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_messages_sender_id ON messages(sender_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_messages_timestamp ON messages(timestamp)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS contacts (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        phone_number TEXT,
                        email TEXT,
                        photo_url TEXT,
                        frequency INTEGER NOT NULL DEFAULT 0,
                        last_interaction INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS locations (
                        user_id TEXT PRIMARY KEY NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS location_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        user_id TEXT NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("CREATE INDEX IF NOT EXISTS index_contacts_frequency ON contacts(frequency)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_location_history_user_id ON location_history(user_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_location_history_timestamp ON location_history(timestamp)")
            }
        }

        fun getInstance(context: Context): XhatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    XhatDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigrationFrom(1)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}