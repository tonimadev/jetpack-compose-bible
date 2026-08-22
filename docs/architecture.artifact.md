# Arquitetura do Projeto Bíblia Sagrada

Este documento descreve a arquitetura modular baseada no padrão **Feature-based Gradle Modules** com a separação **Bridge/Impl**.

## Visão Geral

A modularização visa acelerar o tempo de build através da paralelização e do cache do Gradle, além de impor limites claros entre as diferentes partes do sistema.

## Camadas e Módulos

### 1. Core Modules (`:core:*`)
Módulos que fornecem funcionalidades compartilhadas para todo o app.

- **`:core:common`**: Contém modelos de domínio básicos, utilitários, extensões e lógica que não depende de outras camadas.
- **`:core:database`**: Implementação da persistência local (Room).
- **`:core:network`**: Configuração do Retrofit, OkHttp e chamadas de API base.
- **`:core:ui`**: Componentes de UI reaproveitáveis, temas e recursos de design system.

### 2. Feature Modules (`:feature:*`)
Cada funcionalidade principal é dividida em dois submódulos:

#### Bridge (`:feature:[name]:bridge`)
- **Responsabilidade**: Define o contrato da feature.
- **Conteúdo**: Interfaces de repositórios, interfaces de use cases, modelos específicos da feature e navegação.
- **Dependências**: Apenas `:core:common` e outras `bridge` se necessário.
- **Vantagem**: Módulos que dependem desta feature dependem apenas da `bridge`, evitando recompilações quando a implementação muda.

#### Impl (`:feature:[name]:impl`)
- **Responsabilidade**: Implementação detalhada da feature.
- **Conteúdo**: ViewModels, Composables (Screens), implementação de Repositórios e Use Cases, e injeção de dependência local (Hilt).
- **Dependências**: Sua respectiva `bridge`, `:core:database`, `:core:network`, `:core:ui`, etc.

### 3. App Module (`:app`)
- **Responsabilidade**: Módulo orquestrador.
- **Conteúdo**: `Application` class, configuração global do Hilt e o grafo principal de navegação.
- **Dependências**: Depende de todos os módulos `:impl` para realizar a fiação (wiring) das dependências.

## Padrões de Projeto
- **Clean Architecture**: Separação clara entre Domínio, Dados e Apresentação.
- **MVVM / MVI**: Gerenciamento de estado da UI.
- **Injeção de Dependência**: Hilt para prover instâncias e desacoplar componentes.
- **Jetpack Compose**: Para construção da interface declarativa.

## Benefícios da Abordagem Bridge/Impl
1. **Compilação Incremental**: Alterações na UI ou lógica interna de uma feature (no `impl`) não disparam a recompilação de módulos que dependem dela via `bridge`.
2. **Testabilidade**: Facilita a criação de mocks para as interfaces definidas na `bridge`.
3. **Escalabilidade**: Novos desenvolvedores podem focar em uma feature específica sem precisar entender todo o monólito.

---
*Este documento é mantido para auxiliar agentes de IA e novos desenvolvedores no entendimento da estrutura do projeto.*
