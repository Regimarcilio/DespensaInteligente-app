package br.com.despensa

import br.com.despensa.presentation.lista.OrigemSugestao
import br.com.despensa.presentation.lista.Sugestao
import br.com.despensa.presentation.lista.descricaoSugestao
import br.com.despensa.presentation.lista.emojiSugestao
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Issue #19 (bloco compacto padrão 8.png): emoji do thumb sem foto +
 * descrição TalkBack. Puras — sem Android/Room.
 */
class ListaBlocoTest {

    @Test
    fun emojiPorPalavraChave() {
        assertEquals("🧈", emojiSugestao("Manteiga com Sal"))
        assertEquals("🧀", emojiSugestao("Queijo Muçarela"))
        assertEquals("🥛", emojiSugestao("Leite integral"))
        assertEquals("🍚", emojiSugestao("Arroz branco"))
        assertEquals("☕", emojiSugestao("Café torrado"))
    }

    @Test
    fun emojiGenericoSemMatch() {
        assertEquals("🛒", emojiSugestao("Xarope multiuso"))
        assertEquals("🛒", emojiSugestao(""))
    }

    @Test
    fun descricaoComPrecoEMarcacao() {
        val s = Sugestao("Queijo Muçarela", 1.0, "pct", 16.8, OrigemSugestao.CONSUMO, marcada = true)
        assertEquals("Queijo Muçarela, 1 pct, R$ 16,80, marcado para consumo", descricaoSugestao(s))
    }

    @Test
    fun descricaoSemPrecoNaoMarcado() {
        val s = Sugestao("Manteiga com Sal", 1.0, "pote", null, OrigemSugestao.VALIDADE, marcada = false)
        assertEquals("Manteiga com Sal, 1 pote, não marcado", descricaoSugestao(s))
    }
}
