# Rotas a pé sem internet

Em Mapa, consulte uma leitura recente do GPS, escolha uma unidade de saúde e toque em **Traçar rota a pé sem internet**. O cálculo usa a malha que acompanha o app. O caminho aparece em azul e a distância exibida cobre o trecho na malha. O aplicativo informa separadamente a distância entre cada ponta e o trecho cadastrado.

A primeira malha cobre um recorte urbano de Ribeirão Preto entre as latitudes −21,196 e −21,166 e as longitudes −47,820 e −47,790. O cadastro de saúde continua municipal. Uma unidade fora do recorte ou sem caminho conectado não recebe uma rota. A versão atual fornece um traçado estimado para caminhada. Orientação curva a curva, acesso para veículos e interdições em tempo real são etapas separadas. A malha pode ter degraus e não representa uma rota acessível.

O arquivo [ribeirao-caminhada.odbl](../android/app/src/main/assets/ribeirao-caminhada.odbl) contém a base derivada de © OpenStreetMap contributors, sob a [ODbL 1.0](https://opendatacommons.org/licenses/odbl/1-0/). Ele pode ser regenerado com `python android/tools/build_ribeirao_walk_graph.py extrato.osm arquivo.odbl`. O extrato original foi obtido pelo endpoint `/api/0.6/map` do OpenStreetMap com o recorte acima. O gerador filtra acesso explícito, barreiras e vias principais sem indicação de acesso a pé ou calçada. A atribuição também aparece no app. [Créditos do OpenStreetMap](https://www.openstreetmap.org/copyright)

Validação automatizada no núcleo para trajetos direcionais, distância, ausência de conexão e pontos fora da cobertura. A validação em campo continua pendente.
