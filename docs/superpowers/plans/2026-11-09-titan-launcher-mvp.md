# Titan Launcher MVP: plano de implementação

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Objetivo:** launcher Android em que um ogro original empurra e segura a tela, sincronizado ao dedo, na troca de páginas da tela inicial.

**Arquitetura:** app de 1 módulo em Kotlin e Jetpack Compose. A troca de páginas usa `HorizontalPager`. Uma função pura (`computePushState`) converte o estado do pager em estado do ogro, e uma camada (`TitanLayer`) desenha o quadro correspondente por cima das páginas. Os quadros vêm de imagens em `assets/titan/` ou, com a pasta vazia, de uma silhueta desenhada em código.

**Tecnologias:** Kotlin 2.0.20, Android Gradle Plugin 8.5.2, Gradle 8.9, Compose BOM 2024.09.00, DataStore Preferences 1.1.1, JUnit 4.13.2, GitHub Actions.

**Especificação:** `docs/superpowers/specs/2026-11-09-titan-launcher-design.md`

## Regras gerais

- Pacote `com.titanlauncher`. `minSdk 26`, `targetSdk 35`, `compileSdk 35`.
- Personagem original: ogro de fantasia, pele verde-acinzentada. Nenhuma referência a personagem existente em código, nomes ou imagens.
- Cores da interface: preto `#0B0B0F`, grafite `#1C1C24`, roxo `#8B5CF6`.
- Tela inicial: 3 páginas, grade 4 × 5, barra fixa com 4 atalhos.
- Quadros: `assets/titan/push_NN.webp`, 720 × 1600 px, sequência única empurrando para a esquerda, espelhada pelo app para o outro lado.
- Com a tela parada, a camada do ogro não é composta nem animada.
- Compilação e testes rodam no GitHub Actions. O ambiente de escrita do código não tem acesso aos repositórios do Android, então cada tarefa é verificada pelo resultado do workflow depois do push.
- Textos da interface em português do Brasil.

## Pontos de atenção na revisão

1. Deslize rápido que atravessa 2 páginas sem soltar: o ogro segue a transição em curso, sem pular quadro para trás. Teste em `PushStateTest` (Tarefa 2).
2. Pasta com quadros faltando ou fora de ordem (`push_01`, `push_03`): o app ordena pelo nome e usa os que existem. Teste em `FrameNamesTest` (Tarefa 3).
3. Arquivo de imagem corrompido: esse quadro cai para a silhueta, sem fechar o app. Tratado em `AssetFrameSource` (Tarefa 3), conferido no roteiro manual.
4. App desinstalado enquanto está na grade: o ícone some sem fechar o launcher. Teste de `paginate` com lista menor (Tarefa 4).
5. Personagem desligado nos ajustes no meio de um deslize: a camada some no quadro seguinte e a página continua normal. Roteiro manual (Tarefa 6).

---

### Tarefa 1: Projeto base e build automático

**Arquivos:**
- Criar: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, `.gitignore`
- Criar: `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`
- Criar: `app/src/main/java/com/titanlauncher/MainActivity.kt`, `app/src/main/java/com/titanlauncher/ui/Theme.kt`
- Criar: `app/src/main/res/values/strings.xml`, `app/src/main/res/values/themes.xml`, ícone do app em `res/mipmap-anydpi-v26/`
- Criar: `.github/workflows/build.yml`

**Interfaces:**
- Produz: `TitanTheme(content: @Composable () -> Unit)` e o objeto `TitanColors { Black, Graphite, Purple }` em `ui/Theme.kt`.

- [ ] **Passo 1:** escrever os arquivos Gradle com as versões da seção Tecnologias. O `gradle-wrapper.jar` não é versionado: o workflow usa `gradle/actions/setup-gradle@v4` com `gradle-version: 8.9` e chama `gradle` direto.
- [ ] **Passo 2:** `AndroidManifest.xml` com `MainActivity` declarada como tela inicial: `intent-filter` com `MAIN`, `LAUNCHER`, `HOME` e `DEFAULT`; `launchMode="singleTask"`, `stateNotNeeded="true"`, tema sem barra de título e com papel de parede escuro. Declarar `<queries>` para `MAIN` + `LAUNCHER`.
- [ ] **Passo 3:** `MainActivity` mostrando uma tela vazia com `TitanTheme`. O botão voltar não fecha a activity.
- [ ] **Passo 4:** `build.yml`: em todo push roda `gradle testDebugUnitTest assembleDebug` com JDK 17 e publica o APK como artefato. Em tag `v*`, cria a Release com o `app-debug.apk` renomeado para `TitanLauncher.apk`. O APK de debug usa uma chave fixa versionada em `app/debug.keystore`, para as atualizações instalarem por cima.
- [ ] **Passo 5:** commit. Verificação: workflow verde depois do primeiro push.

