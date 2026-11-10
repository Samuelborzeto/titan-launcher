# Titan Launcher

Launcher Android em que um ogro empurra e segura a tela enquanto você desliza entre as páginas da tela inicial.

## Instalar

1. Abra a página de [Releases](https://github.com/Samuelborzeto/titan-launcher/releases) no celular.
2. Baixe o `TitanLauncher.apk` da versão mais recente e instale.
3. Abra o Titan Launcher, segure o dedo numa área vazia da tela inicial e toque em **Definir como launcher padrão**.

## Usar

| Gesto | O que faz |
|---|---|
| Deslizar para os lados | Troca de página, com o ogro empurrando |
| Deslizar para cima | Abre a gaveta de aplicativos |
| Segurar numa área vazia | Abre os ajustes |
| Engrenagem na gaveta | Abre os ajustes |

## Imagens do personagem

Sem imagens na pasta `app/src/main/assets/titan/`, o app mostra uma silhueta provisória. O formato e as poses estão em [docs/guia-frames.md](docs/guia-frames.md).

## Roteiro de teste no celular

1. Deslizar devagar para a esquerda e para a direita.
2. Deslizar rápido.
3. Parar o dedo no meio e manter parado.
4. Ir e voltar sem soltar o dedo.
5. Soltar antes da metade e depois da metade.
6. Tentar deslizar além da primeira e da última página.
7. Desligar o personagem nos ajustes e trocar de página.
8. Com imagens na pasta: trocar 1 delas por um arquivo inválido e conferir que o app continua abrindo.
