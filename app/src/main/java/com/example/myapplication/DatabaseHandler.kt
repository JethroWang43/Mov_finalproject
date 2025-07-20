package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

// Database constants
val DATABASE_NAME = "Itechs_Final_Project"
val TABLE_NAME = "Users"
val COL_ID = "id"
val COL_USERNAME = "username"
val COL_EMAIL = "email"
val COL_PASSWORD = "password"

val TABLE_MNAME = "MoodEntries"
val COL_MID = "id"
val COL_TITLE = "title"
val COL_CONTENT = "content"
val COL_USER_ID = "user_id"

// Main DatabaseHandler class
class DatabaseHandler(var context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createUserTable = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USERNAME TEXT UNIQUE,
                $COL_EMAIL TEXT UNIQUE,
                $COL_PASSWORD TEXT
            )
        """.trimIndent()

        val createMoodTable = """
            CREATE TABLE $TABLE_MNAME (
                $COL_MID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT,
                $COL_CONTENT TEXT,
                $COL_USER_ID INTEGER,
                FOREIGN KEY($COL_USER_ID) REFERENCES $TABLE_NAME($COL_ID)
            )
        """.trimIndent()

        db?.execSQL(createUserTable)
        db?.execSQL(createMoodTable)
        addDefaultUser(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_MNAME")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertData(user: User): Long {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_USERNAME, user.username)
        cv.put(COL_EMAIL, user.email)
        cv.put(COL_PASSWORD, user.password)

        val result = db.insert(TABLE_NAME, null, cv)
        db.close()

        if (result == -1L) {
            Toast.makeText(context, "Registration Failed! Username or Email might already exist.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Registration Success!", Toast.LENGTH_SHORT).show()
        }
        return result
    }

    fun checkUserCredentials(usernameOrEmail: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE ($COL_USERNAME = ? OR $COL_EMAIL = ?) AND $COL_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(usernameOrEmail, usernameOrEmail, password))
        val isValid = cursor.count > 0
        cursor.close()
        db.close()
        return isValid
    }

    fun getUserDetails(usernameOrEmail: String, password: String): Pair<String, String>? {
        val db = this.readableDatabase
        val query = "SELECT $COL_USERNAME, $COL_EMAIL FROM $TABLE_NAME WHERE ($COL_USERNAME = ? OR $COL_EMAIL = ?) AND $COL_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(usernameOrEmail, usernameOrEmail, password))
        var userDetails: Pair<String, String>? = null

        if (cursor.moveToFirst()) {
            val username = cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL))
            userDetails = Pair(username, email)
        }
        cursor.close()
        db.close()
        return userDetails
    }

    fun checkUserExists(username: String, email: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COL_USERNAME = ? OR $COL_EMAIL = ?"
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

        val rowsAffected = db.update(TABLE_NAME, cv, "$COL_EMAIL = ?", arrayOf(email))
        db.close()
        return rowsAffected > 0
    }

    private fun addDefaultUser(db: SQLiteDatabase?) {
        val countQuery = "SELECT COUNT(*) FROM $TABLE_NAME"
        val cursor = db?.rawQuery(countQuery, null)
        cursor?.moveToFirst()
        val count = cursor?.getInt(0)
        cursor?.close()

        if (count == 0) {
            val cv = ContentValues().apply {
                put(COL_USERNAME, "testuser")
                put(COL_EMAIL, "test@example.com")
                put(COL_PASSWORD, "password123")
            }
            db?.insert(TABLE_NAME, null, cv)
        }
    }

    // --- Mood Functions ---

    fun insertMood(mood: MoodEntry): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues().apply {
            put(COL_TITLE, mood.title)
            put(COL_CONTENT, mood.content)
            put(COL_USER_ID, mood.userId)
        }
        val result = db.insert(TABLE_MNAME, null, cv)
        db.close()
        return result != -1L
    }

    fun getAllMoodsByUser(userId: Int): List<MoodEntry> {
        val moods = mutableListOf<MoodEntry>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_MNAME WHERE $COL_USER_ID = ?",
            arrayOf(userId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val mood = MoodEntry(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_MID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                    content = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID))
                )
                moods.add(mood)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return moods
    }

    fun updateMood(mood: MoodEntry): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues().apply {
            put(COL_TITLE, mood.title)
            put(COL_CONTENT, mood.content)
        }
        val rows = db.update(TABLE_MNAME, cv, "$COL_MID = ?", arrayOf(mood.id.toString()))
        db.close()
        return rows > 0
    }

    fun deleteMoodById(moodId: Int): Boolean {
        val db = this.writableDatabase
        val rowsDeleted = db.delete(TABLE_MNAME, "$COL_MID = ?", arrayOf(moodId.toString()))
        db.close()
        return rowsDeleted > 0
    }

    fun deleteMoodsByUser(userId: Int): Int {
        val db = this.writableDatabase
        val rowsDeleted = db.delete(TABLE_MNAME, "$COL_USER_ID = ?", arrayOf(userId.toString()))
        db.close()
        return rowsDeleted
    }
}
