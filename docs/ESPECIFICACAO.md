# PokéCalc RB — Documento de Especificação (Avaliação 3 · Programação Mobile)

## 1. Identificação

| Integrante | Nome completo |
|---|---|
| 1 | _[preencher]_ |
| 2 | _[preencher]_ |
| 3 | _[preencher]_ |

Repositório/pasta do projeto: https://github.com/Arakakig/pokecalc-rb — compartilhado com matheus.albuquerque@ufms.br

## 2. Visão geral do software

O **PokéCalc RB** é uma calculadora de dano da **Geração 1** de Pokémon (Red/Blue), feita em Android nativo com Java.
O usuário entra na conta, escolhe um Pokémon atacante, um defensor (só #1 a #151), os níveis e um golpe. O app busca os dados na **PokéAPI** e calcula o dano mínimo e máximo, com e sem acerto crítico, a porcentagem do HP do defensor, quantos golpes são precisos para nocautear e a efetividade do tipo.
Cada cálculo pode ser salvo num **histórico** pessoal. Ao salvar, o app mostra uma **notificação**.

Serve para jogadores de Red/Blue/Yellow (incluindo speedrunners e quem joga Nuzlocke) que querem saber se um golpe nocauteia antes de arriscar no jogo.

Configuração fixa assumida (e informada na tela): **DV 15 e Stat Exp máximo** ("Pokémon treinado ao máximo").

## 3. Papéis de usuários

| Papel | O que pode fazer |
|---|---|
| **Usuário não autenticado** | Ver a tela de abertura, **entrar** (login) e **criar conta** (cadastro). Não acessa a calculadora, o histórico nem o perfil. |
| **Usuário autenticado** | Usar a **calculadora**, **salvar** cálculos, **ver e apagar** o próprio histórico (só o dele), **editar o perfil** (nome, e-mail, senha e foto) e **sair** (logout). |

## 4. Requisitos funcionais

### 4.1 Conta e sessão

| ID | Ator | Entrada | Processamento | Saída |
|---|---|---|---|---|
| RF01 Cadastrar | Não autenticado | Formulário: nome, e-mail, senha, confirmação | Valida campos vazios, e-mail inválido, senha com menos de 6 caracteres, senhas diferentes e e-mail já usado. Gera o **hash BCrypt com salt** (custo 10) e grava na tabela `usuarios` do Room. Abre a sessão. | Mensagem no campo com erro; ou vai para a Calculadora |
| RF02 Login | Não autenticado | E-mail e senha | Busca o usuário pelo e-mail e compara a senha digitada com o hash (`BCrypt.verifyer`). | "E-mail ou senha incorretos" (a mesma mensagem nos dois casos, para não revelar quais e-mails existem); ou vai para a Calculadora |
| RF03 Manter sessão | Autenticado | — | Guarda **só o `userId`** em SharedPreferences. Na abertura, a Splash Screen confere se o usuário ainda existe no banco. | Abre direto na Calculadora |
| RF04 Logout | Autenticado | Clique em "Sair" (barra superior ou Perfil) + confirmação | Apaga o `userId` da sessão e limpa a pilha de telas. | Volta ao Login com "Você saiu da conta." O botão "voltar" não reabre a área restrita. |
| RF05 Proteção das telas | Sistema | — | A `MainActivity` (área restrita) confere a sessão no `onCreate` e no `onStart`. Sem sessão, manda para o Login. | Acesso negado sem login |

### 4.2 Perfil

| ID | Entrada | Processamento | Saída |
|---|---|---|---|
| RF06 Editar nome e e-mail | Formulário do Perfil | Valida e confere se o e-mail não é de outra conta. Faz `UPDATE` no Room. | "Perfil atualizado"; a saudação da barra superior muda na hora (LiveData) |
| RF07 Trocar senha | Senha atual, nova, confirmação | Confere a senha atual com o hash. Gera um **novo hash BCrypt** (novo salt). A senha antiga não fica guardada. | "Senha alterada" ou erro no campo |
| RF08 Trocar foto | Botão **Câmera** (foto) ou **Galeria** (Photo Picker) | A câmera grava a foto direto na pasta interna do app (via FileProvider). A imagem da galeria é copiada para a pasta interna. O **caminho** do arquivo é salvo em `usuarios.fotoPath` e a foto anterior é apagada. | Foto redonda no Perfil, que continua lá depois de reabrir o app |

### 4.3 Calculadora

| ID | Entrada | Processamento | Saída |
|---|---|---|---|
| RF09 Escolher Pokémon | Nome ou número do atacante e do defensor (com autocompletar) | Padroniza o texto ("Mr. Mime" → `mr-mime`). Aceita só #1 a #151. | Erro no campo, se estiver vazio |
| RF10 Níveis e golpe | Nível 1–100 de cada um; nome do golpe (com autocompletar) | Valida a faixa do nível. | "Nível deve ser de 1 a 100" |
| RF11 Buscar e calcular | Botão "Calcular dano" | Busca primeiro no cache (Room). Se não tiver, faz `GET /pokemon/{x}` e `GET /move/{x}` na PokéAPI (Retrofit + Gson). Converte para os valores da Gen 1 (`past_types`, `past_stats`, `past_values`) e aplica a fórmula da Gen 1. | Indicador de carregamento |
| RF12 Mostrar resultado | — | — | Sprite de Red/Blue, nome, número e tipos dos dois Pokémon; golpe (tipo, poder, físico/especial, STAB); stats usados; dano normal e crítico (mín.–máx.) com % do HP; "Nocauteia em X golpes"; texto de efetividade; chance de crítico |
| RF13 Salvar no histórico | Botão "Salvar no histórico" | Grava na tabela `calculos` (ligada ao usuário). Dispara a notificação. | Notificação "Cálculo salvo: Charizard → Venusaur (98–116 HP)". Se não houver permissão, aparece um aviso com o botão "Ativar". |

### 4.4 Histórico

| ID | Entrada | Processamento | Saída |
|---|---|---|---|
| RF14 Listar | Abrir a aba Histórico (ou tocar na notificação) | Consulta `calculos WHERE userId = sessão ORDER BY data DESC` (LiveData) | Lista do mais recente ao mais antigo; mensagem quando está vazia |
| RF15 Apagar | Ícone de lixeira + confirmação | `DELETE ... WHERE id = ? AND userId = ?` | O item some da lista; aparece "Cálculo apagado" |

### 4.5 Falhas

| ID | Situação | Saída (sem crash) |
|---|---|---|
| RF16 | Sem internet | "Sem conexão com a internet. Pokémon já consultados funcionam offline." |
| | Pokémon ou golpe inexistente (404) | "Pokémon "abcxyz" não encontrado" |
| | Demora da rede (timeout de 10–15 s) | "A PokéAPI demorou demais para responder." |
| | Pokémon fora da Gen 1 / golpe criado depois da Gen 1 | "Só são aceitos Pokémon da Gen 1 (#1 a #151)" / "O golpe "crunch" não existia na Gen 1." |
| | Golpe de status ou de dano fixo | ""thunder-wave" não causa dano pela fórmula…" |

## 5. Regras de cálculo (Gen 1)

- Stat = ((Base + 15) × 2 + 64) × Nível / 100 + 5
- HP = ((Base + 15) × 2 + 64) × Nível / 100 + Nível + 10
- Dano = (((2 × Nível × Crit / 5 + 2) × Poder × Atq / Def) / 50 + 2) × STAB × Tipo1 × Tipo2 × R / 255
  - Crit = 2 no crítico; STAB = 1,5; R = 217 (mínimo) e 255 (máximo); todas as divisões arredondam para baixo.
  - Físico (Normal, Lutador, Voador, Venenoso, Terrestre, Pedra, Inseto, Fantasma) usa Ataque × Defesa. Especial (Fogo, Água, Planta, Elétrico, Psíquico, Gelo, Dragão) usa Special × Special.
  - Regra do 255: se Atq ou Def passar de 255, os dois são divididos por 4.
- A **tabela de tipos da Gen 1** (15 tipos) fica embutida no app (`TabelaTipos.java`). Ela mantém as peculiaridades da época: Fantasma não afeta Psíquico, Inseto × Venenoso = 2×, Gelo × Fogo = 1×.

Simplificações assumidas:
1. A PokéAPI traz os dados atuais. O app usa os campos históricos dela (`past_types`, `past_stats` e `past_values`) para voltar aos valores da Gen 1. Por exemplo, Clefairy volta a ser Normal, Magnemite volta a ser só Elétrico e Bite volta a ser um golpe Normal. Tipos que não existiam (Aço, Sombrio, Fada) são ignorados.
2. Golpes de dano fixo (Seismic Toss, Dragon Rage, Super Fang…) e de status não são calculados: o app avisa em vez de calcular. Golpes de vários acertos mostram o dano de um acerto só.

## 6. Banco de dados (Room)

| Tabela | Campos | Observação |
|---|---|---|
| `usuarios` | id (PK), nome, email (único), senhaHash, fotoPath | A foto é guardada como **caminho** do arquivo |
| `calculos` | id (PK), userId (**FK → usuarios**, CASCADE), atacante, defensor, golpe, nivelAtq, nivelDef, danoMin, danoMax, critMin, critMax, hpDefensor, efetividade, data | Relação 1 usuário : N cálculos |
| `pokemon_cache` | numero (PK), nome (único), hp, atk, def, special, speed, tipos, spriteUrl | Cache da API |
| `golpe_cache` | nome (PK), poder, tipo | Cache da API |

## 7. Segurança

- Senha: **BCrypt com salt** (biblioteca `at.favre.lib:bcrypt`, custo 10). Nunca é guardada em texto puro nem com criptografia reversível. A troca de senha gera um novo hash.
- Sessão: só o `userId` fica em SharedPreferences privadas, nunca a senha.
- Login com mensagem genérica (não revela se o e-mail existe).
- Cada usuário só vê e só apaga os próprios cálculos (o `userId` faz parte da consulta).
- As fotos ficam no armazenamento interno do app. A câmera recebe um `content://` temporário do FileProvider, sem acesso ao resto dos arquivos.
- `android:allowBackup="false"`: os dados do app não vão para o backup na nuvem.

## 8. Recursos do dispositivo e permissões

| Recurso | Permissão | Tratamento |
|---|---|---|
| **Requisição HTTP (recurso obrigatório escolhido)** — PokéAPI | `INTERNET` (só declarada) | Timeouts, mensagens amigáveis, cache offline |
| Câmera | `CAMERA` (pedida em tempo de execução) | Explica antes de pedir de novo. Se o usuário negar, oferece a Galeria. Se negar de vez, oferece abrir as Configurações. A câmera é opcional (`required="false"`). |
| Galeria | nenhuma (Photo Picker) | — |
| Notificações | `POST_NOTIFICATIONS` (pedida no Android 13+) | É pedida uma vez ao entrar. Se o usuário negar, o app funciona normalmente e, ao salvar, avisa "Cálculo salvo. As notificações estão desativadas." com o botão "Ativar". Um canal de notificação próprio é criado. |
| Splash Screen | — | `androidx.core:splashscreen` com `setKeepOnScreenCondition` enquanto a sessão é verificada. **Sem `Thread.sleep`.** |
| Ícone personalizado | — | Ícone adaptativo (`@mipmap/ic_launcher`) e nome configurados no `AndroidManifest.xml` |

## 9. Telas e navegação

`Splash` → (sem sessão) `LoginActivity` ⇄ `CadastroActivity`
`Splash` → (com sessão) `MainActivity` com **BottomNavigationView**: `CalculadoraFragment` · `HistoricoFragment` · `PerfilFragment`

São 3 Activities e 3 Fragments funcionais. A splash não conta como tela.
Arquitetura: Activities/Fragments → **ViewModel + LiveData** → Repositórios → Room / Retrofit. O banco, a rede e o BCrypt rodam em segundo plano (`Executores.io()`).

## 10. Focos principais (conceitos aplicados)

**Usabilidade** (foco principal):
- Autocompletar com os 151 Pokémon e os 165 golpes da Gen 1, e aceita também o número.
- Validação campo a campo, com a mensagem no próprio campo.
- Indicador de carregamento e botões desativados durante as operações.
- Confirmação antes de apagar e antes de sair. Aviso com ação ("Galeria", "Ativar", "Configurações").
- O nível vem preenchido com 50. Mostrar/ocultar senha. Tocar na notificação abre o Histórico. Os resultados continuam na tela ao girar o aparelho ou trocar de aba.

**Acessibilidade** (foco principal):
- `contentDescription` em todas as imagens e botões de ícone. Exemplos: "Imagem de Charizard" e "Apagar cálculo Charizard contra Venusaur".
- Títulos marcados como cabeçalho (`accessibilityHeading`). Mensagens de erro com `accessibilityLiveRegion` (o TalkBack lê assim que aparecem).
- O resultado do cálculo é anunciado ao TalkBack: "Charizard contra Venusaur: de 98 a 116 de dano. Super efetivo!".
- Áreas de toque de no mínimo 48 dp. Textos em `sp` (respeitam o tamanho de fonte do sistema). Rótulos com `hint` em todos os campos.
- Contraste do vermelho principal (#B71C1C) com o branco = 6,6:1 (WCAG AA). Tema escuro com cores próprias.
- Cada item do histórico é lido de uma vez pelo TalkBack.

**Economia de energia** (bônus):
- Cache no Room: Pokémon e golpes já consultados não geram nova requisição.
- A rede só é usada quando o usuário toca em "Calcular", sem consultas periódicas. Timeouts curtos.
- O Glide guarda os sprites em cache de disco. Nenhum sensor nem GPS é usado.

## 11. Testes caixa-preta

**Como os testes foram feitos:** os 43 casos foram executados.
- A fórmula, o conversor da API e a segurança (T12, T32–T39) rodaram com JUnit, usando respostas reais da PokéAPI.
- Os casos de tela rodaram no app instalado em um emulador Android 15 (Pixel), em 06/11/2026. A internet estava ligada, com modo avião só em T27 e T28.
- Cada resultado tem um print em `docs/prints/`, com o ID do caso no nome do arquivo.
- **Resultado: 43 de 43 aprovados.** Não houve crash em nenhum caso.

Testes automatizados no projeto: `app/src/test/java/.../CalculadoraGen1Test.java`. Para rodar: botão direito no arquivo → *Run*.

### 11.1 Validação de campos

| ID | Condição inicial | Ação/entrada | Esperado | Obtido | Status |
|---|---|---|---|---|---|
| T01 | Tela de cadastro | Tocar em "Cadastrar" com tudo vazio | Erro em cada campo ("Informe o seu nome", "Informe o e-mail"…), sem crash | ✅ Emulador (Android 15): erro em todos os campos ("Informe o seu nome", "Informe o e-mail", "Informe a senha", "Confirme a senha"), sem crash — `prints/T01_cadastro_vazio.png` | Aprovado |
| T02 | Tela de cadastro | E-mail `ash@` | "E-mail inválido (ex.: ash@pallet.com)" | ✅ Emulador (Android 15): "E-mail inválido (ex.: ash@pallet.com)" — `prints/T02_T03_T04_validacoes.png` | Aprovado |
| T03 | Tela de cadastro | Senha `123` | "A senha deve ter pelo menos 6 caracteres" | ✅ Emulador (Android 15): "A senha deve ter pelo menos 6 caracteres" — `prints/T02_T03_T04_validacoes.png` | Aprovado |
| T04 | Tela de cadastro | Senha e confirmação diferentes | "As senhas não conferem" | ✅ Emulador (Android 15): "As senhas não conferem" — `prints/T02_T03_T04_validacoes.png` | Aprovado |
| T05 | Já existe a conta ash@pallet.com | Cadastrar de novo com ash@pallet.com | "Este e-mail já está cadastrado" no campo e-mail | ✅ Emulador (Android 15): "Este e-mail já está cadastrado" no campo e-mail — `prints/T05_email_duplicado.png` | Aprovado |
| T06 | Calculadora | Nível `0` | "Nível deve ser de 1 a 100" | ✅ Emulador (Android 15): "Nível deve ser de 1 a 100" no nível do atacante — `prints/T06_T07_nivel_invalido.png` | Aprovado |
| T07 | Calculadora | Nível `101` | "Nível deve ser de 1 a 100" | ✅ Emulador (Android 15): "Nível deve ser de 1 a 100" no nível do defensor — `prints/T06_T07_nivel_invalido.png` | Aprovado |
| T08 | Calculadora | Atacante vazio | "Informe o nome ou o número do Pokémon" | ✅ Emulador (Android 15): "Informe o nome ou o número do Pokémon" — `prints/T08_atacante_vazio.png` | Aprovado |

### 11.2 Autenticação e perfil

| ID | Condição inicial | Ação/entrada | Esperado | Obtido | Status |
|---|---|---|---|---|---|
| T09 | App recém-instalado | Cadastrar Ash / ash@pallet.com / pikachu1 | Entra direto na Calculadora com "Olá, Ash" na barra | ✅ Emulador (Android 15): abriu a Calculadora com "Olá, Ash" — `prints/T09_cadastro_ok.png` | Aprovado |
| T10 | Conta T09 existe | Login com a senha certa | Abre a Calculadora | ✅ Emulador (Android 15): abriu a Calculadora — `prints/T10_login_ok.png` | Aprovado |
| T11 | Conta T09 existe | Login com a senha `errada1` | "E-mail ou senha incorretos" | ✅ Emulador (Android 15): "E-mail ou senha incorretos", continua no Login — `prints/T11_login_errado.png` | Aprovado |
| T12 | — | Cadastrar a mesma senha 2 vezes | Os hashes são diferentes (salt) e os dois conferem | ✅ hashes `$2a$10$…` diferentes; `conferir` = true | Aprovado |
| T13 | Logado | Perfil: mudar o nome para "Ash Ketchum" → Salvar → fechar e reabrir o app | "Perfil atualizado"; a barra mostra "Olá, Ash Ketchum" depois de reabrir | ✅ Emulador (Android 15): "Perfil atualizado"; depois de fechar e reabrir, a barra mostra "Olá, Ash Ketchum" — `prints/T13_perfil_salvo.png`, `prints/T13_nome_apos_reabrir.png` | Aprovado |
| T14 | Logado | Alterar senha (atual certa, nova `pikachu2`) → Sair → entrar com `pikachu2` | Entra; a senha `pikachu1` não funciona mais | ✅ Emulador (Android 15): "Senha alterada"; `pikachu1` foi recusada e `pikachu2` entrou — `prints/T14_*.png` | Aprovado |
| T15 | Logado | Alterar senha com a senha atual errada | "A senha atual está incorreta" | ✅ Emulador (Android 15): "A senha atual está incorreta" no campo do diálogo — `prints/T15_senha_atual_errada.png` | Aprovado |
| T16 | Logado | Sair → confirmar → apertar "voltar" | Volta ao Login; o "voltar" fecha o app, não reabre a Calculadora | ✅ Emulador (Android 15): voltou ao Login com "Você saiu da conta."; o "voltar" fechou o app — `prints/T16_logout.png` | Aprovado |
| T17 | Logado | Fechar o app (tirar dos recentes) e abrir de novo | A splash abre direto na Calculadora (sessão mantida) | ✅ Emulador (Android 15): reabriu direto na Calculadora (sessão mantida) — `prints/T17_sessao_mantida.png` | Aprovado |

### 11.3 Câmera e galeria

| ID | Condição inicial | Ação/entrada | Esperado | Obtido | Status |
|---|---|---|---|---|---|
| T18 | Permissão de câmera ainda não pedida | Perfil → Câmera → Permitir → tirar foto | A foto aparece redonda no Perfil | ✅ Emulador (Android 15): pediu a permissão (primeiro a explicação do app, depois o sistema), abriu a câmera, a foto foi tirada e apareceu redonda no Perfil com "Foto atualizada" — `prints/T18_*.png` | Aprovado |
| T19 | — | Perfil → Câmera → **Negar** | Aviso "Sem permissão de câmera…" com o botão "Galeria"; sem crash | ✅ Emulador (Android 15): "Sem permissão de câmera. Você pode escolher uma foto da galeria." com o botão "Galeria"; sem crash — `prints/T19_camera_negada.png` | Aprovado |
| T20 | — | Perfil → Galeria → escolher uma imagem | A foto é atualizada (sem pedir permissão) | ✅ Emulador (Android 15): abriu o seletor de fotos do Android sem pedir permissão; a imagem escolhida virou a foto do perfil — `prints/T20_seletor.png`, `prints/T20_foto_galeria.png` | Aprovado |
| T21 | Foto já definida | Fechar e reabrir o app | A foto continua no Perfil (o caminho fica salvo no Room) | ✅ Emulador (Android 15): depois de fechar e reabrir, a foto continua no Perfil — `prints/T21_foto_apos_reabrir.png` | Aprovado |
| T22 | — | Abrir a câmera e cancelar | Nada muda; sem crash | ✅ Emulador (Android 15): voltou ao Perfil sem mudar a foto; sem crash — `prints/T22_camera_cancelada.png` | Aprovado |

### 11.4 Notificações e API (recurso escolhido: requisição HTTP)

| ID | Condição inicial | Ação/entrada | Esperado | Obtido | Status |
|---|---|---|---|---|---|
| T23 | Permissão de notificação concedida | Calcular e salvar Charizard × Venusaur | Notificação "Cálculo salvo: Charizard → Venusaur (98–116 HP)"; tocar nela abre o Histórico | ✅ Emulador (Android 15): notificação "Cálculo salvo: … (… HP)" e aviso "Cálculo salvo no histórico"; tocar na notificação abriu a aba Histórico — `prints/T23_notificacao.png`, `prints/T23_toque_abriu_historico.png` | Aprovado |
| T24 | Permissão de notificação **negada** | Salvar um cálculo | Salva mesmo assim; aviso "As notificações estão desativadas" com o botão "Ativar" | ✅ Emulador (Android 15): salvou e mostrou "Cálculo salvo. As notificações estão desativadas." com o botão "Ativar" — `prints/T24_notif_negada.png` | Aprovado |
| T25 | Internet ligada | Atacante `pikachu`, defensor `onix`, golpe `thunderbolt` | Sprites, tipos e dano aparecem ("Não afeta o defensor..." porque Onix é Terrestre) | ✅ Emulador (Android 15): Onix "Pedra / Terrestre", "Não afeta o defensor...", Normal 0 – 0 HP, "Não causa dano" — `prints/T25_pikachu_onix_resultado.png` | Aprovado |
| T26 | Internet ligada | Atacante `abcxyz` | "Pokémon "abcxyz" não encontrado" | ✅ Emulador (Android 15): "Pokémon "abcxyz" não encontrado" — `prints/T26_pokemon_inexistente.png` | Aprovado |
| T27 | **Modo avião** | Calcular com um Pokémon nunca consultado | "Sem conexão com a internet…" sem crash | ✅ Emulador (Android 15): "Sem conexão com a internet. Pokémon já consultados funcionam offline.", sem crash — `prints/T27_modo_aviao_sem_cache.png` | Aprovado |
| T28 | Modo avião; Charizard e Venusaur já consultados | Calcular de novo | Funciona (cache do Room) | ✅ Emulador (Android 15): em modo avião o cálculo Charizard × Venusaur funcionou (98 – 116 HP) usando o cache — `prints/T28_modo_aviao_com_cache.png` | Aprovado |
| T29 | — | Defensor `152` | "Número inválido: use de 1 a 151 (Gen 1)" | ✅ JUnit e emulador: "Número inválido: use de 1 a 151 (Gen 1)" — `prints/T29_numero_152.png` | Aprovado |
| T30 | — | Golpe `crunch` (Gen 2) | "O golpe "crunch" não existia na Gen 1." | ✅ JUnit e emulador: "O golpe "crunch" não existia na Gen 1." — `prints/T30_golpe_gen2.png` | Aprovado |
| T31 | — | Golpe `thunder-wave` (status) | "…não causa dano pela fórmula…" | ✅ JUnit e emulador: ""thunder-wave" não causa dano pela fórmula (golpe de status ou de dano fixo)." — `prints/T31_golpe_status.png` | Aprovado |
| T32 | — | Conversão de Clefairy, Magnemite e Bite (JSON real) | Normal; só Elétrico com Special 95; Bite Normal com poder 60 | ✅ igual ao esperado | Aprovado |

### 11.5 Cálculo

| ID | Condição inicial | Ação/entrada | Esperado | Obtido | Status |
|---|---|---|---|---|---|
| T33 | — | Nv 50, Atq 100, Def 100, poder 100, sem STAB, 1× | Normal 39–46 | ✅ 39–46 | Aprovado |
| T34 | — | Mesmo do T33, crítico | 73–86 | ✅ 73–86 | Aprovado |
| T35 | — | Golpe Normal contra Fantasma | 0 / "Não afeta o defensor..." | ✅ 0 | Aprovado |
| T36 | — | Mesmo do T33, com STAB (R = 255) | 69 | ✅ 69 | Aprovado |
| T37 | — | Charizard (Nv 50) Flamethrower × Venusaur (Nv 50), dados reais | Especial, STAB, 2×: 98–116, crítico 185–218, HP 187, "Nocauteia em 2 golpes" | ✅ JUnit e emulador: "Super efetivo!", Normal 98 – 116 HP, Crítico 185 – 218 HP, "Nocauteia em 2 golpes" — `prints/T37_charizard_venusaur_resultado.png` | Aprovado |
| T38 | — | Regra do 255 (Atq 300, Def 280) | Mesmo dano que Atq 75 / Def 70 | ✅ igual | Aprovado |
| T39 | — | Tabela Gen 1: Fantasma × Psíquico, Inseto × Venenoso, Gelo × Fogo | 0×, 2×, 1× | ✅ igual | Aprovado |

### 11.6 Histórico

| ID | Condição inicial | Ação/entrada | Esperado | Obtido | Status |
|---|---|---|---|---|---|
| T40 | Logado, histórico vazio | Abrir Histórico | Mensagem "Nenhum cálculo salvo ainda" | ✅ Emulador (Android 15): "Nenhum cálculo salvo ainda." para a usuária nova (Misty) — `prints/T40_historico_vazio.png` | Aprovado |
| T41 | 2 cálculos salvos | Abrir Histórico | Os 2 aparecem, o mais recente primeiro | ✅ Emulador (Android 15): "Pikachu → Squirtle" (último salvo) aparece antes de "Snorlax → Pikachu" — `prints/T41_dois_itens.png` | Aprovado |
| T42 | Item na lista | Lixeira → Apagar | O item some e aparece "Cálculo apagado" | ✅ Emulador (Android 15): diálogo "Apagar cálculo?" → Apagar: o item sumiu e apareceu "Cálculo apagado" — `prints/T42_*.png` | Aprovado |
| T43 | Usuário A tem cálculos | Sair, entrar como usuário B | B não vê os cálculos de A | ✅ Emulador (Android 15): ao entrar como Ash, nenhum cálculo da Misty aparece — `prints/T43_historico_do_ash.png` | Aprovado |

### 11.7 Falhas encontradas e correções

| Falha | Onde | Correção |
|---|---|---|
| Os golpes Bite, Gust, Karate Chop e Sand Attack vinham com os tipos atuais (Sombrio, Voador, Lutador, Terrestre) | Conversão da API | Passou a usar `past_values` (tipo e poder da Gen 1) |
| Magnemite vinha como Elétrico/Aço e Clefairy como Fada | Conversão da API | Passou a usar `past_types` e a ignorar tipos novos |
| O app fechava ao abrir (tela preta no LDPlayer): o APK saiu corrompido (`classes13.dex`, "Zlib error"), e o log mostrou `NoClassDefFoundError` | Empacotamento do build | Compilação limpa (`clean assembleDebug`) e conferência da integridade do APK |
| No Android 15, a barra superior ficava por baixo do relógio e dos ícones do sistema | Tema | `windowOptOutEdgeToEdgeEnforcement` no tema |
| Os campos Atacante, Defensor e Golpe saíam deformados (borda fina, letra grande) | `fragment_calculadora.xml` | Estilo `OutlinedBox.ExposedDropdownMenu` para os campos com autocompletar |
| Na execução completa dos 43 casos no emulador não apareceu nenhuma falha nova no app | — | — |
