# Contribua com o OurTalk

Toda ajuda é bem-vinda: código, design, documentação, testes, acessibilidade e ideias.

## Comece por aqui

1. Leia o README, ROADMAP.md, SECURITY.md e android/README.md. O site está publicado; o Android é uma versão interna, com comunicação próxima experimental ainda sem validação física.
2. Consulte as issues antes de abrir uma sugestão. Descreva o problema real, quem é afetado e como a proposta ajuda.
3. Para mudanças maiores, abra uma issue antes de implementar. Mudanças pequenas podem seguir direto para um pull request.
4. Faça um fork, crie uma branch e mantenha a alteração focada.
5. No pull request, explique o que mudou e como verificou o resultado. Para interface, inclua uma captura em celular e computador.

## Executar localmente

Use Node.js 22 ou superior:

```sh
node server.mjs
```

Abra http://localhost:8080. Para testar os módulos de pagamento sem realizar cobranças:

```sh
node --test checkout.test.mjs pix.test.mjs energy.test.mjs
```

## Onde você pode ajudar

Para Android, use JDK 17/SDK 36 e execute `gradlew.bat :core:verifyCore :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug` dentro de android. No Linux, use `bash ./gradlew` com as mesmas tarefas. Execute a instrumentação em dispositivo de teste; o CI de compilação não substitui testes de rádio. Documente o resultado real e marque como pendente o que não executou.

- Comunicação local entre aparelhos e confirmação de entrega.
- Mapas, localização e rotas offline.
- TalK e consulta a conteúdos salvos com fontes.
- Privacidade, segurança, acessibilidade e testes em aparelhos reais.
- Design, redação e documentação.

## Cuidados nas contribuições

Não inclua credenciais, dados pessoais, localização de usuários ou comprovantes de pagamento. Use dados de teste. Não apresente recursos planejados como funcionalidades prontas. Não aumente a arrecadação com códigos Pix gerados: o total precisa refletir recebimentos confirmados.

Respeite as pessoas que participam e dê crédito às fontes e autorias. As logomarcas e demais marcas pertencem aos seus titulares.

## Falhas de segurança

Não publique detalhes exploráveis, chaves ou dados pessoais em issues públicas. Use o recurso de relato privado de vulnerabilidades do GitHub quando estiver disponível; caso contrário, abra uma issue pedindo um canal privado, sem revelar detalhes.
