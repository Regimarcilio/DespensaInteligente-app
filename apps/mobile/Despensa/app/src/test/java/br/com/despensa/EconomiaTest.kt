package br.com.despensa

import br.com.despensa.data.local.entity.BaixaEntity
import br.com.despensa.data.local.entity.MotivoBaixa
import br.com.despensa.domain.inicioDoMes
import br.com.despensa.domain.perdidosNaLista
import br.com.despensa.domain.proporcaoConsumida
import br.com.despensa.domain.salvosNaLista
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Calendar

class EconomiaTest {

    private fun baixa(nome: String, valor: Double, motivo: MotivoBaixa) =
        BaixaEntity(nome = nome, quantidade = 1.0, unidade = "un", valorTotal = valor, motivo = motivo)

    @Test fun `salvos soma so CONSUMIDO`() {
        val lista = listOf(
            baixa("Arroz", 9.98, MotivoBaixa.CONSUMIDO),
            baixa("Leite", 5.5, MotivoBaixa.CONSUMIDO),
            baixa("Iogurte", 7.0, MotivoBaixa.DESCARTADO)
        )
        assertEquals(15.48, salvosNaLista(lista), 0.001)
    }

    @Test fun `perdidos soma so DESCARTADO`() {
        val lista = listOf(
            baixa("Arroz", 9.98, MotivoBaixa.CONSUMIDO),
            baixa("Iogurte", 7.0, MotivoBaixa.DESCARTADO),
            baixa("Queijo", 3.25, MotivoBaixa.DESCARTADO)
        )
        assertEquals(10.25, perdidosNaLista(lista), 0.001)
    }

    @Test fun `lista vazia zera tudo`() {
        assertEquals(0.0, salvosNaLista(emptyList()), 0.0)
        assertEquals(0.0, perdidosNaLista(emptyList()), 0.0)
        assertEquals(0f, proporcaoConsumida(0.0, 0.0), 0f)
    }

    @Test fun `proporcao consumida meio a meio`() {
        assertEquals(0.5f, proporcaoConsumida(10.0, 10.0), 0.001f)
        assertEquals(1f, proporcaoConsumida(10.0, 0.0), 0.001f)
        assertEquals(0f, proporcaoConsumida(0.0, 10.0), 0.001f)
    }

    @Test fun `inicio do mes e dia 1 meia-noite`() {
        val agora = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = inicioDoMes(agora) }
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
    }
}
