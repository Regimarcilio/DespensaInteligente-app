package br.com.despensa

import br.com.despensa.domain.LIMITE_FOTO_BYTES
import br.com.despensa.domain.precisaReduzir
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Issue #1: só a lógica de decisão é unitária (JVM). A compressão real usa
 * `BitmapFactory` + `compress(WEBP_LOSSY)` em `FotoStore` (Android — sem teste JVM).
 */
class FotoTest {

    @Test fun `abaixo do teto nao reduz`() {
        assertFalse(precisaReduzir(LIMITE_FOTO_BYTES - 1))
    }

    @Test fun `no teto exato nao reduz`() {
        assertFalse(precisaReduzir(LIMITE_FOTO_BYTES))
    }

    @Test fun `acima do teto reduz`() {
        assertTrue(precisaReduzir(LIMITE_FOTO_BYTES + 1))
    }

    @Test fun `foto de camera (3MB) reduz`() {
        assertTrue(precisaReduzir(3L * 1024L * 1024L))
    }

    @Test fun `limite customizado respeitado`() {
        assertFalse(precisaReduzir(100, 100))
        assertTrue(precisaReduzir(101, 100))
    }
}
