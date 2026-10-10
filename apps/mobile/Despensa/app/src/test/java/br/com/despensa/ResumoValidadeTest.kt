package br.com.despensa

import br.com.despensa.notif.ItemVencendo
import br.com.despensa.notif.resumoValidade
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ResumoValidadeTest {

    @Test fun `0 itens nao notifica`() {
        assertNull(resumoValidade(emptyList()))
    }

    @Test fun `1 item usa singular`() {
        val r = resumoValidade(listOf(ItemVencendo("Iogurte", 1)))!!
        assertEquals("🧊 1 item vencendo", r.titulo)
        assertEquals(listOf("• Iogurte — vence amanhã"), r.linhas)
    }

    @Test fun `5 itens lista todos sem linha extra`() {
        val itens = (1..5).map { ItemVencendo("Item$it", it) }
        val r = resumoValidade(itens)!!
        assertEquals("🧊 5 itens vencendo", r.titulo)
        assertEquals(5, r.linhas.size)
    }

    @Test fun `6 itens resume com linha de outros`() {
        val itens = (1..6).map { ItemVencendo("Item$it", it) }
        val r = resumoValidade(itens)!!
        assertEquals("🧊 6 itens vencendo", r.titulo)
        assertEquals(6, r.linhas.size)
        assertEquals("+ 1 outros", r.linhas.last())
    }

    @Test fun `frases de dias cobrem vencido hoje e limite D-3`() {
        val r = resumoValidade(
            listOf(
                ItemVencendo("A", -2),
                ItemVencendo("B", 0),
                ItemVencendo("C", 3)
            )
        )!!
        assertEquals("• A — venceu há 2d", r.linhas[0])
        assertEquals("• B — vence hoje", r.linhas[1])
        assertEquals("• C — vence em 3d", r.linhas[2])
    }
}
