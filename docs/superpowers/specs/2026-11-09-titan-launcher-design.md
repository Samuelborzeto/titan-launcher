# Titan Launcher: especificação do MVP

Data: 09/11/2026

## Objetivo

Launcher Android em que um ogro original empurra e segura a tela enquanto o usuário desliza entre as páginas da tela inicial. O MVP existe para validar essa mecânica no celular. O restante do launcher entra só no mínimo necessário para usar o app como tela inicial.

O personagem é uma criação original: ogro de fantasia, pele verde-acinzentada, muito musculoso. Não reproduz nenhum personagem existente.

## O que entra no MVP

1. Tela inicial com 3 páginas, grade de 4 colunas por 5 linhas e uma barra fixa com 4 atalhos (telefone, mensagens, navegador, câmera).
2. Ícones preenchidos automaticamente com os apps instalados, em ordem alfabética.
3. Gaveta de aplicativos, aberta deslizando para cima, com busca por nome.
4. Abrir qualquer app instalado.
5. Definir o Titan como launcher padrão, com botão que leva à tela do Android para isso.
6. Personagem sincronizado ao dedo na troca de páginas.
7. Tela de ajustes com 1 opção: ligar ou desligar o personagem.
8. Visual escuro em preto, grafite e roxo.

## O que fica para depois

Ogro levantando a gaveta de apps ao deslizar para cima, ogro reagindo ao toque em um ícone antes de o app abrir (cada um com sua própria sequência de imagens), papel de parede, personalização de cores, organização de ícones por arrastar, pastas, widgets, ajuste de intensidade das animações, expressões faciais e efeitos de luz e sombra extras.

## Mecânica do personagem

A troca de páginas usa o deslize nativo do Compose (HorizontalPager). Ele informa, a cada quadro da tela, quanto da transição já aconteceu, de 0 a 1. Todo o comportamento do ogro sai desse valor.

| Situação | Comportamento |
|---|---|
| Dedo desliza para a esquerda | O ogro entra pela direita, com as mãos na borda da página que está saindo, e empurra para a esquerda |
| Dedo desliza para a direita | O mesmo, espelhado: entra pela esquerda e empurra para a direita |
| Dedo avança ou recua | O quadro da animação avança ou recua junto, na mesma proporção |
| Dedo parado no meio | O ogro fica no quadro atual com um tremor leve de esforço |
| Dedo solta | A página conclui ou reverte pela distância e velocidade do gesto, e o ogro acompanha até o fim |
| Transição termina | O ogro sai de cena pelo lado de onde veio |
| Primeira ou última página, sem página seguinte | O ogro não aparece |

Posição na tela: as mãos do ogro ficam coladas na divisa entre a página que sai e a que entra. O corpo fica por cima da página que está entrando. Quando a divisa anda, o ogro anda junto.

Escolha do quadro: com N quadros disponíveis, o quadro exibido é o de número `arredondar(progresso × (N − 1))`.

## Frames do personagem

- 1 sequência só, do ogro empurrando para a esquerda. O app espelha para o outro lado.
- Cerca de 24 imagens. O app usa quantas existirem na pasta.
- Formato WebP com fundo transparente, 720 × 1600 px, mãos encostadas na borda esquerda da imagem.
- Nome dos arquivos: `push_01.webp`, `push_02.webp`, em sequência.
- Pasta: `app/src/main/assets/titan/`.
- Sequência de poses: entra em cena, apoia as mãos, inclina o corpo, empurra com esforço crescente, braços estendidos no final.
- Um guia com a pose de cada quadro acompanha o projeto em `docs/guia-frames.md`.

Enquanto a pasta estiver vazia, o app mostra uma silhueta provisória de ogro desenhada em código, com braços que se estendem conforme o progresso. Assim a mecânica pode ser testada antes de os frames existirem.

## Estrutura do código

Pacote `com.titanlauncher`. Android mínimo 8.0 (API 26). Kotlin e Jetpack Compose.

| Parte | Responsabilidade | Depende de |
|---|---|---|
| `apps/AppRepository` | Lista os apps instalados, abre um app, atualiza a lista quando um app é instalado ou removido | Android PackageManager |
| `home/HomeScreen` | Páginas, grade de ícones, barra de atalhos | AppRepository |
| `drawer/AppDrawer` | Gaveta com lista e busca | AppRepository |
| `titan/PushState` | Converte o estado do deslize em: lado, progresso, dedo parado ou não, visível ou não. Lógica pura, sem tela | Nada |
| `titan/FrameSource` | Entrega o desenho de um quadro. 2 versões: imagens da pasta e silhueta provisória | Pasta de assets |
| `titan/TitanLayer` | Desenha o ogro por cima das páginas a partir do PushState e do FrameSource | PushState, FrameSource |
| `settings/Settings` | Guarda a opção ligar ou desligar e a tela de ajustes | DataStore |

## Desempenho e bateria

- A camada do ogro só existe durante o deslize. Com a tela parada, nada é desenhado nem animado.
- As imagens são carregadas fora da linha principal do app, e ficam na memória apenas o quadro atual e os vizinhos.
- O desenho acompanha a taxa de atualização da tela (60, 90 ou 120 Hz) sem código específico, porque é feito a cada quadro do sistema.
- Tamanhos calculados em proporção à tela, para funcionar em qualquer resolução.

## Testes

- Testes automáticos do `PushState`: lado correto em cada direção, quadro correto para cada progresso, detecção de dedo parado, ogro oculto nas pontas.
- Teste automático da escolha de fonte dos quadros: pasta vazia usa a silhueta, pasta com imagens usa as imagens.
- Roteiro manual no celular: deslizar devagar, deslizar rápido, parar no meio, voltar sem soltar, soltar antes e depois da metade, desligar o personagem nos ajustes.

## Entrega

Repositório no GitHub com build automático (GitHub Actions) que gera o APK e publica na Release, para instalar direto no celular.
