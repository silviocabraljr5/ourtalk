# OurTalk

Projeto de comunicação, informação e apoio entre pessoas, pensado para preparação comunitária e momentos de falha de conexão.

## Estágio atual

Site publicado, protótipo navegável, cadastro de lançamento, calculadora de energia e Pix Copia e Cola. Aplicativo Android interno em desenvolvimento: biblioteca/checklist offline, contatos/grupos locais e conexão próxima experimental. Não há lançamento público na Play Store. Navegação offline, grupos de rede, transporte por internet e TalK com IA continuam pendentes. Consulte [android/README.md](android/README.md) e [ROADMAP.md](ROADMAP.md).

## Contribuir

Leia [CONTRIBUTING.md](CONTRIBUTING.md). Contribua com código, design, testes, documentação, acessibilidade e ideias. Use issues para sugestões e problemas e pull requests para alterações.

## Executar

Use Node.js 22 ou superior. Não há dependências externas nem etapa de compilação.

```sh
node server.mjs
```

Abra http://localhost:8080. Arquivos estáticos em `dist/`.

```sh
node --test checkout.test.mjs pix.test.mjs energy.test.mjs
```

Os testes não realizam pagamentos.

## Estrutura

- `dist/index.html`: apresentação e mockup interativo.
- `dist/app-demo.html`: protótipo em tela cheia.
- `dist/pix.mjs`: Pix estático com valor e CRC16.
- `dist/project-status.json`: status e histórico público do projeto.
- `server.mjs`: servidor local e integração opcional de checkout.
- `docs/mapas-e-navegacao.md`: requisitos de navegação.

## Doações

A interface gera Pix estático no navegador. Gerar ou copiar o código não confirma recebimento. O contador inicia em R$ 0,00; conciliação e atualização automática ainda não foram implementadas. Nunca some códigos gerados como doações recebidas.

O backend mantém uma integração opcional com InfinitePay. Confirmação por payment_check e registros persistentes precisam ser implementados antes de automatizar a arrecadação.

## Publicação

Configure SITE_ORIGIN=https://ourtalk.com.br ao usar a API de checkout atrás do proxy HTTPS na VPS. Porta padrão 8080, vinculada a 127.0.0.1. Não inclua credenciais, .env ou dados pessoais no repositório.

## Atualizar o andamento

Edite `dist/project-status.json`. Datas usam YYYY-MM-DD; `stages` define os cartões e `updates` o histórico. Publique apenas avanços verificados. Não existe painel administrativo nesta versão.

## Código e marcas

O código original do projeto é disponibilizado sob a licença MIT (veja LICENSE). Essa licença se aplica ao código, não às logomarcas e materiais de terceiros. Marcas e logomarcas pertencem aos seus titulares; sua presença aqui não concede direitos de uso.


## Aplicativo Android
Código nativo em `android/`, com Kotlin/Compose e Android SDK 36. A versão interna 0.2 inclui proteção local com Android Keystore e integração experimental Nearby Connections, com comparação de código obrigatória. A comunicação de rádio depende de validação em dois aparelhos físicos; não apresentar a compilação ou testes de emulador como prova de alcance ou entrega real. Leia [SECURITY.md](SECURITY.md) antes de contribuir.

## Cadastro e energia
O cadastro de lançamento funciona em PHP na hospedagem; leia `docs/cadastro-lancamento.md`. A calculadora usa `dist/energy.mjs`; veja `docs/calculadora-energia.md`.
