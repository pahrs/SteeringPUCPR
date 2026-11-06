# Steering Behaviors

Trabalho prático de Steering Behaviors, feito sobre o template em Java da professora. Três tipos de carro se movem em um mapa com obstáculos e um circuito, e cada tipo combina comportamentos diferentes.

## Como rodar

É necessário Java 17 ou mais novo.

```
javac -d out $(find src -name "*.java")
java -cp out cars.engine.Window
```

Também é possível abrir a pasta `src` em uma IDE e executar a classe `cars.engine.Window`.

Controles:
- **Clique**: define o ponto de chegada do carro vermelho.
- **Mouse**: os carros verdes fogem do cursor.
- **Tecla D**: liga e desliga as setas de debug (azul é a velocidade, laranja é a força de steering).

## Como o movimento funciona

A cada quadro, cada carro calcula uma força. A engine limita essa força a `maxForce`, usa ela para mudar a velocidade (limitada a `maxSpeed`) e depois atualiza a posição.

Quase todos os comportamentos usam a mesma ideia: calcular a velocidade que o carro deseja ter e devolver `desejada - velocidade atual`. Dessa forma, o carro corrige o que já está fazendo e faz curvas suaves, em vez de mudar de direção de uma vez.

## Comportamentos

**Seek.** O carro vai até um ponto. A velocidade desejada aponta para o ponto, com módulo `maxSpeed`.

**Flee.** É o contrário do Seek: o carro foge de um ponto. A fuga só acontece dentro de um raio de pânico, já que não faz sentido fugir de algo muito distante.

**Arrive.** É um Seek que desacelera perto do ponto. Dentro de um raio de desaceleração, a velocidade desejada diminui até zero, então o carro para no ponto sem passar dele.

**Wander.** O carro vagueia sem destino. Existe um círculo imaginário à frente dele, e um ponto desse círculo é o alvo. A cada quadro, esse ponto muda um pouco de lugar ao acaso, o que gera um zigue-zague suave.

**Obstacle Avoidance.** O carro olha para frente, e a distância de observação aumenta com a velocidade. Se um obstáculo está no caminho, o carro recebe uma força para o lado, oposto ao do obstáculo. A força é lateral, e não "para longe do centro", já que em um choque de frente a segunda só freia o carro e não o desvia.

**Path Following.** O carro prevê onde estará em 0.2 s, acha o ponto mais próximo do circuito e escolhe um ponto 40 pixels à frente dele. Depois faz um Seek nesse ponto. A previsão faz o carro começar a virar antes das curvas.

**Separation** (extra, usado pelo rebanho). O carro recebe uma força para longe dos vizinhos próximos, e ela é mais forte quanto mais perto o vizinho está.

## Combinações

Cada tipo de carro soma as forças dos seus comportamentos, e cada força é multiplicada por um peso antes da soma. O peso define o quanto aquele comportamento influencia o carro.

### Comportamento de rebanho (verde, 6 carros): Wander + Separation + Flee + Obstacle Avoidance

| Comportamento | Peso |
|---|---|
| Wander | 0.6 |
| Separation | 2.0 |
| Flee (do mouse, raio de pânico 150) | 1.2 |
| Obstacle Avoidance | 3.0 |

Velocidade máxima 100. Os carros passeiam, se mantêm afastados uns dos outros e fogem do cursor.

- O Wander tem o menor peso porque é o comportamento de fundo. Se fosse alto, taparia os outros.
- A Separation tem peso alto porque dois carros de frente se aproximam rápido. Com peso e raio menores, os carros chegaram a se sobrepor nos testes.
- O Flee vence o Wander, para o carro fugir do mouse, porém fica abaixo do desvio, já que fugir não pode causar uma batida.

### Comportamento de caçador(Ao clique) (vermelho): Arrive + Wander + Obstacle Avoidance

| Comportamento | Peso |
|---|---|
| Arrive (no ponto do clique, raio de desaceleração 150) | 1.0 |
| Wander (só antes do primeiro clique) | 0.6 |
| Obstacle Avoidance | 3.0 |

Velocidade máxima 220 e força máxima 450. Antes do clique, o carro vagueia. Depois, vai até o ponto, desacelera e para, desviando dos obstáculos no caminho.

- Foi usado o Arrive, e não o Seek, porque o Seek passaria do ponto e ficaria girando em volta dele.
- O Wander só é usado antes do clique, já que com um destino ele só atrapalharia a chegada.
- A força máxima é maior porque o carro é mais rápido. Quanto maior a velocidade, mais força é necessária para curvar no mesmo espaço.

### Piloto (azul): Path Following + Obstacle Avoidance

| Comportamento | Peso |
|---|---|
| Path Following | 1.0 |
| Obstacle Avoidance | 3.0 |

Velocidade máxima 140 e força máxima 450. O carro dá voltas no circuito. Dois obstáculos foram colocados em cima do circuito, de propósito, para o carro sair do caminho, contornar e voltar.

- A velocidade e a força foram ajustadas para o carro conseguir fazer as curvas do circuito sem cortar demais as quinas.

## Pesos

O desvio de obstáculos tem o maior peso (3.0) porque bater em um obstáculo é o pior resultado. Como a soma das forças é cortada em `maxForce`, esse peso faz o desvio dominar a soma quando há risco de colisão. Quando não há obstáculo por perto, a força de desvio vale zero e não atrapalha. Os outros comportamentos ficam entre 0.6 e 2.0, e se todos os pesos fossem altos, a soma seria cortada e se perderia nuance.

## Limitações

- O circuito é sempre fechado.
- Os obstáculos são sempre círculos.
- Se o clique for dentro de um obstáculo, o carro vermelho para na borda dele.
