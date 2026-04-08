# TaskApp.IA

Aplicativo Android para gestão de tarefas com inteligência local, foco em produtividade e coleta de dados comportamentais — 100% offline usando Room Database.

## Visão geral

O projeto foi desenvolvido em Kotlin com Jetpack Compose e arquitetura em camadas (UI → ViewModel → Repository → Room).  
O app funciona de forma local/offline, armazenando todos os dados no dispositivo.

---

## Funcionalidades

### Fase 1 — Coleta de Dados ✅

#### Feature 1: Cronômetro de Foco (Time Tracking)
- Timer integrado na tela de edição de tarefa (contagem progressiva HH:MM:SS).
- Acumula `actualMinutes` no banco ao parar o timer.
- Campo de tempo estimado (`estimatedMinutes`) para comparação futura.

#### Feature 2: Rastreador de Adiamento (Snooze Tracker)
- Detecta automaticamente quando o prazo de uma tarefa é movido para o futuro.
- Incrementa `postponedCount` a cada adiamento.
- Tarefas adiadas 3+ vezes exibem ícone de alerta visual (🔔) no card.

#### Feature 3: Micro-Feedback de Energia
- Ao marcar uma tarefa como concluída, abre um BottomSheet perguntando o nível de esforço.
- 3 opções: 😊 Leve (1), 😐 Médio (2), 😓 Exaustivo (3).
- Salva `energyLevel` e `completedAt` para análise de padrões.

#### Feature 4: Sistema de Tags/Hashtags
- Digitar `#tag` no título da tarefa cria tags automaticamente.
- Tags exibidas como chips verdes no card da tarefa.
- Filtro por tag na lista de tarefas e no dashboard.

### Fase 2 — Modo Pomodoro Nativo ✅

#### Feature 5: Tela de Pomodoro
- Tela dedicada com campo de atividade e timer de contagem regressiva.
- 2 presets: Clássico (25min foco / 5min pausa) e Longo (50min foco / 10min pausa).
- Controle de ciclos: foco → pausa → foco (com contagem de pomodoros concluídos).
- Botão "Finalizar Sessão" salva automaticamente no Room como tarefa concluída com `isDone = true`, `actualMinutes`, `completedAt` e tag `#pomodoro`.
- Timer implementado com coroutines (`viewModelScope`) — sem memory leaks.

### Fase 3 — Smart Dashboard ✅

#### Feature 6: Dashboard Inteligente
- **Header Contextual**: Saudação que muda conforme o horário do dia. Heurística de energia analisa 30 dias de dados para sugerir tarefas quando o usuário tem alta energia.
- **Mini-Métricas do Dia**: 3 cards — 🍅 Pomodoros concluídos, ⏱ Tempo total focado, ✅ Tarefas concluídas hoje.
- **Lista Anti-Procrastinação**: Tarefas com `postponedCount > 2` aparecem em destaque sob "Merecem sua Atenção 🚨" com borda vermelha/laranja.
- **Tarefas do Dia**: Lista filtrada por tags com chips interativos.
- **FAB Expansível**: Menu com 2 opções — "Nova Tarefa" e "Foco Rápido (Pomodoro)".

### Fase 4 — Preferências de Rotina e Notificações Inteligentes ✅

#### Feature 7: Preferências de Rotina (DataStore)
- **Horário de Planejamento**: define a hora da notificação diária de briefing matinal.
- **Janela de Silêncio (Quiet Hours)**: intervalo de horas em que notificações são suprimidas — suporta janelas que cruzam a meia-noite.
- **Pico de Foco**: enum `MORNING`, `AFTERNOON`, `EVENING` ou `CUSTOM` com horário personalizado.
- Persistência via Jetpack DataStore Preferences com `Flow` reativo.

#### Feature 8: Tela de Configurações de Rotina
- Tela dedicada acessível via ícone de ⚙️ no Dashboard.
- 3 seções com cards estilizados (tema escuro):
  - **Horário de Planejamento** — TimePicker.
  - **Janela de Silêncio** — TimePickers para início e fim.
  - **Pico de Foco** — FilterChips (Manhã / Tarde / Noite / Personalizado).

#### Feature 9: Agendamento Inteligente de Alarmes
- `AlarmScheduler` agenda alarmes exatos diários via `AlarmManager.setExactAndAllowWhileIdle`.
- `BootReceiver` reagenda o alarme automaticamente após reinício do dispositivo.
- O alarme é reagendado sempre que o horário de planejamento é alterado nas configurações.

#### Feature 10: Notificações com Quiet Hours
- `NotificationHelper` centraliza criação e envio de notificações.
- Guard de Quiet Hours: verifica `RoutinePreferences` antes de disparar qualquer notificação.
- **Morning Briefing**: notificação contextual em português com contagem de tarefas pendentes, atrasadas e do dia — consulta o Room em tempo real.

---

### Tarefas (CRUD base)
- Criar tarefa com título, descrição e prioridade (Baixa/Média/Alta/Urgente).
- Editar e excluir tarefa.
- Marcar tarefa como concluída (com fluxo de feedback de energia).
- Definir prazo (data e hora) opcional.
- Definir intervalo de evento (início e fim) com cálculo visual de duração.
- Validações de data/hora (sem datas no passado ao criar; fim > início).

