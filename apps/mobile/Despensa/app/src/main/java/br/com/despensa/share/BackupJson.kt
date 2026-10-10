package br.com.despensa.share

import br.com.despensa.data.local.entity.BaixaEntity
import br.com.despensa.data.local.entity.EstoqueEntity
import br.com.despensa.data.local.entity.LocalEstoque
import br.com.despensa.data.local.entity.MotivoBaixa
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.util.UUID

/**
 * Backup JSON local (issue #2, US09): export/import puros espelhando o padrão do
 * ListaCompras (`JsonCodec`): `ignoreUnknownKeys`, `schemaVersion`, validações por item.
 * Fotos ficam de fora (só `fotoPath` é persistido; o arquivo mora em `filesDir/fotos/`).
 */
object BackupJson {
    const val SCHEMA = 1
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun export(
        itens: List<EstoqueEntity>,
        baixas: List<BaixaEntity>,
        exportedAt: String
    ): String {
        val arrItens = buildJsonArray {
            itens.forEach { i ->
                add(buildJsonObject {
                    put("id", i.id)
                    put("nome", i.nome)
                    put("quantidade", i.quantidade)
                    put("unidade", i.unidade)
                    i.validade?.let { put("validade", it) }
                    put("local", i.local.name)
                    i.fotoPath?.takeIf { it.isNotBlank() }?.let { put("fotoPath", it) }
                    i.precoRef?.let { put("precoRef", it) }
                    put("createdAt", i.createdAt)
                })
            }
        }
        val arrBaixas = buildJsonArray {
            baixas.forEach { b ->
                add(buildJsonObject {
                    put("id", b.id)
                    put("nome", b.nome)
                    put("quantidade", b.quantidade)
                    put("unidade", b.unidade)
                    put("valorTotal", b.valorTotal)
                    put("motivo", b.motivo.name)
                    put("ts", b.ts)
                })
            }
        }
        return json.encodeToString(
            JsonObject.serializer(),
            buildJsonObject {
                put("app", "Despensa")
                put("schemaVersion", SCHEMA)
                put("exportedAt", exportedAt)
                put("itens", arrItens)
                put("baixas", arrBaixas)
            }
        )
    }

    data class ImportResult(
        val itens: List<EstoqueEntity>,
        val baixas: List<BaixaEntity>,
        val erros: List<String>
    )

    fun import(texto: String): ImportResult {
        val erros = mutableListOf<String>()
        val root = runCatching { json.parseToJsonElement(texto).jsonObject }.getOrElse {
            return ImportResult(emptyList(), emptyList(), listOf("JSON inválido: ${it.message}"))
        }
        val v = root["schemaVersion"]?.jsonPrimitive?.intOrNull ?: 0
        if (v > SCHEMA) {
            return ImportResult(
                emptyList(), emptyList(),
                listOf("schemaVersion $v maior que suportado ($SCHEMA)")
            )
        }
        val itens = mutableListOf<EstoqueEntity>()
        root["itens"]?.jsonArray?.forEachIndexed { ii, el ->
            try {
                val o = el.jsonObject
                val nome = o["nome"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                    ?: throw IllegalArgumentException("nome ausente")
                val qtd = o["quantidade"]?.jsonPrimitive?.doubleOrNull ?: 1.0
                if (qtd <= 0) throw IllegalArgumentException("quantidade deve ser > 0")
                val preco = o["precoRef"]?.jsonPrimitive?.doubleOrNull
                if (preco != null && preco < 0) throw IllegalArgumentException("preço negativo")
                val validade = o["validade"]?.jsonPrimitive?.longOrNull
                if (validade != null && validade < 0) throw IllegalArgumentException("validade negativa")
                val local = runCatching {
                    LocalEstoque.valueOf(o["local"]?.jsonPrimitive?.content ?: "DESPENSA")
                }.getOrDefault(LocalEstoque.DESPENSA)
                itens.add(
                    EstoqueEntity(
                        id = o["id"]?.jsonPrimitive?.contentOrNull ?: UUID.randomUUID().toString(),
                        nome = nome,
                        quantidade = qtd,
                        unidade = o["unidade"]?.jsonPrimitive?.contentOrNull ?: "un",
                        validade = validade,
                        local = local,
                        fotoPath = o["fotoPath"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() },
                        precoRef = preco,
                        createdAt = o["createdAt"]?.jsonPrimitive?.longOrNull ?: System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                erros.add("itens[$ii]: ${e.message}")
            }
        }
        val baixas = mutableListOf<BaixaEntity>()
        root["baixas"]?.jsonArray?.forEachIndexed { bi, el ->
            try {
                val o = el.jsonObject
                val nome = o["nome"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                    ?: throw IllegalArgumentException("nome ausente")
                val qtd = o["quantidade"]?.jsonPrimitive?.doubleOrNull ?: 1.0
                if (qtd <= 0) throw IllegalArgumentException("quantidade deve ser > 0")
                val total = o["valorTotal"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                if (total < 0) throw IllegalArgumentException("valorTotal negativo")
                val motivo = runCatching {
                    MotivoBaixa.valueOf(o["motivo"]?.jsonPrimitive?.content ?: "CONSUMIDO")
                }.getOrDefault(MotivoBaixa.CONSUMIDO)
                baixas.add(
                    BaixaEntity(
                        id = o["id"]?.jsonPrimitive?.contentOrNull ?: UUID.randomUUID().toString(),
                        nome = nome,
                        quantidade = qtd,
                        unidade = o["unidade"]?.jsonPrimitive?.contentOrNull ?: "un",
                        valorTotal = total,
                        motivo = motivo,
                        ts = o["ts"]?.jsonPrimitive?.longOrNull ?: System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                erros.add("baixas[$bi]: ${e.message}")
            }
        }
        return ImportResult(itens, baixas, erros)
    }
}
