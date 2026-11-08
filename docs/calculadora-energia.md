# Calculadora de energia

Núcleo puro em dist/energy.mjs; interface em energy-ui.mjs. Testes: node --test energy.test.mjs. Inputs em W/Wh/Ah explicitamente separados. Consumo diário inclui cargas por energia/ciclo ou potência x tempo e autoconsumo do inversor. Autonomia, margem, eficiência, fração utilizável, tensão do banco e horas de sol pleno são editáveis.

A estimativa solar considera consumo diário e reposição da reserva vazia no prazo escolhido. Não é uma simulação horária ou garantia climática. Não equivale ao modelo PVWatts. A soma simultânea de potências é conservadora; o pico de partida deve ser informado quando houver motores. As indicações são categorias e capacidades mínimas, sem promessa de compatibilidade de um produto específico.

Confira sempre tensão/corrente/MPPT, protocolos USB-C, profundidade de descarga, potência contínua e pico no manual do equipamento real. Não usar a calculadora como projeto de instalação elétrica.
