package br.com.despensa.data.local.prefs

/**
 * Modo de tema do app (§07-paleta-v4 b).
 * Persistido em DataStore (key "modo_tema"), default SISTEMA (comportamento pré-v4).
 */
enum class ModoTema {
    SISTEMA,
    CLARO,
    ESCURO;

    val rotulo: String
        get() = when (this) {
            SISTEMA -> "Sistema"
            CLARO -> "Claro"
            ESCURO -> "Escuro"
        }

    val descricao: String
        get() = when (this) {
            SISTEMA -> "Segue o celular"
            CLARO -> "Sempre maré clara"
            ESCURO -> "Sempre noite e-commerce"
        }
}