### Tarefa 2: Estado do ogro (lógica pura)

**Arquivos:**
- Criar: `app/src/main/java/com/titanlauncher/titan/PushState.kt`
- Teste: `app/src/test/java/com/titanlauncher/titan/PushStateTest.kt`

**Interfaces:**
- Produz:

```kotlin
enum class PushSide { FromRight, FromLeft }

data class PushState(
    val visible: Boolean,
    val side: PushSide,
    val progress: Float,   // 0f..1f
    val holding: Boolean,
) { companion object { val Hidden: PushState } }

/** offset: página atual + fração, ex. 0.35 = 35% do caminho da página 0 para a 1.
 *  settledPage: página em repouso antes do gesto. */
fun computePushState(
    offset: Float, settledPage: Int, pageCount: Int,
    dragging: Boolean, velocityPxPerSec: Float,
): PushState

fun frameIndex(progress: Float, frameCount: Int): Int
```

- Regras: `progress = |offset − settledPage|` limitado a 0..1. `offset > settledPage` dá `FromRight`, menor dá `FromLeft`. `visible` é falso quando `progress < 0.001f`. `holding` é verdadeiro quando `dragging` e `|velocityPxPerSec| < 40f`. `frameIndex = round(progress × (frameCount − 1))`, e 0 quando `frameCount <= 1`.

- [ ] **Passo 1: escrever os testes**

```kotlin
@Test fun parado_fica_oculto() = assertFalse(computePushState(1f, 1, 3, false, 0f).visible)
@Test fun deslize_para_esquerda_vem_da_direita() {
    val s = computePushState(1.35f, 1, 3, true, 900f)
    assertEquals(PushSide.FromRight, s.side); assertEquals(0.35f, s.progress, 0.001f); assertFalse(s.holding)
}
@Test fun deslize_para_direita_vem_da_esquerda() {
    val s = computePushState(0.8f, 1, 3, true, -900f)
    assertEquals(PushSide.FromLeft, s.side); assertEquals(0.2f, s.progress, 0.001f)
}
@Test fun dedo_parado_no_meio_segura() = assertTrue(computePushState(1.5f, 1, 3, true, 10f).holding)
@Test fun soltar_nao_segura() = assertFalse(computePushState(1.5f, 1, 3, false, 0f).holding)
@Test fun progresso_limitado_a_1_ao_atravessar_2_paginas() =
    assertEquals(1f, computePushState(2.4f, 1, 3, true, 2000f).progress, 0.001f)
@Test fun quadro_segue_progresso() {
    assertEquals(0, frameIndex(0f, 24)); assertEquals(12, frameIndex(0.5f, 24)); assertEquals(23, frameIndex(1f, 24))
}
@Test fun quadro_com_0_ou_1_imagem() { assertEquals(0, frameIndex(0.7f, 0)); assertEquals(0, frameIndex(0.7f, 1)) }
```

- [ ] **Passo 2:** implementar `PushState.kt` conforme as regras.
- [ ] **Passo 3:** commit. Verificação: `testDebugUnitTest` verde no workflow.

### Tarefa 3: Fonte dos quadros e camada do ogro

**Arquivos:**
- Criar: `app/src/main/java/com/titanlauncher/titan/FrameSource.kt` (interface, `AssetFrameSource`, `sortedFrameNames`)
- Criar: `app/src/main/java/com/titanlauncher/titan/PlaceholderOgre.kt`
- Criar: `app/src/main/java/com/titanlauncher/titan/TitanLayer.kt`
- Criar: `app/src/main/assets/titan/.gitkeep`, `docs/guia-frames.md`
- Teste: `app/src/test/java/com/titanlauncher/titan/FrameNamesTest.kt`

**Interfaces:**
- Consome: `PushState`, `PushSide`, `frameIndex` (Tarefa 2).
- Produz:

