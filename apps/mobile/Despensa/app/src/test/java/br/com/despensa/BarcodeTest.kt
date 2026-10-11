package br.com.despensa

import br.com.despensa.domain.digitoEan13
import br.com.despensa.domain.ehCodigoValido
import br.com.despensa.domain.nomeSugeridoPara
import br.com.despensa.domain.normalizarCodigo
import br.com.despensa.domain.validarEan13
import br.com.despensa.domain.validarEan8
import br.com.despensa.domain.validarUpcA
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Issue #3 — leitor de código de barras (AC em Gherkin na issue).
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

    // AC1 (código novo): pré-preenche com o próprio código p/ concluir em 1 toque.
    @Test fun `codigo desconhecido sugere o proprio codigo`() {
        assertEquals(
            "7894900011517",
            nomeSugeridoPara("7894900011517", listOf("Arroz Tio João"))
        )
        assertEquals("7894900011517", nomeSugeridoPara("7894900011517", emptyList()))
    }
}
