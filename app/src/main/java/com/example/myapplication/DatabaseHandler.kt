package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

// Ensure these are top-level constants or inside a companion object if preferred
// But given their usage, having them globally accessible as you've done is fine
val DATABASE_NAME = "Itechs_Final_Project"
val TABLE_USERS = "Users"
val COL_USER_ID = "id"
val COL_USERNAME = "username"
val COL_EMAIL = "email"
val COL_PASSWORD = "password"

// Constants for Diary Entries Table
val TABLE_MOOD_ENTRIES = "MoodEntries"
val COL_ENTRY_ID = "entry_id"
val COL_USER_ID_FK = "user_id_fk"
val COL_ENTRY_DATE = "entry_date"
val COL_ENTRY_TITLE = "entry_title"
val COL_ENTRY_CONTENT = "entry_content"

class DatabaseHandler (var context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 2) { // Version is 2

    override fun onCreate(db: SQLiteDatabase?) {

        // Create Users Table
        val createUserTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_USERNAME + " TEXT UNIQUE," +
                COL_EMAIL + " TEXT UNIQUE," +
                COL_PASSWORD + " TEXT)"
        db?.execSQL(createUserTable)

        // Create Mood Entries Table
        val createMoodEntriesTable = "CREATE TABLE " + TABLE_MOOD_ENTRIES + " (" +
                COL_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_USER_ID_FK + " INTEGER," +
                COL_ENTRY_DATE + " TEXT NOT NULL," +
                COL_ENTRY_TITLE + " TEXT NOT NULL," +
                COL_ENTRY_CONTENT + " TEXT," +
                "FOREIGN KEY(" + COL_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")" +
                ")"
        db?.execSQL(createMoodEntriesTable)

        addDefaultUser(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Drop tables in reverse order of creation (mood entries first, then users)
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_MOOD_ENTRIES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db) // Recreate all tables
    }

    // --- User related functions ---

    fun insertData(user: User): Long {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_USERNAME, user.username)
        cv.put(COL_EMAIL, user.email)
        cv.put(COL_PASSWORD, user.password) // Uses 'password' property from User data class

        val result = db.insert(TABLE_USERS, null, cv)
        db.close()