### Lista de tarefas
- Filtros por status: Ativas, Todas, Concluídas, Atrasadas.
- Filtro por tags (chips interativos).
- Botão de acesso rápido ao Pomodoro na top bar.
- Estado vazio amigável quando não há itens.

### Dashboard
- Cards de resumo: Total, Pendentes, Concluídas, Atrasadas.
- Barra de progresso de conclusão.
- Gráfico de pizza com distribuição por prioridade.
- Seção de tarefas atrasadas.
- Atalhos dos cards para abrir a lista já filtrada.

### Calendário
- Navegação por mês (anterior/próximo).
- Destaque do dia selecionado e dia atual.
- Indicador visual nos dias com tarefas.
- Lista de tarefas do dia selecionado.
- Mensagem especial de descanso em finais de semana sem tarefas.

### Navegação e UX
- Navegação por barra inferior: Dashboard, Tarefas e Calendário.
- Fluxo de criação/edição por tela dedicada.
- Tela de Pomodoro acessível via Dashboard (FAB) e Lista de Tarefas (top bar).
- Tema escuro com componentes customizados (glassmorphism, gradientes e animações).

---

## Arquitetura e tecnologias

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Kotlin 2.0 |
| Build | Android Gradle Plugin 8.13 |
| UI | Jetpack Compose + Material 3 |
| Navegação | Navigation Compose |
| DI | Hilt |
| Banco de Dados | Room (SQLite) — com migrations 1→2→3 |
| Preferências | Jetpack DataStore Preferences |
| Notificações | AlarmManager + NotificationCompat |
| Async | Kotlin Coroutines + StateFlow |
| Gráficos | MPAndroidChart |

## Estrutura do projeto

```
TaskApp.IA/
├── app/src/main/java/com/example/myapplication/
│   ├── data/
│   │   ├── local/
│   │   │   ├── dao/            # TaskDao — queries Room (CRUD + insights)
│   │   │   ├── database/       # AppDatabase, Converters, Migrations
│   │   │   ├── entity/         # TaskEntity (com campos de ML)
│   │   │   └── preferences/    # RoutinePreferences, WakeUpPreferences (DataStore)
│   │   └── repository/         # TaskRepository, RoutineRepository
│   ├── di/                      # DatabaseModule (Hilt)
│   ├── notification/            # AlarmScheduler, BootReceiver, MorningNotificationReceiver, NotificationHelper
│   ├── presentation/
│   │   └── viewmodel/          # TaskViewModel, DashboardViewModel, PomodoroViewModel, CalendarViewModel, RoutineViewModel, NotificationViewModel
│   └── ui/
│       ├── components/          # TaskCard, GlassmorphismCard, GradientButton, WakeUpTimeDialog, etc.
│       ├── navigation/          # NavGraph + Routes
│       ├── screens/
│       │   ├── addedittask/     # AddEditTaskScreen (com timer de foco)
│       │   ├── calendar/        # CalendarScreen
│       │   ├── dashboard/       # DashboardScreen (Smart Dashboard)
│       │   ├── pomodoro/        # PomodoroScreen
│       │   ├── settings/        # RoutineSettingsScreen
│       │   └── tasklist/        # TaskListScreen
│       └── theme/               # Color, Theme, Typography
├── gradle/libs.versions.toml
└── README.md
```

## Modelo de dados (TaskEntity)

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | Long (PK) | ID auto-gerado |
| `title` | String | Título da tarefa |
| `description` | String | Descrição opcional |
| `priority` | Int | 0=Baixa, 1=Média, 2=Alta, 3=Urgente |
| `isDone` | Boolean | Status de conclusão |
| `dueDate` | Long? | Prazo (timestamp) |
| `startTime` | Long? | Início do evento |
| `endTime` | Long? | Fim do evento |
| `estimatedMinutes` | Int? | Tempo estimado (Feature 1) |
| `actualMinutes` | Int | Tempo real gasto (Feature 1) |
| `postponedCount` | Int | Vezes adiada (Feature 2) |
| `energyLevel` | Int? | 1=Leve, 2=Médio, 3=Exaustivo (Feature 3) |
| `completedAt` | Long? | Timestamp de conclusão (Feature 3) |
| `tags` | List\<String\> | Tags extraídas do título (Feature 4) |

## Como executar

### Pré-requisitos
- Android Studio (versão recente)
- Android SDK configurado (com `platform-tools`)
- JDK 11+

### Build

```bash
git clone https://github.com/VitorKreis/TaskApp.IA.git
cd TaskApp.IA
./gradlew :app:assembleDebug
```

No Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
```

### Instalar no dispositivo

```powershell
# Via ADB direto
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
```

## Roadmap

- [x] Fase 1: Coleta de Dados (Features 1-4)
- [x] Fase 2: Modo Pomodoro Nativo (Feature 5)
- [x] Fase 3: Smart Dashboard (Feature 6)
- [x] Fase 4: Preferências de Rotina e Notificações Inteligentes (Features 7-10)
- [ ] Fase 5: Motor de Recomendação Local (IA baseada em heurísticas)
- [ ] Fase 6: Sincronização em nuvem

## Contribuição

1. Crie uma branch para sua feature.
2. Faça commits pequenos e descritivos.
3. Abra um Pull Request com contexto da mudança.

## Licença

Projeto sob licença MIT.
