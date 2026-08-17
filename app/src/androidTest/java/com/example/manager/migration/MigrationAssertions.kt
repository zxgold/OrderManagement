package com.example.manager.migration

import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse

/** Shared assertions for V20 fixture validation and every later explicit Room migration test. */
object MigrationAssertions {
    fun assertTableCount(database: SupportSQLiteDatabase, table: String, expected: Int) {
        val actual = database.query("SELECT COUNT(*) FROM `$table`").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
        assertEquals("Unexpected row count in $table", expected, actual)
    }

    fun assertForeignKeyIntegrity(database: SupportSQLiteDatabase) {
        database.query("PRAGMA foreign_key_check").use { cursor ->
            assertFalse("Foreign-key violation: ${cursor.columnNames.joinToString()}", cursor.moveToFirst())
        }
    }

    fun assertScalarLong(database: SupportSQLiteDatabase, query: String, expected: Long) {
        val actual = database.query(query).use { cursor ->
            cursor.moveToFirst()
            cursor.getLong(0)
        }
        assertEquals("Unexpected scalar for $query", expected, actual)
    }
}
