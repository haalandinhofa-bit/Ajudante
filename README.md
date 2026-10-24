# O Ajudante — Fabric 1.20.1

Mod multiplayer para Minecraft Java 1.20.1 (Fabric), versão **1.0.1**.

## Compilar
Requer Java 17 e Gradle 8.6. Execute `gradle build`. O JAR será gerado em `build/libs/`.

## Instalar e testar
Instale Fabric Loader para Minecraft 1.20.1 e Fabric API 0.92.2+1.20.1 no cliente e no servidor. Copie o JAR do mod para a pasta `mods` de cada um.

## Mecânicas da versão 1.0.1
- O Ajudante tenta aparecer perto de um jogador a cada **8–15 minutos**.
- Uma barra no topo da tela mostra a contagem regressiva. Ela fica oculta enquanto o Ajudante está presente e reinicia quando o encontro termina.
- O jogador digita um ID de item, por exemplo `minecraft:diamond`.
- A chance de aceitar depende da raridade padrão do item: **Comum 50%**, **Incomum 35%**, **Raro 15%**, **Épico 5%**.
- Se recusar, toca um som sombrio, aplica Lentidão/Cegueira e causa dano mágico.

## Limitações conhecidas
- O Ajudante usa o modelo de aldeão vanilla.
- O som assustador usa um efeito vanilla, sem arquivo de áudio personalizado.
- A recusa causa efeitos e dano, mas não é uma morte instantânea.
- A raridade é a raridade padrão definida pelo Minecraft para o item; mods de itens podem usar categorias próprias.
