# Entregas e gates do OurTalk

Atualizado em 08/11/2026. Este arquivo organiza o projeto completo sem confundir código implementado, validação e lançamento.

| Área | Estado atual | Gate para próxima entrega |
|---|---|---|
| Site, identidade, Instagram e apoio | Site publicado; marcas originais disponíveis aplicadas | Receber artes oficiais de marcas faltantes |
| Doações | Pix com valor; arrecadação confirmada mantida em zero | Conciliação de recebimentos autenticada, persistência e idempotência; nunca contar Pix gerado |
| Cadastro de lançamento | Captura consentida por e-mail/WhatsApp na hospedagem | Provedor de envio, descadastro e campanha só após download oficial |
| Calculadora energética | Cálculos W/Wh/Ah, perdas, reserva e solar com testes | Revisão de hipóteses por cenário e dados reais dos equipamentos |
| Android local | Biblioteca, busca offline, checklist, contatos, grupos e históricos locais | Evoluir armazenamento e acessibilidade com testes físicos |
| Proteção local | AES-GCM/Android Keystore e migração implementados | Testes, revisão independente e estratégia de recuperação/exportação |
| Conversa próxima individual | Nearby, opt-in, comparação de código e ACK implementados experimentalmente no Android | Testes físicos Android ↔ Android e Android ↔ iPhone, sem internet, com permissões negadas e perda de alcance |
| Interoperabilidade Android e iOS | Primeira distribuição pública no Android. Contrato compartilhado definido em [interoperabilidade](docs/interoperabilidade.md) | Cliente iOS de teste e validação bidirecional em aparelhos reais |
| Contatos de rede e bloqueio | Referências locais e consentimento por sessão | Identidade persistente verificada, vinculação a contato e bloqueio persistente sem confiar em apelidos |
| Convite pessoal de contato | Android e serviço de convites com exclusão e limpeza de registros expirados | Configurar a limpeza periódica no ambiente de produção, revisar Segurança dos dados no Play Console e validar a instalação e a sincronização pela Google Play. |
| Grupos de rede | Grupos locais somente | Protocolo revisado, convites/aceite, gestão/rotação de chaves, saída e remoção de membros |
| Comunicação por internet | Pendente | Backend autenticado, protocolo de mensagens revisado, abuso/limites, observabilidade sem conteúdo e operação segura |
| Mapa Ribeirão Preto e rotas offline | Cadastro municipal, busca offline, GPS e cálculo de rota a pé em um recorte urbano | Testes de campo, ampliar a malha, atualizar o cadastro e implementar orientação curva a curva |
| Clima e comunicados | Previsão e avisos do INMET para Ribeirão Preto, com fonte, horário, validade e cópia offline | Validar em aparelho, ampliar a cobertura e integrar comunicados de outras fontes e notificações |
| Pedir ajuda | Preparo local de mensagem, GPS opcional, seletor de compartilhamento do Android e atalho para o discador | Validar em aparelhos e integrar pedido e confirmação dentro do OurTalk |
| Ficha pessoal de saúde | Cadastro local criptografado de alergias, medicamentos, observações e contato de emergência | Validar a tela e o discador em aparelho |
| Cofre de documentos | Importação, exportação e exclusão de PDFs e imagens com armazenamento local criptografado | Validar a integridade dos arquivos e os seletores em aparelho |
| TalK | Busca por palavras em guias incluídos | Modelo/recursos do aparelho, fontes, revisão de segurança e avaliação sem inventar orientações |
| Google Play | AAB de preparação sem chave de produção | Funções validadas, assinatura, política, Data Safety, conta/testadores e revisão da loja |

## Ordem de trabalho

1. Fechar a validação do transporte próximo e da proteção local. Corrigir falhas antes de depender da comunicação.
2. Definir identidade persistente e protocolo para contatos/grupos, sem transformar grupos locais em grupos de rede silenciosamente.
3. Implementar pacote de mapa/POIs e roteamento offline de Ribeirão Preto com procedência e atualização.
4. Conectar transporte pela internet, clima/comunicados e evolução do TalK, cada um com modelo de ameaças e testes próprios.
5. Ensaios físicos integrados, acessibilidade, energia, privacidade, auditoria e teste interno/fechado da Play Store.

Não há garantia de funcionar sem bateria, sem rádio compatível, sem satélites ou sem dados cartográficos previamente salvos. Uma sessão próxima usa alcance físico; não cria comunicação ilimitada ou retransmissão automática.
