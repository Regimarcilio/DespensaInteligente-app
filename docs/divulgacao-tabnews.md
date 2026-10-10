# Post TabNews — DespensaInteligente (copiar e colar)

**Título:** Criei um app open-source que me mostra quanto jogo de comida fora (em R$)

**Corpo:**

Pessoal, quero compartilhar um projeto que nasceu de uma dor bem doméstica: todo mês eu jogava comida fora sem perceber — iogurte vencido, alface esquecida no fundo da geladeira. Resolvi transformar isso num app Android open-source: a **DespensaInteligente** 🧊

## O que ele faz

- 📦 Guarda a despensa com **validade, quantidade e local** (geladeira/freezer/despensa), com foto opcional
- 🚦 **Semáforo de validade** (FRESCO/ATENÇÃO/VENCEU) + notificações locais 3 dias, 1 dia e no dia
- 👆 Baixa em 1 gesto (consumir/descartar)
- 🛒 Gera a **lista de reposição sozinha** num formato que meu outro app ([ListaCompras](https://github.com/Regimarcilio/Projeto_ListaCompras)) importa direto
- 💰 Tela de **desperdício em R$**: salvos vs perdidos no mês — o gancho que me fez usar todo dia

## Stack e decisões

- 100% **offline-first**: Kotlin + Compose + Room, sem conta, sem rastreio, sem backend
- Dark/light com seletor, TalkBack desde o dia 1, backup JSON local
- Banco com migrations versionadas + MigrationTest; 45 testes unitários verdes
- Identidade própria dark/light documentada no repo (`docs/07-*.md`)

## O que aprendi

1. Notificação local resolve 90% do caso sem precisar de servidor nenhum (WorkManager resolve).
2. Formato de texto como "API" entre dois apps offline é subestimado: colar/compartilhar integra sem backend.
3. Escrever a identidade antes do código economizou uns 3 refactors visuais (e mesmo assim refizemos 3x 😅).

## Links

- Repositório: https://github.com/Regimarcilio/DespensaInteligente
- APK debug da v0.8 na raiz do repo (instala direto)
- Aceito feedback, issues e PRs — quem quiser parear (`Co-authored-by`), bora!

Se curtir, deixa uma ⭐ que ajuda demais. Valeu!
