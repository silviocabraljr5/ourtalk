# Mapas e orientação — requisito central

Definido pelo usuário em 08/11/2026: a pessoa deve saber onde está e para onde está indo, inclusive sem internet e sem sinal de operadora. Ribeirão Preto será o primeiro cenário de apresentação. Não tratar como recurso secundário.

## Experiência

- Mapa com a posição do usuário, precisão e horário da última localização.
- Locais importantes no próprio mapa, sincronizados com uma lista pesquisável e filtros por categoria. Selecionar um local abre sua ficha e permite definir destino.
- Botão Minha localização, orientação norte/direção, destino visível, distância restante e percurso desenhado.
- Roteamento e recálculo locais, começando por deslocamento a pé, após implementação do motor e validação dos dados. Não presumir que uma linha reta seja uma rota transitável.
- Orientação por voz local quando suportada, sem impedir o uso visual.
- Favoritos e ponto de encontro familiar salvos no aparelho.
- Fichas com fonte, verificação e estado de disponibilidade. Local cadastrado não implica serviço em operação, abrigo oficial ou via segura.

## Pacote offline

A versão interna Android 0.3 inclui o contorno municipal de Ribeirão Preto e um cadastro offline de 31 unidades. A busca funciona por nome, endereço, categoria ou CNES e o filtro municipal destaca quatro UPAs e o pronto atendimento Vila Virgínia. O GPS é opcional, consultado por ação do usuário e distinguido de leituras antigas. A rede de ruas e o cálculo de rotas continuam pendentes.
O cadastro parte do [CNES de 07/11/2026](https://dadosabertos.saude.gov.br/dataset/cnes-cadastro-nacional-de-estabelecimentos-de-saude/resource/cd884664-b378-4c6c-854b-6a08faa25d19). Inclui registros municipais sem motivo de desabilitação dos tipos hospital geral, especializado, hospital-dia, pronto-socorro e pronto atendimento, além das cinco unidades identificadas pela [Prefeitura](https://www.ribeiraopreto.sp.gov.br/portal/saude/relacao-telefones-enderecos). Registros desabilitados ficam fora do diretório. O cadastro é um retrato das fontes e a classificação hospitalar não confirma porta aberta para urgências. A conferência de unidades omitidas, endereços, entradas e disponibilidade em campo segue pendente.
A [malha municipal do IBGE](https://servicodados.ibge.gov.br/api/v3/malhas/municipios/3543402?formato=application%2Fvnd.geo%2Bjson&qualidade=maxima) fornece o contorno. A Prefeitura identifica o endereço da Unidade de Emergência do HC em sua [lista de contatos](https://www.ribeiraopreto.sp.gov.br/portal/saude/telefones-uteis). O marcador dessa unidade vem do [nó 492635628 do OpenStreetMap](https://www.openstreetmap.org/node/492635628), © OpenStreetMap contributors sob [ODbL 1.0](https://opendatacommons.org/licenses/odbl/1-0/). O botão do SAMU abre o discador com [192](https://www.gov.br/saude/pt-br/composicao/saes/samu-192/samu-192).
O JSON inclui apenas dados públicos necessários para encontrar as unidades. A licença MIT cobre o código. Para regenerar o cadastro, execute `python android/tools/build_ribeirao_health_map.py malha.geojson cnes.zip android/app/src/main/assets/ribeirao-saude.json AAAA-MM-DD` com os arquivos das fontes acima.

Baixar previamente cartografia, índice de endereços e locais, dados necessários ao cálculo de rotas, estilos, fontes e ícones. Não depender de mapas, geocodificação ou rotas hospedados externamente para as ações essenciais. Verificar integridade e permitir atualização e gerenciamento de armazenamento.

## Estados e contingências

1. Com internet e localização: mapa local, atualização de dados quando disponível e navegação.
2. Sem internet/operadora, com recepção GNSS: posição e navegação sobre o pacote já salvo; dados e rotas calculados localmente.
3. Sem posição atual: manter mapa, busca e destino; mostrar última posição com horário, sem apresentá-la como atual. Permitir marcar ponto de partida manualmente e consultar percurso.
4. Sem bússola confiável: manter norte fixo; usar direção de deslocamento somente quando puder ser estimada. Não exibir seta de orientação com confiança falsa.
5. Sem pacote: informar que os mapas da região não foram baixados. GPS não cria um mapa nem uma base de rotas.
6. Sem bateria/aparelho operacional: aplicativo não funciona. Preparação energética e alternativas externas são necessárias.

Prédios, túneis, interiores e interferência podem comprometer localização. Não garantir operação em todas as circunstâncias. Trânsito, bloqueios recentes, inundação e disponibilidade de locais não podem ser conhecidos offline sem novas informações.

## Privacidade

Pedir localização no momento de uso. A posição fica no aparelho por padrão; seguir um destino não publica nem transmite localização a contatos. Compartilhamento é ação explícita e separada. Acesso em segundo plano deve ter justificativa e autorização próprias.

## Critérios de aceite

Em aparelho real, baixar o pacote e retirar a conectividade de dados/operadora mantendo GNSS disponível. Confirmar mapa e pontos, busca local, posição com precisão, seleção de destino, cálculo e recálculo sem chamadas de rede obrigatórias. Testar perda de GPS, recusa de permissão, saída da região baixada e consumo de bateria. Só anunciar navegação offline como operacional após esses testes.

## Estado atual

O protótipo web usa um iframe online do OpenStreetMap e telas demonstrativas. A implementação offline descrita acima pertence ao aplicativo Android interno.
