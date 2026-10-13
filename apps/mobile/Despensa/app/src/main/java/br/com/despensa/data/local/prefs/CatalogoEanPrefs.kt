package br.com.despensa.data.local.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.despensa.domain.normalizarCodigo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val NOME_STORE = "catalogo_ean"
private const val PREFIXO_EAN = "ean_"

private val Context.storeCatalogo by preferencesDataStore(name = NOME_STORE)

/**
 * Catálogo local EAN → nome (issue #9, offline-first, NFR-001).
 *
 * - Seed curado vive no domínio ([br.com.despensa.domain.SEMENTE_CATALOGO_EAN]);
 *   aqui mora só o APRENDIZADO: o nome que o usuário digitou/confirmou para um
 *   EAN desconhecido, lembrado na próxima leitura (AC 9c).
 * - DataStore (não Room): zero migration, zero risco destrutivo — mesma lição
 *   de `port-web-status-header` / `scan-mlkit-ean` (regra no domínio, SQL só
 *   para busca+local). Chave por EAN normalizado (só dígitos).
 * - Lookup online segue OUT (era OUT da #3: rede + privacidade).
 */
@Singleton
class CatalogoEanPrefs @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    /** Mapa EAN normalizado → nome aprendido (vazio = nada aprendido ainda). */
    val aprendidos: Flow<Map<String, String>> = ctx.storeCatalogo.data.map { prefs ->
        prefs.asMap()
            .mapNotNull { (chave, valor) ->
                val nomeChave = chave.name
                if (nomeChave.startsWith(PREFIXO_EAN) && valor is String) {
                    val ean = nomeChave.removePrefix(PREFIXO_EAN)
                    if (ean.isNotBlank() && (valor as String).isNotBlank()) {
                        ean to (valor as String)
                    } else null
                } else null
            }
            .toMap()
    }

    /** Nome aprendido para o [codigo] (bruto ou normalizado), null se desconhecido. */
    fun nomePara(codigo: String): Flow<String?> {
        val alvo = normalizarCodigo(codigo)
        val chave = stringPreferencesKey(PREFIXO_EAN + alvo)
        return ctx.storeCatalogo.data.map { it[chave]?.takeIf { nome -> nome.isNotBlank() } }
    }

    /** Aprende (ou corrige) o nome de um EAN. Ignora código vazio/nome em branco. */
    suspend fun lembrar(codigo: String, nome: String) {
        val alvo = normalizarCodigo(codigo)
        val limpo = nome.trim()
        if (alvo.isEmpty() || limpo.isEmpty()) return
        val chave = stringPreferencesKey(PREFIXO_EAN + alvo)
        ctx.storeCatalogo.edit { it[chave] = limpo }
    }
}
