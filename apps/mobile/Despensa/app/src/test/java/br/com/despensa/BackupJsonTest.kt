package br.com.despensa

import br.com.despensa.data.local.entity.BaixaEntity
import br.com.despensa.data.local.entity.EstoqueEntity
import br.com.despensa.data.local.entity.LocalEstoque
import br.com.despensa.data.local.entity.MotivoBaixa
import br.com.despensa.share.BackupJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Issue #2: round-trip export→import preserva tudo; inválidos viram erro por item. */
class BackupJsonTest {

    private fun item() = EstoqueEntity(
        id = "i1", nome = "Arroz", quantidade = 2.0, unidade = "kg",
        validade = 1_800_000_000_000L, local = LocalEstoque.DESPENSA,
        fotoPath = "/x/foto.webp", precoRef = 5.99, createdAt = 1_700_000_000_000L
    )

    private fun baixa() = BaixaEntity(
        id = "b1", nome = "Arroz", quantidade = 1.0, unidade = "kg",
        valorTotal = 5.99, motivo = MotivoBaixa.CONSUMIDO, ts = 1_700_000_100_000L
    )

    @Test fun `round-trip preserva itens e baixas sem erros`() {
        val json = BackupJson.export(listOf(item()), listOf(baixa()), "2026-10-09T00:00:00Z")
        val res = BackupJson.import(json)
        assertTrue(res.erros.isEmpty(), "erros: ${res.erros}")
        assertEquals(listOf(item()), res.itens)
        assertEquals(listOf(baixa()), res.baixas)
    }

    @Test fun `round-trip com nulos opcionais`() {
        val semOpcionais = item().copy(validade = null, fotoPath = null, precoRef = null)
        val res = BackupJson.import(BackupJson.export(listOf(semOpcionais), emptyList(), "t"))
        assertTrue(res.erros.isEmpty(), "erros: ${res.erros}")
        assertEquals(listOf(semOpcionais), res.itens)
        assertTrue(res.baixas.isEmpty())
    }

    @Test fun `item invalido vira erro e nao derruba os validos`() {
        val json = """
            {"app":"Despensa","schemaVersion":1,"exportedAt":"t",
             "itens":[
               {"id":"ok","nome":"Feijão","quantidade":1.0,"unidade":"kg","local":"DESPENSA","createdAt":1},
               {"id":"s-nome","quantidade":1.0,"unidade":"un","local":"DESPENSA"},
               {"id":"qtd-zero","nome":"Ruim","quantidade":0,"unidade":"un","local":"DESPENSA"},
               {"id":"preco-neg","nome":"Ruim2","quantidade":1.0,"unidade":"un","local":"DESPENSA","precoRef":-1.0}
             ],
             "baixas":[
               {"id":"b-ok","nome":"Leite","quantidade":1.0,"unidade":"L","valorTotal":4.5,"motivo":"DESCARTADO","ts":2},
               {"id":"b-ruim","quantidade":1.0,"unidade":"L","valorTotal":0.0,"motivo":"CONSUMIDO","ts":2}
             ]}
        """.trimIndent()
        val res = BackupJson.import(json)
        assertEquals(1, res.itens.size)
        assertEquals("Feijão", res.itens.first().nome)
        assertEquals(1, res.baixas.size)
        assertEquals(4, res.erros.size)
        assertTrue(res.erros.any { it.startsWith("itens[1]") })
        assertTrue(res.erros.any { it.startsWith("baixas[1]") })
    }

    @Test fun `schema futuro rejeitado`() {
        val res = BackupJson.import("""{"schemaVersion":99,"itens":[],"baixas":[]}""")
        assertTrue(res.itens.isEmpty())
        assertTrue(res.erros.any { it.contains("schemaVersion 99") })
    }

    @Test fun `json invalido rejeitado`() {
        val res = BackupJson.import("não é json {")
        assertTrue(res.itens.isEmpty())
        assertTrue(res.erros.any { it.startsWith("JSON inválido") })
    }

    @Test fun `chaves desconhecidas ignoradas (compat futura)`() {
        val json = BackupJson.export(listOf(item()), emptyList(), "t")
            .replace("\"nome\":\"Arroz\"", "\"nome\":\"Arroz\",\"campoFuturo\":123")
        val res = BackupJson.import(json)
        assertTrue(res.erros.isEmpty(), "erros: ${res.erros}")
        assertEquals("Arroz", res.itens.single().nome)
    }
}
