# 🏷️ Sistema de Controle de Validade & Impressão Térmica ESC/POS

> **Aplicação em Java Core para controle prático de shelf-life de insumos perecíveis em food service, cálculo automatizado de prazos e geração de comandos binários ESC/POS para impressoras térmicas compactas.**

![Java](https://img.shields.io/badge/Java_Core-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![ESC/POS](https://img.shields.io/badge/Hardware-ESC%2FPOS_Thermal-blue?style=for-the-badge)
![HTTP Server](https://img.shields.io/badge/HTTP_Server-Embedded_Native-green?style=for-the-badge)
![CSS Print](https://img.shields.io/badge/CSS3-Print_60x40mm-orange?style=for-the-badge)

---

## 🎯 1. O Desafio Operacional & de Armazenamento

No setor de food service (restaurantes, cozinhas industriais e dark kitchens), a rotulagem de insumos fracionados, manipulados ou descongelados é uma rotina operacional crítica (orientada pelas boas práticas de vigilância sanitária e Anvisa), exigindo identificação rápida do insumo, data de manipulação e prazo de validade.

Os principais problemas enfrentados no chão de operação são:
* **Fita crepe e caneta:** O método manual tradicional apaga com a umidade da câmara fria, gera erros de cálculo mental de dias e resulta em descarte desnecessário ou falhas de conformidade em inspeções de rotina.
* **Falta de priorização visual:** Dificuldade da equipe identificar rapidamente quais recipientes vencem primeiro no sistema *PEPS (Primeiro que Entra, Primeiro que Sai)*.
* **Custo de sistemas pesados:** Softwares de PDV tradicionais não possuem módulos ágeis e leves de etiquetagem rápida para celulares ou tablets da cozinha.

Este software foi desenhado para rodar de forma instantânea, calcular o status de prioridade e disparar a impressão de etiquetas térmicas em segundos.

---

## ⚙️ 2. Destaques Técnicos & Engenharia

### A) Arquitetura Leve (Zero-Dependency)
Construído em **Java Core** puro sem dependências pesadas de terceiros:
* Utilização do **`com.sun.net.httpserver.HttpServer`** embutido na JDK para fornecer um servidor HTTP local veloz.
* Interface web responsiva renderizada diretamente para permitir acesso via navegador de qualquer celular ou computador conectado na rede local.

### B) Integração de Hardware via Comandos ESC/POS
Para impressoras térmicas de cupom e etiquetas (Epson, Daruma, Elgin, Bematech), o sistema conta com método dedicado de geração de comandos de controle binários em baixo nível (`byte[]`):
* `\u001b\u0040` (Inicialização e Reset)
* `\u001b\u0061\u0001` (Alinhamento Centralizado)
* `\u001b\u0045\u0001` (Negrito Ativado)
* `\u001d\u0021\u0011` (Altura Dupla de Caracteres para destaque do título)
* `\u001d\u0056\u0042\u0000` (Comando de corte de guilhotina / Full Cut)

### C) Layout de Impressão Milimétrico (@media print)
Implementação de regras de estilo específicas para rolos de etiquetas adesivas térmicas de **60mm x 40mm**:
```css
@media print {
  @page { size: 60mm 40mm; margin: 0; }
  .card-imprimir { width: 58mm; height: 38mm; padding: 2mm; font-family: monospace; }
}
```

---

## 🚦 3. Matriz de Prioridade Operacional

O cálculo de validade é executado dinamicamente via `java.time.LocalDate` e `ChronoUnit.DAYS`:

| Dias Restantes | Status Operacional | Nível de Prioridade | Ação Recomendada |
| :---: | :---: | :---: | :--- |
| `< 0` | **VENCIDO** | Crítica | Descarte imediato e registro de quebra |
| `0` | **VENCE HOJE** | Urgente | Uso prioritário na produção do dia |
| `1 a 3` | **ATENÇÃO** | Alta | Consumo preferencial no fluxo PEPS |
| `> 3` | **OK** | Normal | Armazenamento regular em câmara fria |

---

## 🗺️ 4. Roadmap de Evolução Técnica

- [ ] **Módulo Parametrizado de Categorias:** Tabela customizável de dias de shelf-life por tipo de insumo (pescados, laticínios, carnes, hortifrúti).
- [ ] **Socket TCP/IP Direto:** Envio de pacotes ESC/POS diretamente para o IP da impressora na rede sem dependência do driver do sistema operacional.
- [ ] **Persistência Relacional:** Migração do armazenamento baseado em arquivo texto para banco de dados relacional (H2 / PostgreSQL).
- [ ] **Tratamento de Quebra de Linha:** Algoritmo de ajuste automático de tamanho de fonte para nomes longos de insumos.
- [ ] **Integração de Leitura por QR Code:** Adição de código bidimensional na etiqueta para rastreamento de lote e data de abertura.

---

## 🚀 5. Como Executar

### Pré-requisitos
* Java Runtime Environment (JRE) ou JDK 11+ instalado.

### Executando o JAR
1. Clone o repositório:
   ```bash
   git clone https://github.com/rcarmodev/sistema-validade-etiquetas.git
   cd sistema-validade-etiquetas
   ```
2. Execute o arquivo empacotado:
   ```bash
   java -jar EtiquetasCozinha.jar
   ```
3. Abra no navegador: [http://localhost:8080](http://localhost:8080)

---

## 📄 Licença & Propriedade Intelectual

Projeto desenvolvido para fins educacionais, avaliação técnica e demonstração de portfólio. Todos os direitos reservados a **Rafael Carmo (`rcarmodev`)**.
