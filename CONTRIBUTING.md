# Contribua com o OurTalk

Toda ajuda é bem-vinda: código, design, documentação, testes, acessibilidade e ideias.

## Comece por aqui

1. Leia o README para entender o estágio atual: apresentação e protótipo navegável. O aplicativo de comunicação offline ainda será desenvolvido.
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
node --test checkout.test.mjs pix.test.mjs
```

## Onde você pode ajudar

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