```kotlin
interface FrameSource {
    val frameCount: Int                       // 0 = usar a silhueta
    fun frame(index: Int): ImageBitmap?       // null = ainda carregando ou inválido
    fun prefetch(center: Int)                 // carrega center−2..center+2 fora da thread principal
}
fun sortedFrameNames(all: List<String>): List<String>   // só push_*.webp, em ordem
class AssetFrameSource(context: Context, scope: CoroutineScope) : FrameSource
fun DrawScope.drawPlaceholderOgre(progress: Float)       // desenhado como se viesse da direita
@Composable fun TitanLayer(state: PushState, frames: FrameSource, modifier: Modifier = Modifier)
```

- [ ] **Passo 1: escrever os testes**

```kotlin
@Test fun ordena_e_filtra() = assertEquals(
    listOf("push_01.webp", "push_03.webp", "push_10.webp"),
    sortedFrameNames(listOf("push_10.webp", "leia.txt", "push_03.webp", ".gitkeep", "push_01.webp")))
@Test fun pasta_vazia() = assertEquals(emptyList<String>(), sortedFrameNames(listOf(".gitkeep")))
```

- [ ] **Passo 2:** `AssetFrameSource`: lista `assets/titan`, decodifica com `BitmapFactory` em `Dispatchers.IO`, guarda em `LruCache` de 5 quadros. Erro de decodificação resulta em `null` para aquele quadro.
- [ ] **Passo 3:** `drawPlaceholderOgre`: silhueta original em formas simples (tronco largo, cabeça baixa, 2 braços com mãos espalmadas na borda esquerda do desenho), pele `#7C8F6B`, contorno `#2A3324`. Os braços vão de dobrados (`progress` 0) a estendidos (`progress` 1) e o tronco inclina até 12° para a frente.
- [ ] **Passo 4:** `TitanLayer`: não desenha nada quando `!state.visible`. Largura do ogro = 45% da largura da tela, altura proporcional a 720 × 1600, alinhado embaixo. Posição X da borda das mãos = divisa entre as páginas: `largura × (1 − progress)` em `FromRight`; em `FromLeft` a camada inteira é espelhada com `scaleX = −1`. Nos primeiros 15% do progresso o ogro entra deslizando de fora da tela; nos últimos 10% sai com `alpha` de 1 a 0. Com `holding`, aplica tremor de ±2 dp a cada 60 ms. Usa `frames.frame(frameIndex(...))` e cai para `drawPlaceholderOgre` quando vier `null` ou `frameCount == 0`.
- [ ] **Passo 5:** `docs/guia-frames.md`: formato, tamanho, nomes, pasta e a pose de cada um dos 24 quadros (1 a 4 entra em cena, 5 a 8 apoia as mãos, 9 a 16 inclina e empurra, 17 a 24 braços estendendo até o fim).
- [ ] **Passo 6:** commit. Verificação: workflow verde.

### Tarefa 4: Apps instalados e tela inicial

**Arquivos:**
- Criar: `app/src/main/java/com/titanlauncher/apps/AppRepository.kt`, `apps/Paginate.kt`
- Criar: `app/src/main/java/com/titanlauncher/home/HomeScreen.kt`, `home/AppIcon.kt`, `home/Dock.kt`
- Modificar: `MainActivity.kt`
- Teste: `app/src/test/java/com/titanlauncher/apps/PaginateTest.kt`

**Interfaces:**
- Consome: `computePushState`, `TitanLayer`, `AssetFrameSource`, `TitanTheme`.
- Produz:

```kotlin
data class AppEntry(val label: String, val packageName: String, val activityName: String)
class AppRepository(context: Context) {
    val apps: StateFlow<List<AppEntry>>      // ordem alfabética, sem o próprio launcher
    fun launch(app: AppEntry)
    fun icon(app: AppEntry): ImageBitmap
    fun dockApps(): List<AppEntry>           // telefone, mensagens, navegador, câmera; ignora os ausentes
}
fun paginate(apps: List<AppEntry>, pages: Int = 3, perPage: Int = 20): List<List<AppEntry>>
@Composable fun HomeScreen(repo: AppRepository, titanEnabled: Boolean, onOpenDrawer: () -> Unit, onOpenSettings: () -> Unit)
```

- [ ] **Passo 1: escrever os testes**

```kotlin
private fun apps(n: Int) = (1..n).map { AppEntry("App $it", "p$it", "a$it") }
@Test fun sempre_3_paginas() = assertEquals(3, paginate(apps(5)).size)
@Test fun enche_20_por_pagina() { val p = paginate(apps(45)); assertEquals(listOf(20, 20, 5), p.map { it.size }) }
@Test fun corta_o_excedente() = assertEquals(60, paginate(apps(100)).sumOf { it.size })
@Test fun lista_menor_apos_desinstalar() = assertEquals(listOf(4, 0, 0), paginate(apps(4)).map { it.size })
```

