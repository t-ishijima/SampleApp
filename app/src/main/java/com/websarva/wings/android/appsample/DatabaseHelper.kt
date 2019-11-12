package com.websarva.wings.android.appsample

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.StringBuilder

class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "location.db"
        private const val DATABASE_VERSION =1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val sb = StringBuilder()
        sb.append("CREATE TABLE sections (")
        sb.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,")
        sb.append("section_name TEXT,")
        sb.append("stamp_qty INTEGER")
        sb.append(");")
        sb.append("CREATE TABLE stamps (")
        sb.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,")
        sb.append("latitude REAL,")
        sb.append("longitude REAL,")
        sb.append("section_id INT")
        sb.append(");")
        val sql = sb.toString()

        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}