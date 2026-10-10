package br.com.despensa

import br.com.despensa.domain.valorTotalBaixa
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BaixaCalculoTest {

    @Test fun `qtd x preco`() {
        assertEquals(19.96, valorTotalBaixa(4.0, 4.99), 0.001)
    }

    @Test fun `sem preco zera sem crash`() {
        assertEquals(0.0, valorTotalBaixa(4.0, null), 0.0)
    }

    @Test fun `preco zero ou negativo zera`() {
        assertEquals(0.0, valorTotalBaixa(2.0, 0.0), 0.0)
        assertEquals(0.0, valorTotalBaixa(2.0, -1.5), 0.0)
    }

    @Test fun `quantidade zerada zera`() {
        assertEquals(0.0, valorTotalBaixa(0.0, 9.99), 0.0)
    }

    @Test fun `quantidade fracionada`() {
        assertEquals(4.995, valorTotalBaixa(0.5, 9.99), 0.001)
    }
}
