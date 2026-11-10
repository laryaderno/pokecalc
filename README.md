# PokéCalc RB

*Calculadora de dano da Geração 1 de Pokémon (Red/Blue/Yellow) desenvolvida para Android em Java como trabalho da Avaliação 3 de Programação Mobile.*

## Sobre o Projeto

O **PokéCalc RB** é um aplicativo mobile nativo para Android que implementa a fórmula clássica de cálculo de dano da Geração 1 de Pokémon. O usuário escolhe o Pokémon atacante, o defensor (IDs #1 a #151), os níveis e um golpe específico. O aplicativo busca e processa os dados diretamente na **PokéAPI** e exibe:

- **Dano mínimo e máximo**, com e sem acerto crítico.

- **Porcentagem do HP** do defensor consumida.

- **Número de golpes necessários** para o nocaute (2HKO, 3HKO, etc.).

- **Efetividade do tipo** baseada estritamente nas regras e bugs da Gen 1 (ex: Fantasma contra Psíquico = $0\times$).

- **Histórico pessoal** de cálculos salvos por usuário.

## Funcionalidades

- **Autenticação Segura:** Cadastro e login de usuários com senhas protegidas por hash **BCrypt** com *salt*. Sessão persistente pós-fechamento e gerenciamento de logout.

- **Calculadora Gen 1 Fiel:** Implementação da fórmula oficial da primeira geração (STAB, crítico, regra do 255 e tabela de tipos original). Campos com autocompletar inteligente para Pokémon e golpes.

- **Dados Históricos Reais:** Uso dos parâmetros `past_types`, `past_stats` e `past_values` da PokéAPI para garantir que as características da época sejam respeitadas (ex: Magnemite puramente Elétrico, *Bite* do tipo Normal e Clefairy Normal).

- **Modo Offline (Room):** Cada Pokémon e golpe consultado é armazenado em banco de dados local via **Room**, permitindo novos cálculos sem conexão com a internet.

- **Histórico Pessoal:** Cada usuário visualiza e gerencia exclusivamente o seu próprio histórico de cálculos.

- **Sistema de Notificações:** Notificação interativa ao salvar um cálculo (o toque direciona diretamente para a tela de histórico).

- **Perfil de Usuário:** Tela dedicada para alteração de nome, e-mail e senha, com suporte a foto de perfil obtida via **Câmera** ou **Photo Picker** (Galeria).

- **Splash Screen:** Inicialização fluida utilizando a *Splash Screen API* oficial do Android.

## Tecnologias e Arquitetura

O projeto adota uma arquitetura limpa e modularizada, separando regras de negócio em Java puro dos componentes de UI do Android.

| Camada / Domínio | Tecnologia / Biblioteca | 
| ----- | ----- | 
| **Linguagem e Plataforma** | Java 11 · Android SDK 35 (Mínimo: SDK 26) · Gradle 8.9 / AGP 8.7 | 
| **Interface de Usuário** | Material 3 · AppCompat · Splash Screen API · BottomNavigation | 
| **Arquitetura** | MVVM (ViewModel + LiveData) · Repositórios | 
| **Banco de Dados Local** | Room 2.6.1 (4 tabelas: `usuarios`, `calculos` com FK, cache de Pokémon e golpes) | 
| **Comunicação de Rede** | Retrofit 2.11 + Gson · OkHttp 4.12 | 
| **Gerenciamento de Mídia** | Glide 4.16 (sprites) · FileProvider (Câmera) · Photo Picker (Galeria) | 
| **Segurança** | `at.favre.lib:bcrypt` 0.10.2 (Custo 10) | 

## Estrutura do Projeto

```
PokeCalcRB/app/src/main/java/br/ufms/pokecalcrb/
├── PokeCalcApp.java          # Configuração inicial e canal de notificação
├── domain/                   # Java puro e testável: fórmula Gen 1 e tabela de tipos
├── data/
│   ├── db/                   # Room: Entidades e DAOs
│   ├── api/                  # Retrofit, DTOs e ConversorGen1
│   └── repo/                 # Repositórios (cache → API) e gestão de sessão
├── security/                 # Gerenciamento de Hash (BCrypt)
├── ui/
│   ├── auth/                 # Telas de Login e Cadastro
│   ├── main/                 # MainActivity (Splash e navegação estruturada)
│   ├── calc/                 # Módulo da Calculadora
│   ├── historico/            # Módulo de Histórico de Cálculos
│   └── perfil/               # Módulo de Perfil (Câmera, Galeria, Senha e Logout)
└── util/                     # Validadores, utilitários de notificação e autocompletar
```

## Como Executar o Projeto

### Opção 1: Instalação Direta do APK

1. Copie o arquivo `PokeCalcRB.apk` para um dispositivo físico ou emulador com **Android 8.0 (API 26) ou superior**.

2. Instale o aplicativo. *(Nota: Para o funcionamento das notificações interativas, recomenda-se Android 13 / API 33 ou superior).*

### Opção 2: Compilação via Android Studio

1. Abra o **Android Studio**.

2. Selecione **File > Open** e escolha a pasta raiz do projeto `PokeCalcRB`.

3. Aguarde o sincronizamento do **Gradle**. *(Certifique-se de estar conectado à internet na primeira execução para o download automático das dependências).*

4. Conecte um dispositivo Android ou inicie um emulador e clique no botão **Run ( ▶ )**.

### Opção 3: Compilação por Linha de Comando (CLI)

Na raiz do diretório `PokeCalcRB`, execute o comando do Gradle Wrapper:

```bash
./gradlew assembleDebug
```

O executável compilado estará disponível em:
`PokeCalcRB/app/build/outputs/apk/debug/app-debug.apk`

## Testes

- **Testes Unitários (JUnit):** Validam matematicamente a fórmula de dano, tabela de tipos e regras específicas da Geração 1.

- *Localização do arquivo:* `PokeCalcRB/app/src/test/java/br/ufms/pokecalcrb/domain/CalculadoraGen1Test.java`

- *Para rodar via terminal:*

    ```bash
    ./gradlew test
    ```

- **Testes de Caixa-Preta:** Com 43 casos de teste validados com sucesso em emulador Android 15. Os resultados detalhados por caso encontram-se na seção 11 da especificação, com evidências visuais em `docs/prints`.

## Documentação

A especificação técnica completa contendo os papéis de usuário, requisitos funcionais e não funcionais, modelo de dados detalhado, telas e fundamentação matemática da fórmula está documentada em:
`docs/ESPECIFICACAO.md`

## Equipe

- **Laryssa Bernardo Aderno**
- **Arthur Irineu de Souza Ortiz**
- **Laura Santa Cruz Rodrigues**

## Aviso Legal

*Pokémon e todas as marcas associadas são propriedades intelectuais da Nintendo, Game Freak e Creatures Inc. Este projeto possui caráter estritamente acadêmico, sem fins comerciais. Todos os dados de consumo são obtidos publicamente através da PokéAPI.*
