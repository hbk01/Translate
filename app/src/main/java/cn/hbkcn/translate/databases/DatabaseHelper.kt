package cn.hbkcn.translate.databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

const val version = 1
const val name = "Translate"
val tables = HashMap<String, String>()

class DatabaseHelper(c: Context) : SQLiteOpenHelper(c, name, null, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        tables["TranslateHistory"] = """
            CREATE TABLE IF NOT EXISTS TranslateHistory (
                time INTEGER PRIMARY KEY NOT NULL,
                json TEXT NOT NULL
            );
        """
        tables.onEach { entry -> db?.execSQL(entry.value) }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("onUpgrade")
    }

    fun insertHistory(json: String) {
        val values = with(ContentValues()) {
            put("json", json)
            put("time", System.currentTimeMillis())
            this
        }
        writableDatabase.insert("TranslateHistory", "", values)
    }

    /**
     * Query database to
     * @param limit default is 10, it's set result limit.
     * @return HashMap<Int, String> Int is insert time, String is insert json
     */
    fun selectHistory(limit: Int = 10): LinkedHashMap<Long, String> {
        val map = LinkedHashMap<Long, String>()
        val query = "SELECT * FROM TranslateHistory ORDER BY time DESC LIMIT $limit;"
        val cursor = readableDatabase.rawQuery(query, emptyArray())
        while (cursor.moveToNext()) {
            val time = cursor.getLong(cursor.getColumnIndex("time"))
            val json = cursor.getString(cursor.getColumnIndex("json"))
            map[time] = json
        }
        cursor.close()
        return map
    }

    fun selectLastHistory(): String {
        val sql = "SELECT * FROM TranslateHistory ORDER BY time DESC;"
        val cursor = readableDatabase.rawQuery(sql, emptyArray())
        cursor.moveToFirst()
        val json = cursor.getString(cursor.getColumnIndex("json"))
        cursor.close()
        return json
    }
}











