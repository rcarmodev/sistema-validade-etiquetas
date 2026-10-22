import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Produto {
    public String nome;
    public String dataVencimentoStr;
    public long diasRestantes;
    public String status;
    public String prioridade;

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Produto(String nome, String dataVencimentoStr) {
        this.nome = nome;
        this.dataVencimentoStr = dataVencimentoStr;
        calcularStatus();
    }

    private void calcularStatus() {
        try {
            LocalDate dataVencimento = LocalDate.parse(this.dataVencimentoStr, FORMATO_DATA);
            LocalDate hoje = LocalDate.now();

            this.diasRestantes = ChronoUnit.DAYS.between(hoje, dataVencimento);

            if (this.diasRestantes < 0) {
                this.status = "VENCIDO";
                this.prioridade = "CRÍTICA (Descartar)";
            } else if (this.diasRestantes == 0) {
                this.status = "VENCE HOJE";
                this.prioridade = "URGENTE (Usar Imediatamente)";
            } else if (this.diasRestantes <= 3) {
                this.status = "ATENÇÃO";
                this.prioridade = "ALTA (Usar em Breve)";
            } else {
                this.status = "OK";
                this.prioridade = "NORMAL";
            }
        } catch (Exception e) {
            this.status = "DATA INVÁLIDA";
            this.prioridade = "INDEFINIDA";
            this.diasRestantes = 999;
        }
    }

    public String paraLinhaTexto() {
        return nome + ";" + dataVencimentoStr;
    }

    // Gera comandos no padrão ESC/POS para impressoras térmicas
    public byte[] gerarComandosImpressora() {
        StringBuilder escpos = new StringBuilder();

        String INIT = "\u001b\u0040";        
        String CENTER = "\u001b\u0061\u0001";  
        String LEFT = "\u001b\u0061\u0000";    
        String BOLD_ON = "\u001b\u0045\u0001";  
        String BOLD_OFF = "\u001b\u0045\u0000"; 
        String DOUBLE_HEIGHT = "\u001d\u0021\u0011"; 
        String NORMAL_SIZE = "\u001d\u0021\u0000";  
        String CUT = "\u001d\u0056\u0042\u0000";    

        escpos.append(INIT);
        escpos.append(CENTER);
        escpos.append(BOLD_ON).append(DOUBLE_HEIGHT);
        escpos.append("ETIQUETA DE COZINHA\n");
        escpos.append(NORMAL_SIZE).append(BOLD_OFF);
        escpos.append("--------------------------------\n");

        escpos.append(LEFT);
        escpos.append(BOLD_ON).append("PRODUTO: ").append(BOLD_OFF).append(nome.toUpperCase()).append("\n");
        escpos.append(BOLD_ON).append("VALIDADE: ").append(BOLD_OFF).append(dataVencimentoStr).append("\n");
        escpos.append(BOLD_ON).append("PRIORIDADE: ").append(BOLD_OFF).append(prioridade).append("\n");

        escpos.append(CENTER);
        escpos.append("--------------------------------\n");
        escpos.append("Impresso via Controle de Estoque\n\n\n");
        escpos.append(CUT);

        return escpos.toString().getBytes();
    }
}