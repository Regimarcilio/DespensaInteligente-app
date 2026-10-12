package br.com.despensa

import br.com.despensa.data.local.prefs.MODOS_TEMA_VISIVEIS
import br.com.despensa.data.local.prefs.ModoTema
import br.com.despensa.data.local.prefs.migrarModoTema
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Issue #22 (só Modo Claro + Modo Noturno).
 * Migração segura de quem estava em "Seguir o Sistema" → Noturno,
 * sem crash e sem tela sem tema. Puro — sem DataStore/Android.
 */
class ModoTemaMigrationTest {

    @Test
    fun `legado sistema migra para noturno`() {
        assertEquals(ModoTema.ESCURO, migrarModoTema(ModoTema.SISTEMA))
    }

    @Test
    fun `claro e noturno passam intactos`() {
        assertEquals(ModoTema.CLARO, migrarModoTema(ModoTema.CLARO))
        assertEquals(ModoTema.ESCURO, migrarModoTema(ModoTema.ESCURO))
    }

    @Test
    fun `dialogo so oferece claro e noturno com nomes atuais`() {
        assertEquals(listOf(ModoTema.CLARO, ModoTema.ESCURO), MODOS_TEMA_VISIVEIS)
        assertEquals("Modo Claro", ModoTema.CLARO.rotulo)
        assertEquals("Modo Noturno", ModoTema.ESCURO.rotulo)
    }

    @Test
    fun `migracao nunca devolve sistema`() {
        ModoTema.values().forEach {
            assertTrue(migrarModoTema(it) in MODOS_TEMA_VISIVEIS)
        }
    }
}
