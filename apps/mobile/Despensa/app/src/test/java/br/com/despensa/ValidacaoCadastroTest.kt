package br.com.despensa

import br.com.despensa.domain.validarCadastro
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ValidacaoCadastroTest {
    @Test fun `nome vazio bloqueia`() {
        assertEquals("Nome obrigatório", validarCadastro("", 1.0))
    }

    @Test fun `nome so espacos bloqueia`() {
        assertEquals("Nome obrigatório", validarCadastro("   ", 1.0))
    }

    @Test fun `qtd zero bloqueia`() {
        assertEquals("Quantidade deve ser > 0", validarCadastro("Iogurte", 0.0))
    }

    @Test fun `qtd negativa bloqueia`() {
        assertEquals("Quantidade deve ser > 0", validarCadastro("Iogurte", -2.0))
    }

    @Test fun `qtd nula bloqueia`() {
        assertEquals("Quantidade deve ser > 0", validarCadastro("Iogurte", null))
    }

    @Test fun `valido passa`() {
        assertNull(validarCadastro("Iogurte", 4.0))
    }
}
