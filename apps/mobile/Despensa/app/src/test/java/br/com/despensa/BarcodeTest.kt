package br.com.despensa

import br.com.despensa.domain.JANELA_DEBOUNCE_SCAN_MS
import br.com.despensa.domain.SEMENTE_CATALOGO_EAN
import br.com.despensa.domain.deveMostrarDialogoCadastro
import br.com.despensa.domain.digitoEan13
import br.com.despensa.domain.ehCodigoValido
import br.com.despensa.domain.nomeSugeridoPara
import br.com.despensa.domain.normalizarCodigo
import br.com.despensa.domain.sugerirNomeCatalogo
import br.com.despensa.domain.validarEan13
import br.com.despensa.domain.validarEan8
import br.com.despensa.domain.validarUpcA
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Issues #3/#8/#9 — leitor de código de barras (AC em Gherkin nas issues).
 * Reproduz o aceite em JVM puro: EAN-13 válido pré-preenche o nome;
 * inválido/negado cai no cadastro manual (cobertura do fallback em
 * `nomeSugeridoPara` + validação que barra o salvamento do lixo).
 */
class BarcodeTest {
    // 7894900011517: GTIN-13 com dígito GS1 válido (fixo p/ regressão).
    @Test fun `ean13 valido passa`() {
        assertTrue(validarEan13("7894900011517"))
    }

    @Test fun `ean13 digito errado falha`() {
        assertFalse(validarEan13("7894900011518"))
    }

    @Test fun `ean13 tamanho errado falha`() {
        assertFalse(validarEan13("78949000115"))
        assertFalse(validarEan13("78949000115177"))
        assertFalse(validarEan13(""))
    }

    @Test fun `normalizar tira espacos e hifens`() {
        assertEquals("7894900011517", normalizarCodigo("7894 9000-1151 7"))
        assertTrue(validarEan13("7894 9000-1151 7"))
    }

    @Test fun `digito verificador confere exemplo`() {
        assertEquals(7, digitoEan13("789490001151"))
    }

    @Test fun `ean8 valido passa e invalido falha`() {
        assertTrue(validarEan8("96385074"))
        assertFalse(validarEan8("96385075"))
    }

    @Test fun `upca valido passa e invalido falha`() {
        assertTrue(validarUpcA("036000291452"))
        assertFalse(validarUpcA("036000291453"))
    }

    @Test fun `ehCodigoValido aceita formatos do varejo`() {
        assertTrue(ehCodigoValido("7894900011517"))
        assertTrue(ehCodigoValido("96385074"))
        assertTrue(ehCodigoValido("036000291452"))
        assertFalse(ehCodigoValido("123"))
        assertFalse(ehCodigoValido("abc"))
    }

    // AC1: lookup local acha o nome já cadastrado → pré-preenche com ele.
    @Test fun `lookup local sugere nome cadastrado`() {
        val nomes = listOf("Leite Integral 7894900011517", "Arroz Tio João")
        assertEquals(
            "Leite Integral 7894900011517",
            nomeSugeridoPara("7894900011517", nomes)
        )
    }

    // AC1 (código novo fora do seed): pré-preenche com o próprio código p/
    // concluir em 1 toque. (7894900011517 está no seed desde a #9 — ver teste
    // `ean valido do seed resolve nome`.)
    @Test fun `codigo desconhecido sugere o proprio codigo`() {
        assertEquals(
            "7891111111111",
            nomeSugeridoPara("7891111111111", listOf("Arroz Tio João"))
        )
        // Sem seed correspondente: EAN válido mas fora do seed cai no fallback.
        assertEquals("7891111111111", nomeSugeridoPara("7891111111111", emptyList()))
    }

    // Issue #8: diálogo oculto com o scanner aberto (preview tela cheia + mira).
    @Test fun `dialogo oculta com scanner aberto e volta limpa`() {
        // Dado "Escanear código" com diálogo aberto → scanner abre sem sobreposição.
        assertFalse(deveMostrarDialogoCadastro(dialogoAberto = true, escaneando = true))
        // Scanner em tela cheia: sem diálogo por cima.
        assertTrue(deveMostrarDialogoCadastro(dialogoAberto = true, escaneando = false))
        // Volta: fechou o scanner → diálogo de cadastro de volta, sem tela presa.
        assertFalse(deveMostrarDialogoCadastro(dialogoAberto = false, escaneando = false))
        assertFalse(deveMostrarDialogoCadastro(dialogoAberto = false, escaneando = true))
    }

    // Issue #9a: EAN-13 válido do seed resolve nome (pré-preenche código/nome).
    @Test fun `ean valido do seed resolve nome`() {
        assertTrue(ehCodigoValido("7894900011517"))
        assertEquals(
            SEMENTE_CATALOGO_EAN.getValue("7894900011517"),
            sugerirNomeCatalogo("7894900011517")
        )
    }

    // Issue #9a (timing): debounce bem abaixo do limite de 2s do AC.
    @Test fun `debounce do scan cabe nos 2s do AC`() {
        assertTrue(JANELA_DEBOUNCE_SCAN_MS <= 2000L)
    }

    // Issue #9b: EAN já cadastrado/ensinado sugere o nome anterior (apr override seed).
    @Test fun `ean aprendido sugere nome anterior mesmo com seed`() {
        val aprendidos = mapOf("7894900011517" to "Guaraná 2L (nome da casa)")
        assertEquals(
            "Guaraná 2L (nome da casa)",
            sugerirNomeCatalogo("7894900011517", emptyList(), aprendidos)
        )
    }

    // Issue #9b (memória): item do estoque que menciona o código vence o seed.
    @Test fun `nome em memoria que menciona codigo vence seed`() {
        val nomes = listOf("Meu Guaraná 7894900011517")
        assertEquals(
            "Meu Guaraná 7894900011517",
            sugerirNomeCatalogo("7894900011517", nomes, emptyMap())
        )
    }

    // Issue #9c: EAN desconhecido → digita o nome e lembra na próxima.
    @Test fun `ean desconhecido permite digitar e lembra na proxima`() {
        val desconhecido = "7891111111111"
        assertTrue(ehCodigoValido(desconhecido))
        // 1ª leitura: sem nada aprendido → fallback é o próprio código.
        assertEquals(desconhecido, sugerirNomeCatalogo(desconhecido, emptyList(), emptyMap()))
        // Usuário digita o nome e salva → vira aprendizado (DataStore em prod).
        val aposAprender = mapOf(desconhecido to "Azeite Extra Virgem 500ml")
        assertEquals(
            "Azeite Extra Virgem 500ml",
            sugerirNomeCatalogo(desconhecido, emptyList(), aposAprender)
        )
    }
}
