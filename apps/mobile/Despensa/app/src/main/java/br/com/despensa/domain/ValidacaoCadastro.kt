package br.com.despensa.domain

/** Validação do cadastro (US01). Retorna mensagem de erro pt-BR ou null quando válido. */
fun validarCadastro(nome: String, quantidade: Double?): String? {
    if (nome.isBlank()) return "Nome obrigatório"
    if (quantidade == null || quantidade <= 0) return "Quantidade deve ser > 0"
    return null
}
