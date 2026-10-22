import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.*;

public class Main {
    private static List<Produto> estoque = new ArrayList<>();
    private static final String ARQUIVO_ESTOQUE = "estoque.txt";
    private static final int PORTA = 8080;

    public static void main(String[] args) throws IOException {
        // Gerador automático de .jar
        if (args.length > 0 && args[0].equalsIgnoreCase("--gerar-jar")) {
            gerarJarAutomatico();
            return;
        }

        carregarDadosDoArquivo();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORTA), 0);
        server.createContext("/", new DashboardHandler());
        server.createContext("/cadastrar", new CadastrarHandler());
        server.createContext("/excluir", new ExcluirHandler());
        server.setExecutor(null);

        System.out.println("====================================================");
        System.out.println("🚀 SISTEMA DE ETIQUETAS DE COZINHA INICIADO!");
        System.out.println("📱 Acesse no seu computador:");
        System.out.println("👉 http://localhost:" + PORTA);
        System.out.println("====================================================");

        server.start();

        // Tenta abrir o navegador padrão automaticamente no Windows/Mac/Linux
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:" + PORTA));
            }
        } catch (Exception e) {
            System.out.println("Nao foi possivel abrir o navegador automaticamente: " + e.getMessage());
        }
    }

    private static void gerarJarAutomatico() {
        try {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "Main");

            JarOutputStream target = new JarOutputStream(new FileOutputStream("EtiquetasCozinha.jar"), manifest);

            // Inclui TODOS os arquivos .class da pasta (incluindo as inner classes Main$*.class)
            File pastaAtual = new File(".");
            File[] arquivosClass = pastaAtual.listFiles((dir, name) -> name.endsWith(".class"));

            if (arquivosClass != null) {
                for (File file : arquivosClass) {
                    adicionarAoJar(file, target);
                }
            }

            target.close();
            System.out.println("====================================================");
            System.out.println("SUCESSO! O ARQUIVO 'EtiquetasCozinha.jar' FOI GERADO COM TODAS AS CLASSES!");
            System.out.println("====================================================");
        } catch (Exception e) {
            System.out.println("Erro ao gerar .jar: " + e.getMessage());
        }
    }

    private static void adicionarAoJar(File source, JarOutputStream target) throws IOException {
        if (source.exists()) {
            JarEntry entry = new JarEntry(source.getName());
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(source))) {
                byte[] buffer = new byte[1024];
                while (true) {
                    int count = in.read(buffer);
                    if (count == -1) break;
                    target.write(buffer, 0, count);
                }
                target.closeEntry();
            }
        }
    }

    static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            estoque.sort(Comparator.comparingLong(p -> p.diasRestantes));

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html lang='pt-br'><head><meta charset='UTF-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                .append("<title>Etiquetas de Cozinha</title>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f4f4f9; }")
                .append("h1 { color: #333; text-align: center; }")
                .append(".card { background: white; padding: 20px; margin-bottom: 15px; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }")
                .append(".btn { display: inline-block; width: 100%; padding: 15px; margin-top: 10px; background-color: #28a745; color: white; text-align: center; font-size: 18px; border: none; border-radius: 8px; font-weight: bold; cursor: pointer; text-decoration: none; box-sizing: border-box; }")
                .append(".btn-danger { background-color: #dc3545; padding: 8px 12px; font-size: 14px; color: white; text-decoration: none; border-radius: 5px; }")
                .append(".btn-print { background-color: #007bff; padding: 8px 12px; font-size: 14px; margin-right: 5px; color: white; text-decoration: none; border-radius: 5px; cursor: pointer; }")
                .append("input { width: 100%; padding: 12px; margin: 8px 0; border: 1px solid #ccc; border-radius: 6px; font-size: 16px; box-sizing: border-box; }")
                .append(".urgente { border-left: 8px solid #dc3545; }")
                .append(".atencao { border-left: 8px solid #ffc107; }")
                .append(".ok { border-left: 8px solid #28a745; }")
                
                // --- CSS DE IMPRESSÃO COMPACTA (60mm x 40mm) ---
                .append("@media print {")
                .append("  @page { size: 60mm 40mm; margin: 0; }")
                .append("  body { background: white; margin: 0; padding: 0; width: 60mm; height: 40mm; overflow: hidden; }")
                .append("  h1, h2, form, .btn, .btn-danger, .btn-print, p:not(.imprimir) { display: none !important; }")
                .append("  .card { display: none !important; }")
                .append("  .card-imprimir { display: block !important; border: 1px solid #000 !important; box-shadow: none !important; margin: 0 !important; width: 58mm !important; height: 38mm !important; padding: 2mm !important; box-sizing: border-box; font-size: 11px; font-family: monospace; }")
                .append("  .card-imprimir h3 { margin: 0 0 4px 0; font-size: 13px; text-transform: uppercase; border-bottom: 1px solid #000; padding-bottom: 2px; text-align: center; }")
                .append("  .card-imprimir p { margin: 2px 0 !important; font-size: 11px; }")
                .append("}")
                
                .append("</style></head><body>");

            html.append("<h1>🏷️ Etiquetas da Cozinha</h1>");

            html.append("<div class='card'>")
                .append("<h2>➕ Novo Item de Estoque</h2>")
                .append("<form action='/cadastrar' method='POST'>")
                .append("<label>Nome do Insumo/Molho:</label>")
                .append("<input type='text' name='nome' placeholder='Ex: Molho de Tomate' required>")
                .append("<label>Data de Vencimento:</label>")
                .append("<input type='text' name='data' placeholder='DD/MM/AAAA' required>")
                .append("<button type='submit' class='btn'>Cadastrar e Salvar</button>")
                .append("</form>")
                .append("</div>");

            html.append("<h2>📋 Itens no Estoque (Ordenado por Urgência)</h2>");
            if (estoque.isEmpty()) {
                html.append("<p style='text-align:center;'>Nenhum item cadastrado no momento.</p>");
            } else {
                for (int i = 0; i < estoque.size(); i++) {
                    Produto p = estoque.get(i);
                    String classeUrgencia = p.diasRestantes <= 0 ? "urgente" : (p.diasRestantes <= 3 ? "atencao" : "ok");

                    html.append("<div class='card ").append(classeUrgencia).append("' id='card-").append(i).append("'>")
                        .append("<h3>").append(p.nome).append("</h3>")
                        .append("<p class='imprimir'><b>Validade:</b> ").append(p.dataVencimentoStr).append(" (").append(p.diasRestantes).append("d)</p>")
                        .append("<p class='imprimir'><b>Status:</b> ").append(p.status).append("</p>")
                        .append("<button type='button' onclick='imprimirDirecto(").append(i).append(")' class='btn btn-print'>🖨️ Imprimir Etiqueta</button> ")
                        .append("<a href='/excluir?id=").append(i).append("' class='btn btn-danger'>🗑️ Excluir</a>")
                        .append("</div>");
                }
            }

            html.append("<script>")
                .append("function imprimirDirecto(id) {")
                .append("  var cards = document.querySelectorAll('.card');")
                .append("  cards.forEach(function(c) { c.classList.remove('card-imprimir'); });")
                .append("  var cardAlvo = document.getElementById('card-' + id);")
                .append("  if(cardAlvo) { cardAlvo.classList.add('card-imprimir'); }")
                .append("  window.print();")
                .append("}")
                .append("</script>");

            html.append("</body></html>");

            byte[] response = html.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    static class CadastrarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();

                Map<String, String> params = parseFormData(formData);
                String nome = params.get("nome");
                String data = params.get("data");

                if (nome != null && data != null) {
                    Produto p = new Produto(nome, data);
                    estoque.add(p);
                    salvarDadosNoArquivo();
                }
            }
            exchange.getResponseHeaders().set("Location", "/");
            exchange.sendResponseHeaders(302, -1);
        }
    }

    static class ExcluirHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.startsWith("id=")) {
                try {
                    int index = Integer.parseInt(query.substring(3));
                    if (index >= 0 && index < estoque.size()) {
                        estoque.remove(index);
                        salvarDadosNoArquivo();
                    }
                } catch (NumberFormatException ignored) {}
            }
            exchange.getResponseHeaders().set("Location", "/");
            exchange.sendResponseHeaders(302, -1);
        }
    }

    private static Map<String, String> parseFormData(String formData) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name());
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name());
                map.put(key, value);
            }
        }
        return map;
    }

    private static void salvarDadosNoArquivo() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARQUIVO_ESTOQUE))) {
            for (Produto p : estoque) {
                writer.write(p.paraLinhaTexto());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar dados: " + e.getMessage());
        }
    }

    private static void carregarDadosDoArquivo() {
        File file = new File(ARQUIVO_ESTOQUE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(ARQUIVO_ESTOQUE))) {
            String linha;
            estoque.clear();
            while ((linha = reader.readLine()) != null) {
                if (!linha.trim().isEmpty()) {
                    String[] partes = linha.split(";");
                    if (partes.length == 2) {
                        estoque.add(new Produto(partes[0], partes[1]));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar dados: " + e.getMessage());
        }
    }
}