# Reddit — DespensaInteligente (copiar e colar)

## 1. r/SideProject — post standalone (qualquer dia)
**Title:** I built an open-source pantry app that shows me how much food money I throw away (in R$)

**Body:**
Hey! I kept throwing food away without noticing — expired yogurt, forgotten lettuce. So I built **DespensaInteligente** (PT-BR, Android, 100% open-source, offline-first, no accounts, no tracking).

What it does:
- Pantry inventory with expiry dates, quantity and storage location (fridge/freezer/pantry), optional photo
- Traffic-light freshness (FRESH/SOON/EXPIRED) + local notifications (3 days, 1 day, day-of)
- One-swipe consume/discard, and it auto-generates the next shopping list in a text format my other app ([ListaCompras](https://github.com/Regimarcilio/Projeto_ListaCompras)) imports straight from WhatsApp
- A "money saved vs wasted" screen — the hook that got my family actually using it

Stack: Kotlin + Jetpack Compose + Room (migrations tested), 45 unit tests green, dark/light with a theme picker, TalkBack from day one.

Repo (+ debug APK in releases): https://github.com/Regimarcilio/DespensaInteligente

I'd love feedback — especially on the "two offline apps talking via shareable text" idea. Stars/issues/PRs welcome!

## 2. r/androiddev — SÓ no fio "Saturday Showcase" (comentário, aos sábados)
> **DespensaInteligente — offline-first pantry/expiry tracker (Kotlin, Compose, Room)** 🧊🇧🇷
> Tracks pantry with expiry dates, local notifications, one-swipe consume/discard, auto-generates a shopping list my other app imports from plain text (no backend involved), plus a "money saved vs wasted" screen. 45 unit tests, dark/light, TalkBack, backup via JSON. Fully offline, no accounts.
> https://github.com/Regimarcilio/DespensaInteligente — feedback welcome!

## Regras de sobrevivência (não ser removido)
- Conta com algum karma antes (comente em outros posts 1–2 dias antes).
- r/androiddev: NUNCA post standalone de app — só no megathread de sábado.
- Responda TODO comentário nas primeiras 6h (o algoritmo + a comunidade recompensam).
- Não poste o mesmo texto em 5 subs no mesmo dia (filtro anti-spam).
