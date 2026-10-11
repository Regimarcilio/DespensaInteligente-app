package br.com.despensa.domain

/**
 * Leitor de código de barras — camada de domínio pura (issue #3, S5 pós-MVP).
 *
 * Regras sem Android/Room, 100% testáveis em JVM:
 * - [normalizarCodigo]: tira espaços, hífens e prefixos; mantém só dígitos.
 * - [validarEan13]: EAN-13 com dígito verificador módulo-10 (GS1).
 * - [ehCodigoValido]: aceita o que o ML Kit entrega no varejo BR
 *   (EAN-13, EAN-8 e UPC-A com dígito válido).
 * - [nomeSugeridoPara]: lookup LOCAL primeiro (offline-first, sem conta):
 *   procura nos nomes já cadastrados (em memória, sem `@Query` nova nem
 *   migration — lição do Aprendiz `port-web-status-header`) quem menciona o
 *   código; desconhecido → sugere o próprio código p/ o usuário completar.
 *
 * OUT explícito da issue (NÃO implementar aqui): busca online de produtos,
 * comparação de preços, sync nuvem.
 */

/** Só dígitos do que o scanner/UI entregou (`"7894 9000-1151 7"` → `"7894900011517"`). */
fun normalizarCodigo(bruto: String): String =
    bruto.filter { it.isDigit() }

/** Dígito verificador EAN-13 (módulo-10 GS1) para os 12 primeiros dígitos. */
fun digitoEan13(dozeDigitos: String): Int {
    require(dozeDigitos.length == 12 && dozeDigitos.all { it.isDigit() }) {
        "EAN-13 exige 12 dígitos para o cálculo"
    }
    var soma = 0
    dozeDigitos.forEachIndexed { i, c ->
        val d = c.digitToInt()
        soma += if (i % 2 == 0) d else d * 3
    }
    return (10 - (soma % 10)) % 10
}

/** EAN-13 válido: 13 dígitos + verificador GS1 confere. */
fun validarEan13(codigo: String): Boolean {
    val digitos = normalizarCodigo(codigo)
    if (digitos.length != 13) return false
    return runCatching {
        digitoEan13(digitos.substring(0, 12)) == digitos[12].digitToInt()
    }.getOrDefault(false)
}

/** EAN-8 válido: 8 dígitos + verificador (mesma regra, pesos invertidos). */
fun validarEan8(codigo: String): Boolean {
    val digitos = normalizarCodigo(codigo)
    if (digitos.length != 8) return false
    var soma = 0
    digitos.substring(0, 7).forEachIndexed { i, c ->
        val d = c.digitToInt()
        soma += if (i % 2 == 0) d * 3 else d
    }
    val esperado = (10 - (soma % 10)) % 10
    return runCatching { esperado == digitos[7].digitToInt() }.getOrDefault(false)
}

/** UPC-A válido: 12 dígitos; equivale ao EAN-13 com `0` à esquerda. */
fun validarUpcA(codigo: String): Boolean {
    val digitos = normalizarCodigo(codigo)
    if (digitos.length != 12) return false
    return validarEan13("0$digitos")
}

/** Aceito pelo scanner no varejo BR: EAN-13, EAN-8 ou UPC-A com dígito válido. */
fun ehCodigoValido(codigo: String): Boolean {
    val digitos = normalizarCodigo(codigo)
    return when (digitos.length) {
        13 -> validarEan13(digitos)
        8 -> validarEan8(digitos)
        12 -> validarUpcA(digitos)
        else -> false
    }
}

/**
 * Lookup local-first: devolve o nome já cadastrado que menciona o [codigo]
 * (comparação exata primeiro, depois `contains`, case-insensitive); se nenhum
 * item menciona o código, devolve o próprio código para pré-preencher o campo
 * nome do cadastro (o usuário completa em 1 toque — AC1).
 *
 * @param codigo código já normalizado/validado do scanner.
 * @param nomesExistentes nomes dos itens em memória (vindos do ViewModel).
 */
fun nomeSugeridoPara(codigo: String, nomesExistentes: List<String>): String {
    val alvo = normalizarCodigo(codigo)
    if (alvo.isEmpty()) return codigo
    nomesExistentes.firstOrNull {
        normalizarCodigo(it) == alvo
    }?.let { return it }
    nomesExistentes.firstOrNull {
        it.contains(codigo.trim(), ignoreCase = true) || it.contains(alvo, ignoreCase = true)
    }?.let { return it }
    return codigo.trim()
}
