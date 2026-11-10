# Guia dos frames do ogro

O app troca a silhueta provisória pelas imagens assim que elas estiverem na pasta. Não precisa mexer em código.

## Formato

| Item | Valor |
|---|---|
| Pasta | `app/src/main/assets/titan/` |
| Nome | `push_01.webp`, `push_02.webp`, até `push_24.webp` |
| Formato | WebP com fundo transparente |
| Tamanho | 720 × 1600 px |
| Quantidade | 24 é a referência. O app usa quantas existirem |

## Enquadramento

- 1 sequência só, com o ogro vindo da direita e empurrando para a esquerda. O app espelha para o outro lado.
- As palmas das mãos encostam na borda esquerda da imagem, na mesma altura em todos os quadros. Essa borda é a divisa entre as telas.
- Os pés encostam na borda de baixo.
- Câmera, escala e luz iguais em todos os quadros. Só a pose muda.
- Personagem original: ogro de fantasia, pele verde-acinzentada, muito musculoso. Não usar personagem existente como base.

## Poses

| Quadros | Pose |
|---|---|
| 01 a 04 | Entra em cena. Corpo ainda perto da borda, braços bem dobrados, mãos chegando na borda |
| 05 a 08 | Apoia as mãos. Palmas abertas na borda, cotovelos dobrados, joelhos flexionando |
| 09 a 16 | Inclina e empurra. Tronco inclinando para a frente, ombros subindo, rosto de esforço crescente |
| 17 a 24 | Braços estendendo até ficarem retos no quadro 24, perna de trás esticada, esforço máximo |

A passagem de um quadro para o seguinte precisa ser pequena e contínua, porque o dedo pode parar em qualquer quadro e voltar.

## Como testar

1. Coloque os arquivos na pasta.
2. Faça o commit no GitHub. O build gera um APK novo.
3. Instale por cima do anterior.
