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

O protótipo web usa um iframe online do OpenStreetMap e telas demonstrativas. Não possui motor de navegação, posição GNSS real, mapas baixados nem roteamento offline. A experiência descrita neste documento é um requisito de desenvolvimento, não uma capacidade entregue.