        if (result == -1.toLong()) {
            Toast.makeText(context, "Registration Failed! Username or Email might already exist.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Registration Success!", Toast.LENGTH_SHORT).show()
        }
        return result
    }

    fun checkUserCredentials(usernameOrEmail: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE ($COL_USERNAME = ? OR $COL_EMAIL = ?) AND $COL_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(usernameOrEmail, usernameOrEmail, password))
        val isValid = cursor.count > 0
        cursor.close()
        db.close()
        return isValid
    }

    fun getUserDetails(usernameOrEmail: String, password: String): Pair<String, String>? {
        val db = this.readableDatabase
        val query = "SELECT $COL_USERNAME, $COL_EMAIL FROM $TABLE_USERS WHERE ($COL_USERNAME = ? OR $COL_EMAIL = ?) AND $COL_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(usernameOrEmail, usernameOrEmail, password))
        var userDetails: Pair<String, String>? = null

        if (cursor.moveToFirst()) {
            val usernameIndex = cursor.getColumnIndex(COL_USERNAME)
            val emailIndex = cursor.getColumnIndex(COL_EMAIL)
            if (usernameIndex != -1 && emailIndex != -1) {
                val username = cursor.getString(usernameIndex)
                val email = cursor.getString(emailIndex)
                userDetails = Pair(username, email)
            }
        }
        cursor.close()
        db.close()
        return userDetails
    }

    fun checkUserExists(username: String, email: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COL_USERNAME = ? OR $COL_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(username, email))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun updateUserPassword(email: String, newPassword: String): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_PASSWORD, newPassword)

        val rowsAffected = db.update(TABLE_USERS, cv, "$COL_EMAIL = ?", arrayOf(email))
        db.close()
        return rowsAffected > 0
    }

    fun getUserId(username: String): Int {
        val db = this.readableDatabase
        val query = "SELECT $COL_USER_ID FROM $TABLE_USERS WHERE $COL_USERNAME = ?"
        val cursor = db.rawQuery(query, arrayOf(username))
        var userId = -1

        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(COL_USER_ID)
            if (idIndex != -1) {
                userId = cursor.getInt(idIndex)
            }
        }
        cursor.close()
        db.close()
        return userId
    }

    private fun addDefaultUser(db: SQLiteDatabase?) {
        val countQuery = "SELECT COUNT(*) FROM $TABLE_USERS"
        val cursor = db?.rawQuery(countQuery, null)
        cursor?.moveToFirst()
        val count = cursor?.getInt(0)
        cursor?.close()

        if (count == 0) {
            val defaultUsername = "testuser"
            val defaultEmail = "test@example.com"
            val defaultPassword = "password123"

            val cv = ContentValues()
            cv.put(COL_USERNAME, defaultUsername)
            cv.put(COL_EMAIL, defaultEmail)
            cv.put(COL_PASSWORD, defaultPassword)

            db?.insert(TABLE_USERS, null, cv)
        }
    }

    // --- Mood Entry related functions ---

    fun insertMoodEntry(entry: MoodEntry): Long {
        val db = this.writableDatabase
        val cv = ContentValues().apply {
            put(COL_USER_ID_FK, entry.userId)
            put(COL_ENTRY_DATE, entry.date)
            put(COL_ENTRY_TITLE, entry.title)
            put(COL_ENTRY_CONTENT, entry.content)
        }
        val result = db.insert(TABLE_MOOD_ENTRIES, null, cv)
        db.close()
        return result
    }

    // NEW: Function to get a single Mood Entry by its ID
    fun getMoodEntry(entryId: Long): MoodEntry? {
        val db = this.readableDatabase
        var entry: MoodEntry? = null
        val selectQuery = "SELECT * FROM $TABLE_MOOD_ENTRIES WHERE $COL_ENTRY_ID = ?"
        val cursor = db.rawQuery(selectQuery, arrayOf(entryId.toString()))

        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ENTRY_ID))
            val userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID_FK))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COL_ENTRY_DATE))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COL_ENTRY_TITLE))
            val content = cursor.getString(cursor.getColumnIndexOrThrow(COL_ENTRY_CONTENT))
            entry = MoodEntry(id, userId, date, title, content)
        }
        cursor.close()
        db.close()
        return entry
    }

    fun updateMoodEntry(entry: MoodEntry): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues().apply {
            put(COL_USER_ID_FK, entry.userId)
            put(COL_ENTRY_DATE, entry.date)
            put(COL_ENTRY_TITLE, entry.title)
            put(COL_ENTRY_CONTENT, entry.content)
        }
        val rowsAffected = db.update(TABLE_MOOD_ENTRIES, cv, "$COL_ENTRY_ID = ?", arrayOf(entry.id.toString()))
        db.close()
        return rowsAffected > 0
    }

    fun deleteMoodEntry(entryId: Long): Boolean {
        val db = this.writableDatabase
        val rowsAffected = db.delete(TABLE_MOOD_ENTRIES, "$COL_ENTRY_ID = ?", arrayOf(entryId.toString()))
        db.close()
        return rowsAffected > 0
    }

    fun getMoodEntriesForUserAndDate(userId: Int, date: String): List<MoodEntry> {
        val entryList = mutableListOf<MoodEntry>()
        val db = this.readableDatabase
        // Use the correct column names (COL_USER_ID_FK, COL_ENTRY_DATE, COL_ENTRY_ID)
        val selectQuery = "SELECT * FROM $TABLE_MOOD_ENTRIES WHERE $COL_USER_ID_FK = ? AND $COL_ENTRY_DATE = ? ORDER BY $COL_ENTRY_ID DESC"
        val cursor = db.rawQuery(selectQuery, arrayOf(userId.toString(), date))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ENTRY_ID))
                val retrievedUserId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID_FK)) // Use COL_USER_ID_FK
                val entryDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_ENTRY_DATE))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(COL_ENTRY_TITLE))
                val content = cursor.getString(cursor.getColumnIndexOrThrow(COL_ENTRY_CONTENT))

                val entry = MoodEntry(id, retrievedUserId, entryDate, title, content)
                entryList.add(entry)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return entryList
    }

    fun getMoodEntriesForUser(userId: Int): List<MoodEntry> {
        val entries = mutableListOf<MoodEntry>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_MOOD_ENTRIES WHERE $COL_USER_ID_FK = ? ORDER BY $COL_ENTRY_DATE DESC, $COL_ENTRY_ID DESC"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex(COL_ENTRY_ID)
                val userIdIndex = cursor.getColumnIndex(COL_USER_ID_FK)
                val dateIndex = cursor.getColumnIndex(COL_ENTRY_DATE)
                val titleIndex = cursor.getColumnIndex(COL_ENTRY_TITLE)
                val contentIndex = cursor.getColumnIndex(COL_ENTRY_CONTENT)

                if (idIndex != -1 && userIdIndex != -1 && dateIndex != -1 && titleIndex != -1 && contentIndex != -1) {
                    val id = cursor.getLong(idIndex)
                    val retrievedUserId = cursor.getInt(userIdIndex)
                    val retrievedDate = cursor.getString(dateIndex)
                    val title = cursor.getString(titleIndex)
                    val content = cursor.getString(contentIndex)
                    entries.add(MoodEntry(id, retrievedUserId, retrievedDate, title, content))
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return entries
    }
}