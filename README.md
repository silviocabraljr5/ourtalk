# OurTalk — Site de apresentação

Site estático responsivo em português, preparado para publicação na VPS. Arquivos públicos em `dist/`. Não requer instalação, compilação ou banco de dados.

## Prévia local

Execute `python -m http.server 8080 --directory dist` e abra http://localhost:8080.

## Publicação na VPS

Cadastre `ourtalk.com.br` e `www.ourtalk.com.br` no servidor web, confira os registros DNS e copie o conteúdo de `dist/` para a raiz pública exclusiva desse domínio. Ative HTTPS e redirecione HTTP para HTTPS. Não sobreponha diretórios de outros projetos. O site não foi publicado na VPS nesta entrega.

## Doações

Pix informado e autorizado pelo responsável do projeto: CNPJ `32.928.642/0001-41`, conta OurWorld. O botão copia a chave; a transferência ocorre no aplicativo do banco. A titularidade bancária não foi verificada por consulta ao banco. Nenhum dado financeiro é coletado pelo site. Não há contador ou relatório fictício de arrecadação.

## Marca e situação

A referência visual foi recuperada diretamente da conversa original no Chrome em 08/11/2026: `dist/assets/ourtalk-app-original.png`. A arte usa fundo preto/azul profundo, branco e verde-lima, com a marca em balão de conversa. O site utiliza essa arte original na apresentação e sua marca por enquadramento CSS, sem redesenhar o símbolo. Ainda é recomendável obter o arquivo vetorial independente da logo. A proposta anterior de mãos/coração e a primeira interpretação clara deste site foram superadas.

As funcionalidades do aplicativo são explicitamente apresentadas como planejadas. O simulador energético funciona localmente com valores ilustrativos e hipóteses exibidas. Não há formulário de cadastro, analytics ou rastreamento publicitário. Fontes Google são carregadas externamente; há fallback local. Para auto-hospedar, adicionar os arquivos licenciados da fonte e remover o @import de styles.css.

## GitHub

Repositório criado: https://github.com/silviocabraljr5/ourtalk, com visibilidade privada inicial até definição da licença. O histórico privado dos chats não faz parte do repositório. Não foi escolhida licença de código automaticamente; a intenção do produto é ser de código aberto. O endereço deve ser divulgado no site quando houver abertura pública.

## Conteúdo

- Apresentação, propósito e princípios.
- Seis grupos de recursos e limites de funcionamento.
- Apresentação individual dos dez módulos e das três frentes do projeto.
- Experiência móvel redesenhada em 390 × 844: duas ações principais com 58 px de altura, texto de corpo de 14–15 px, navegação Início/Conversas/Recursos e conteúdo secundário fora da tela inicial. Protótipo isolado em `dist/app-demo.html`; nenhum pedido de ajuda ou mensagem é enviado.
- Atalhos Cidade/Bíblia/Saúde/Cofre/Preparar e painel alternável Tempo/Comunicados. Os dados meteorológicos são ilustrativos; não existe integração com serviços de previsão.
- Fluxos conceituais de adição de pessoa por QR Code, convite ou proximidade autorizada. Nenhuma descoberta, leitura de câmera ou identidade real é executada. A tela de privacidade descreve requisitos planejados, incluindo aceite de convites, bloqueio e criptografia a implementar e auditar.
- Recursos com busca e filtros; atalhos iniciais IA, Mapas e Locais. Conversas distingue Meus contatos e Por perto. O convite de divulgação compartilha o domínio de apresentação e não adiciona automaticamente um contato.
- Ribeirão Preto é a cidade demonstrativa. O mapa usa iframe online do OpenStreetMap, não um pacote offline. HC Campus e Parque Curupira têm links de fontes oficiais; disponibilidade e atendimento não são confirmados. Fontes consultadas em 08/11/2026.
- Simulador de energia com validação dos limites dos campos.
- Roadmap sem prazos inventados.
- Pix com identificação da conta recebedora.
- Transparência, dúvidas e informação sobre privacidade.

Antes da publicação final, verificar o Pix em um aplicativo bancário. Relatórios reais poderão ser acrescentados quando disponíveis. Bíblia opcional e previsão previamente salva foram incorporadas ao escopo; a Bíblia depende de tradução licenciada e a previsão deve exibir atualização e validade.
