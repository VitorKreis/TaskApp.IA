# TaskApp.IA

Aplicativo Android para gestão de tarefas com foco em organização diária, visão de progresso e planejamento por calendário.

## Visão geral

O projeto foi desenvolvido em Kotlin com Jetpack Compose e arquitetura em camadas (UI, ViewModel, Repository e Room).
Atualmente o app funciona de forma local/offline, armazenando os dados no dispositivo.

## Funcionalidades disponíveis hoje

### Tarefas
- Criar tarefa com título, descrição e prioridade.
- Editar e excluir tarefa.
- Marcar tarefa como concluída.
- Definir prazo (data e hora) opcional.
- Definir intervalo de evento (início e fim) opcional.
- Cálculo visual de duração quando início e fim são definidos.
- Validações de data/hora:
   - não permite prazo no passado ao criar;
   - não permite horário de início no passado ao criar;
   - não permite fim menor ou igual ao início.

### Lista de tarefas
- Filtros por status:
   - Ativas
   - Todas
   - Concluídas
   - Atrasadas
- Estado vazio amigável quando não há itens.

### Dashboard
- Cards de resumo com métricas:
   - Total
   - Pendentes
   - Concluídas
   - Atrasadas
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
- Tema escuro com componentes customizados (glassmorphism, gradientes e animações).

## Tecnologias usadas

- Kotlin 2.0
- Android Gradle Plugin 8.13
- Jetpack Compose + Material 3
- Navigation Compose
- Hilt (injeção de dependência)
- Room (persistência local)
- Kotlin Coroutines + Flow
- MPAndroidChart (gráfico no dashboard)

## Estrutura resumida

```
MyApplication/
├── app/
│   ├── src/main/java/com/example/myapplication/
│   │   ├── data/          # Room (Entity, Dao, Database) + Repository
│   │   ├── di/            # Módulos do Hilt
│   │   ├── presentation/  # ViewModels
│   │   └── ui/            # Navegação, telas e componentes Compose
│   └── build.gradle.kts
├── gradle/libs.versions.toml
└── README.md
```

## Como executar

### Pré-requisitos
- Android Studio (versão recente)
- Android SDK configurado (com `platform-tools`)
- JDK 11+

### Passos

```bash
git clone <url-do-repositorio>
cd MyApplication
./gradlew :app:assembleDebug
```

No Windows PowerShell, use:

```powershell
.\gradlew.bat :app:assembleDebug
```

### Instalar no dispositivo/emulador

```powershell
.\gradlew.bat :app:installDebug
```

### Atalho para instalar e abrir o app

```powershell
.\gradlew.bat runDebug
```

Se houver conflito de assinatura em instalações anteriores:

```powershell
.\gradlew.bat runFreshDebug
```

## Estado atual e próximos passos

Hoje o app entrega um fluxo completo de tarefas com visão em lista, dashboard e calendário, tudo com persistência local.

Ainda não implementado nesta versão:
- autenticação de usuário;
- sincronização em nuvem;
- notificações/lembretes ativos;
- recursos de IA.

## Contribuição

1. Crie uma branch para sua feature.
2. Faça commits pequenos e descritivos.
3. Abra um Pull Request com contexto da mudança.

## Licença

Projeto sob licença MIT (ajuste conforme o repositório, se necessário).