- [ ] **Passo 2:** `AppRepository`: consulta `queryIntentActivities` com `MAIN` + `LAUNCHER`, atualiza a lista por `BroadcastReceiver` de `PACKAGE_ADDED`, `PACKAGE_REMOVED` e `PACKAGE_CHANGED`. Atalhos da barra resolvidos por `ACTION_DIAL`, `CATEGORY_APP_MESSAGING`, `CATEGORY_APP_BROWSER` e `MediaStore.ACTION_IMAGE_CAPTURE`.
- [ ] **Passo 3:** `HomeScreen`: `HorizontalPager` de 3 páginas com grade 4 × 5, indicador de página, `Dock` fixo embaixo. Toque longo em área vazia chama `onOpenSettings`. Deslize para cima chama `onOpenDrawer`.
- [ ] **Passo 4:** ligar o ogro: dentro de `HomeScreen`, calcular `computePushState` a partir de `pagerState.currentPage + currentPageOffsetFraction`, `pagerState.settledPage`, da interação de arrasto do pager e da velocidade medida entre quadros com `withFrameNanos`. `TitanLayer` fica por cima do pager e só entra na composição quando `titanEnabled && state.visible`.
- [ ] **Passo 5:** commit. Verificação: workflow verde.

### Tarefa 5: Gaveta de aplicativos

**Arquivos:**
- Criar: `app/src/main/java/com/titanlauncher/drawer/AppDrawer.kt`, `drawer/FilterApps.kt`
- Modificar: `MainActivity.kt`
- Teste: `app/src/test/java/com/titanlauncher/drawer/FilterAppsTest.kt`

**Interfaces:**
- Consome: `AppRepository`, `AppEntry`.
- Produz: `fun filterApps(apps: List<AppEntry>, query: String): List<AppEntry>` e `@Composable fun AppDrawer(repo: AppRepository, onClose: () -> Unit)`.

- [ ] **Passo 1: escrever os testes**

```kotlin
private val lista = listOf(AppEntry("Câmera", "c", "c"), AppEntry("Calculadora", "k", "k"), AppEntry("Mapas", "m", "m"))
@Test fun vazio_devolve_tudo() = assertEquals(3, filterApps(lista, "  ").size)
@Test fun ignora_maiusculas_e_acentos() = assertEquals(listOf("Câmera"), filterApps(lista, "camera").map { it.label })
@Test fun busca_no_meio_do_nome() = assertEquals(listOf("Calculadora"), filterApps(lista, "lcul").map { it.label })
```

- [ ] **Passo 2:** `AppDrawer`: painel em tela cheia na cor grafite, campo de busca no topo, grade de 4 colunas com rolagem. Fecha com o botão voltar, com deslize para baixo no topo da lista ou ao abrir um app.
- [ ] **Passo 3:** commit. Verificação: workflow verde.

### Tarefa 6: Ajustes e launcher padrão

**Arquivos:**
- Criar: `app/src/main/java/com/titanlauncher/settings/SettingsStore.kt`, `settings/SettingsScreen.kt`
- Modificar: `MainActivity.kt`
- Criar: `README.md`

**Interfaces:**
- Produz: `class SettingsStore(context: Context) { val titanEnabled: Flow<Boolean>; suspend fun setTitanEnabled(value: Boolean) }` (padrão `true`) e `@Composable fun SettingsScreen(store: SettingsStore, onBack: () -> Unit)`.

- [ ] **Passo 1:** `SettingsStore` com DataStore Preferences, chave `titan_enabled`.
- [ ] **Passo 2:** `SettingsScreen` com 2 itens: chave "Personagem" e botão "Definir como launcher padrão". O botão usa `RoleManager.ROLE_HOME` a partir do Android 10 e `Settings.ACTION_HOME_SETTINGS` nas versões anteriores.
- [ ] **Passo 3:** `MainActivity` alterna entre tela inicial, gaveta e ajustes e passa `titanEnabled` para `HomeScreen`.
- [ ] **Passo 4:** `README.md` com instalação pela Release e o roteiro manual da especificação: deslizar devagar, deslizar rápido, parar no meio, voltar sem soltar, soltar antes e depois da metade, desligar o personagem durante um deslize, colocar 1 imagem corrompida na pasta de quadros.
- [ ] **Passo 5:** commit, tag `v0.1.0`. Verificação: Release criada com `TitanLauncher.apk`.
