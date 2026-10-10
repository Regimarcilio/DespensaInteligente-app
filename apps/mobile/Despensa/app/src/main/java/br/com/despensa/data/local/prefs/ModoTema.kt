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
            SISTEMA -> "Seguir o Sistema"
            CLARO -> "Modo Claro"
            ESCURO -> "Modo Noturno"
        }

    val descricao: String
        get() = when (this) {
            SISTEMA -> "Adapta automaticamente ao tema do seu dispositivo Android."
            CLARO -> "Cores quentes e acolhedoras com fundo areia suave."
            ESCURO -> "Fundo escuro profundo para economizar bateria OLED."
        }
}
