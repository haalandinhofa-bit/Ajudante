# O Ajudante — Fabric 1.20.1

Mod multiplayer para Minecraft Java 1.20.1 (Fabric).

## Compilar
Requer Java 17 e Gradle 8.6. Execute `gradle build`. O JAR será gerado em `build/libs/`.

## Testar
Instale Fabric Loader para Minecraft 1.20.1 e Fabric API 0.92.2+1.20.1 no cliente e no servidor. Copie o JAR do mod para a pasta `mods`.

## Mecânica inicial
- Um aldeão chamado **O Ajudante** aparece perto de um jogador a cada 10–20 minutos.
- O jogador recebe uma GUI e pode digitar um ID como `minecraft:diamond`.
- O servidor valida o ID e decide com 50% de chance se entrega o item.
- Na recusa, toca um som sombrio, aplica Lentidão/Cegueira e causa dano mágico.

## Limitações desta primeira versão
- A aparência é a do aldeão vanilla com nome especial.
- O som assustador usa um efeito vanilla; não inclui áudio personalizado.
- A punição causa dano e efeitos, mas não mata instantaneamente.
- O aparecimento é global por servidor e a entidade é visualmente um aldeão vanilla.
