package br.com.despensa

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.despensa.data.local.AppDb
import br.com.despensa.data.local.MIGRATION_1_2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Issue #4 — valida a migração v1→v2 sem perda (precoRef nullable + tabela baixa).
 * Compila aqui; RODA EM APARELHO/EMULADOR: `./gradlew :app:connectedDebugAndroidTest`.
 * Schemas versionados vêm de `app/schemas` (via `androidTest.assets.srcDirs`).
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDb::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migra1_para2_preservaEstoque_eCriaBaixa() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                "INSERT INTO estoque (id, nome, quantidade, unidade, validade, local, fotoPath, createdAt) " +
                    "VALUES ('e1', 'Arroz', 2.0, 'kg', NULL, 'DESPENSA', NULL, 1000)"
            )
            execSQL(
                "INSERT INTO estoque (id, nome, quantidade, unidade, validade, local, fotoPath, createdAt) " +
                    "VALUES ('e2', 'Leite', 1.0, 'L', 1800000000000, 'GELADEIRA', '/f/foto.webp', 2000)"
            )
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2).apply {
            // Dados preservados + coluna nova nullable.
            query("SELECT nome, quantidade, unidade, validade, local, fotoPath, precoRef FROM estoque WHERE id='e1'").use {
                assertTrue(it.moveToFirst())
                assertEquals("Arroz", it.getString(0))
                assertEquals(2.0, it.getDouble(1), 0.0)
                assertEquals("kg", it.getString(2))
                assertTrue(it.isNull(3))
                assertEquals("DESPENSA", it.getString(4))
                assertTrue(it.isNull(5))
                assertTrue(it.isNull(6))
            }
            query("SELECT nome, validade, local, fotoPath FROM estoque WHERE id='e2'").use {
                assertTrue(it.moveToFirst())
                assertEquals("Leite", it.getString(0))
                assertEquals(1800000000000L, it.getLong(1))
                assertEquals("GELADEIRA", it.getString(2))
                assertEquals("/f/foto.webp", it.getString(3))
            }
            // Tabela baixa criada pela migração.
            query("SELECT name FROM sqlite_master WHERE type='table' AND name='baixa'").use {
                assertTrue(it.moveToFirst())
            }
            close()
        }
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
