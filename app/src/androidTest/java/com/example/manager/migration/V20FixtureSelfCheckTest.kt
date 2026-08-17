package com.example.manager.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.example.manager.data.db.AppDatabase
import org.json.JSONObject
import org.junit.After
import org.junit.Test

class V20FixtureSelfCheckTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val helper = MigrationTestHelper(
        instrumentation,
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @After
    fun deleteFixtureDatabase() {
        instrumentation.targetContext.deleteDatabase(V20Fixture.databaseName)
    }

    @Test
    fun fictionalV20Fixture_matchesManifest_andHasValidForeignKeys() {
        val expected = instrumentation.context.assets
            .open("v20_fixture_expected.json")
            .bufferedReader()
            .use { JSONObject(it.readText()).getJSONObject("counts") }
        val database = helper.createDatabase(V20Fixture.databaseName, 20)

        V20Fixture.insertInto(database)

        expected.keys().forEach { table ->
            MigrationAssertions.assertTableCount(database, table, expected.getInt(table))
        }
        MigrationAssertions.assertForeignKeyIntegrity(database)
        MigrationAssertions.assertScalarLong(
            database,
            "SELECT COUNT(*) FROM staff WHERE role = 'SHAREHOLDER' AND is_active = 1",
            1
        )
        MigrationAssertions.assertScalarLong(
            database,
            "SELECT COUNT(*) FROM staff WHERE is_active = 0",
            1
        )
        MigrationAssertions.assertScalarLong(
            database,
            "SELECT COUNT(*) FROM orders WHERE customer_id IS NULL",
            1
        )
    }
}
