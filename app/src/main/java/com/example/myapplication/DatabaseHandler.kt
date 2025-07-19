package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

val DATABASE_NAME = "Itechs_Final_Project"
val TABLE_NAME = "Users"
val COL_ID = "id"
val COL_USERNAME = "username"
val COL_EMAIL = "email"
val COL_PASSWORD = "password"

class DatabaseHandler (var context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {

        val createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_USERNAME + " TEXT UNIQUE," +
                COL_EMAIL + " TEXT UNIQUE," +
                COL_PASSWORD + " TEXT)"

        db?.execSQL(createTable)

        addDefaultUser(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
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

        if (result == -1.toLong()) {
            Toast.makeText(context, "Registration Failed! Username or Email might already exist.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Registration Success!", Toast.LENGTH_SHORT).show()
        }
        return result
    }

    fun checkUserCredentials(usernameOrEmail: String, password: String): Boolean {
        // This function is still useful for simple boolean check, but getUserDetails is more specific.
        // The check in SignIn.kt now relies on getUserDetails.
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE ($COL_USERNAME = ? OR $COL_EMAIL = ?) AND $COL_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(usernameOrEmail, usernameOrEmail, password))
        val isValid = cursor.count > 0
        cursor.close()
        db.close()
        return isValid
    }

    // --- NEW FUNCTION: Get User Details if credentials are valid ---
    fun getUserDetails(usernameOrEmail: String, password: String): Pair<String, String>? {
        val db = this.readableDatabase
        val query = "SELECT $COL_USERNAME, $COL_EMAIL FROM $TABLE_NAME WHERE ($COL_USERNAME = ? OR $COL_EMAIL = ?) AND $COL_PASSWORD = ?"
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
            val defaultUsername = "testuser"
            val defaultEmail = "test@example.com"
            val defaultPassword = "password123"

            val cv = ContentValues()
            cv.put(COL_USERNAME, defaultUsername)
            cv.put(COL_EMAIL, defaultEmail)
            cv.put(COL_PASSWORD, defaultPassword)

            val result = db?.insert(TABLE_NAME, null, cv)
            if (result != -1.toLong()) {
                Toast.makeText(context, "Default user created: $defaultUsername", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to create default user.", Toast.LENGTH_LONG).show()
            }
        }
    }
}