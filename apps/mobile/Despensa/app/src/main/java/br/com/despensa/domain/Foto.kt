package br.com.despensa.domain

/** Teto da foto comprimida (issue #1, US02): WebP ≤ 200 KB. */
const val LIMITE_FOTO_BYTES: Long = 200L * 1024L

/**
 * Lógica de decisão pura (testável em JVM): a foto precisa ser reduzida?
 *
 * @param tamanhoBytes tamanho atual do arquivo em bytes.
 * @param limiteBytes teto aceito (default [LIMITE_FOTO_BYTES]).
 */
fun precisaReduzir(tamanhoBytes: Long, limiteBytes: Long = LIMITE_FOTO_BYTES): Boolean =
    tamanhoBytes > limiteBytes
