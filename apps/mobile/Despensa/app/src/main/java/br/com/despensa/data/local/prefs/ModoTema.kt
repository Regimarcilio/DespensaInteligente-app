package br.com.despensa.data.local.prefs

/**
 * Modo de tema do app (§07-paleta-v4 b).
 * Persistido em DataStore (key "modo_tema").
 * Issue #22: a UI oferece SOMENTE Claro + Noturno (nomes mantidos).
 * SISTEMA segue no enum só como valor legado para migrar prefs antigas
 * ([migrarModoTema]) — nunca mais é oferecido nem persistido.
 */
enum class ModoTema {
    @Deprecated("Legado #22: migrar para ESCURO via migrarModoTema; fora da UI.")
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

/** Opções visíveis no diálogo Aparência (issue #22: só as 2 atuais). */
val MODOS_TEMA_VISIVEIS: List<ModoTema> = listOf(ModoTema.CLARO, ModoTema.ESCURO)

/**
 * Issue #22 (pura, testável em JVM): migração segura de quem estava em
 * "Seguir o Sistema" → Modo Noturno, sem crash e sem tela sem tema.
 * Valores ausentes/corrompidos também caem em ESCURO (default).
 */
fun migrarModoTema(modo: ModoTema): ModoTema = when (modo) {
    ModoTema.SISTEMA -> ModoTema.ESCURO
    ModoTema.CLARO -> ModoTema.CLARO
    ModoTema.ESCURO -> ModoTema.ESCURO
}
