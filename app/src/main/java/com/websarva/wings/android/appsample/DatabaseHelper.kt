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
        val sb_sections = StringBuilder()
        sb_sections.append("CREATE TABLE sections (")
        sb_sections.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,")
        sb_sections.append("section_name TEXT,")
        sb_sections.append("stamp_qty INTEGER")
        sb_sections.append(");")
        val sql_sections = sb_sections.toString()
        db.execSQL(sql_sections)

        val sb_stamps = StringBuilder()
        sb_stamps.append("CREATE TABLE stamps (")
        sb_stamps.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,")
        sb_stamps.append("latitude REAL,")
        sb_stamps.append("longitude REAL,")
        sb_stamps.append("section_id INT")
        sb_stamps.append(");")
        val sql_stamps = sb_stamps.toString()
        db.execSQL(sql_stamps)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}