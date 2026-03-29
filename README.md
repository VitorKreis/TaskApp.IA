# 🧠 Taskly.ai
### *O app de tarefas que aprende com você.*

> Mais do que um to-do list — um parceiro inteligente de produtividade que entende seus padrões, aprende com seus hábitos e te ajuda a evoluir dia a dia.

---

## 📌 Sobre o Projeto

**Taskly.ai** é um aplicativo mobile de organização de tarefas com foco em produtividade inteligente. Diferente de um simples to-do list, ele utiliza **Inteligência Artificial** para analisar o comportamento do usuário ao longo do tempo, identificar padrões e oferecer sugestões personalizadas que realmente fazem diferença na rotina.

O projeto nasce com uma visão de produto escalável: começando com funcionalidades essenciais de organização, e evoluindo progressivamente com camadas de IA cada vez mais sofisticadas.

---

## ✨ Funcionalidades

### MVP — Core
- [ ] Criação, edição e exclusão de tarefas
- [ ] Organização por categorias, prioridade e prazo
- [ ] Status de conclusão e histórico de tarefas
- [ ] Notificações e lembretes personalizados
- [ ] Autenticação de usuário (e-mail / OAuth)

### V2 — Análise de Produtividade
- [ ] Dashboard com métricas de desempenho semanal/mensal
- [ ] Gráficos de conclusão por categoria e horário
- [ ] Identificação de padrões de procrastinação
- [ ] Pontuação de produtividade (gamificação leve)

### V3 — Inteligência Artificial
- [ ] Sugestões de horários ideais com base no histórico
- [ ] Reagendamento automático de tarefas não concluídas
- [ ] Classificação automática de tarefas por contexto
- [ ] Assistente conversacional integrado (chat com IA)
- [ ] Previsão de carga de trabalho semanal

---

## 🏗️ Arquitetura

O projeto segue uma arquitetura modular e bem separada, pensada para escalar com a evolução do produto:

```
taskly-ai/
├── src/
│   ├── app/              # Navegação e rotas
│   ├── features/         # Módulos por funcionalidade (tasks, ai, auth, dashboard)
│   │   ├── tasks/
│   │   ├── analytics/
│   │   ├── ai/
│   │   └── auth/
│   ├── shared/           # Componentes, hooks e utils reutilizáveis
│   ├── services/         # Integrações com API e serviços externos
│   └── store/            # Gerenciamento de estado global
├── backend/ (opcional)
│   ├── api/              # Endpoints REST ou GraphQL
│   ├── ai-engine/        # Lógica de IA e análise de comportamento
│   └── database/         # Models e migrations
└── docs/                 # Documentação técnica e de produto
```

---

## 🤖 Módulo de IA

O coração diferencial do Taskly.ai. O motor de IA é construído de forma incremental:

| Camada | Tecnologia | Objetivo |
|--------|-----------|----------|
| **Análise de padrões** | Algoritmos de clustering (K-Means) | Identificar horários e contextos de maior produtividade |
| **Sugestões** | Modelo de recomendação | Sugerir o melhor momento para cada tipo de tarefa |
| **NLP** | LLM (ex: OpenAI / Gemini API) | Classificar e entender tarefas a partir de texto livre |
| **Assistente** | LLM com histórico de contexto | Chat inteligente integrado ao estado das tarefas |
| **Previsão** | Time Series (ex: Prophet) | Antecipar sobrecarga e estresse na agenda |

---

## 🛠️ Stack Tecnológica

> *A stack será definida conforme evolução do projeto. As opções abaixo são as candidatas principais.*

### Mobile
- **React Native** com Expo — desenvolvimento cross-platform (iOS + Android)
- **TypeScript** — tipagem estática para maior robustez
- **Zustand** ou **Redux Toolkit** — gerenciamento de estado
- **React Query / TanStack Query** — cache e sincronização de dados

### Backend
- **Node.js** com **Fastify** ou **NestJS** — API performática e modular
- **PostgreSQL** + **Prisma** — banco relacional com ORM moderno
- **Redis** — cache de sessões e dados frequentes

### IA / ML
- **Python** com **FastAPI** — microsserviço dedicado ao engine de IA
- **OpenAI API** / **Google Gemini** — LLM para linguagem natural
- **scikit-learn** — algoritmos de ML para análise de padrões

### Infra
- **Docker** + **Docker Compose** — ambiente containerizado
- **CI/CD** via GitHub Actions
- **Supabase** ou **Firebase** — alternativa BaaS para MVP rápido

---

## 🚀 Como Rodar o Projeto

### Pré-requisitos
- Node.js 18+
- Expo CLI
- Docker (opcional, para backend local)

### Instalação

```bash
# Clone o repositório
git clone https://github.com/seu-usuario/taskly-ai.git
cd taskly-ai

# Instale as dependências
npm install

# Configure as variáveis de ambiente
cp .env.example .env

# Rode o app mobile
npx expo start
```

### Backend (opcional para desenvolvimento local)
```bash
cd backend
docker-compose up -d
npm run dev
```

---

## 📊 Roadmap

```
Q1 ──── MVP: CRUD de tarefas + autenticação
   │
Q2 ──── Dashboard de produtividade + métricas
   │
Q3 ──── Engine de IA: análise de padrões + sugestões
   │
Q4 ──── Assistente conversacional + previsão de agenda
```

---

## 🤝 Contribuindo

Contribuições são bem-vindas! Para começar:

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feat/minha-feature`)
3. Commit suas mudanças (`git commit -m 'feat: adiciona X'`)
4. Push para a branch (`git push origin feat/minha-feature`)
5. Abra um Pull Request

Por favor, siga o padrão de commits [Conventional Commits](https://www.conventionalcommits.org/).

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

<div align="center">
  <sub>Feito com 💙 e muita produtividade.</sub>
</div>
