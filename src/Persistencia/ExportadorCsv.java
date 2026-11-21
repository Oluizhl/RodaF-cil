package Persistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Exporta as quatro tabelas em CSV com ponto e virgula (Excel em portugues). */
public final class ExportadorCsv {
    private ExportadorCsv() { }

    public static void exportarTodos(EstadoSistema dados, Path pastaDestino) throws IOException {
        Files.createDirectories(pastaDestino);
        exportarCarros(dados, pastaDestino.resolve("carros.csv"));
        exportarClientes(dados, pastaDestino.resolve("clientes.csv"));
        exportarFuncionarios(dados, pastaDestino.resolve("funcionarios.csv"));
        exportarAlugueis(dados, pastaDestino.resolve("alugueis.csv"));
    }

    public static void exportarCarros(EstadoSistema dados, Path destino) throws IOException {
        List<List<String>> linhas = new ArrayList<>();
        linhas.add(Arrays.asList("tipo", "placa", "marca", "modelo", "ano", "km", "situacao", "diaria", "kmUltimaRevisao"));
        for (Object c : dados.getCarros()) {
            linhas.add(Arrays.asList(c.getClass().getSimpleName(), chamar(c,"getPlaca"), chamar(c,"getMarca"),
                    chamar(c,"getModelo"), chamar(c,"getAno"), chamar(c,"getKm"), chamar(c,"getSituacao"),
                    chamar(c,"getValorDiaria"), chamar(c,"getKmUltimaRevisao")));
        }
        escrever(destino, linhas);
    }

    public static void exportarClientes(EstadoSistema dados, Path destino) throws IOException {
        List<List<String>> linhas = new ArrayList<>();
        linhas.add(Arrays.asList("nome", "cpf", "telefone", "email", "nascimento", "cnh", "pontos", "debito", "motivoDebito"));
        for (Object c : dados.getClientes()) {
            Object cnh = chamarObjeto(c, "getCnh");
            linhas.add(Arrays.asList(chamar(c,"getNome"), chamar(c,"getCpf"), chamar(c,"getTelefone"),
                    chamar(c,"getEmail"), chamar(c,"getDataNascimento"),
                    cnh == null ? "" : chamar(cnh, "getNumero"), chamar(c,"getPontos"),
                    chamar(c,"getDebito"), chamar(c,"getMotivoDebito")));
        }
        escrever(destino, linhas);
    }

    public static void exportarFuncionarios(EstadoSistema dados, Path destino) throws IOException {
        List<List<String>> linhas = new ArrayList<>();
        linhas.add(Arrays.asList("nome", "cpf", "telefone", "email", "matricula", "cargo"));
        for (Object f : dados.getFuncionarios()) {
            linhas.add(Arrays.asList(chamar(f,"getNome"), chamar(f,"getCpf"), chamar(f,"getTelefone"),
                    chamar(f,"getEmail"), chamar(f,"getMatricula"), chamar(f,"getCargo")));
        }
        escrever(destino, linhas);
    }

    public static void exportarAlugueis(EstadoSistema dados, Path destino) throws IOException {
        List<List<String>> linhas = new ArrayList<>();
        linhas.add(Arrays.asList("id", "cpfCliente", "placaCarro", "dataRetirada", "devolucaoPrevista", "dataDevolucaoReal", "situacao", "totalOrcamento"));
        for (Object a : dados.getAlugueis()) {
            Object c = chamarObjeto(a,"getCliente");
            Object v = chamarObjeto(a,"getVeiculo");
            Object o = chamarObjeto(a,"getOrcamento");
            linhas.add(Arrays.asList(chamar(a,"getId"), c == null ? "" : chamar(c,"getCpf"),
                    v == null ? "" : chamar(v,"getPlaca"), chamar(a,"getDataRetirada"),
                    chamar(a,"getDataPrevistaDevolucao"), chamar(a,"getDataDevolucaoReal"),
                    chamar(a,"getSituacao"), o == null ? "" : chamar(o,"getTotal")));
        }
        escrever(destino, linhas);
    }

    private static Object chamarObjeto(Object obj, String metodo) throws IOException {
        if (obj == null) return null;
        try {
            return obj.getClass().getMethod(metodo).invoke(obj);
        } catch (ReflectiveOperationException e) {
            throw new IOException("Nao foi possivel exportar " + obj.getClass().getSimpleName() + "." + metodo, e);
        }
    }

    private static String chamar(Object obj, String metodo) throws IOException {
        Object valor = chamarObjeto(obj, metodo);
        return valor == null ? "" : valor.toString();
    }

    private static void escrever(Path destino, List<List<String>> linhas) throws IOException {
        if (destino.getParent() != null) Files.createDirectories(destino.getParent());
        StringBuilder csv = new StringBuilder("\uFEFF"); // BOM UTF-8 para Excel reconhecer acentos
        for (List<String> linha : linhas) {
            for (int i = 0; i < linha.size(); i++) {
                if (i != 0) csv.append(';');
                csv.append(celula(linha.get(i)));
            }
            csv.append("\r\n");
        }
        Files.writeString(destino, csv.toString(), StandardCharsets.UTF_8);
    }

    private static String celula(String texto) {
        if (texto == null) texto = "";
        // Nao executar formula quando o CSV for aberto em uma planilha.
        if (!texto.isEmpty() && (texto.charAt(0) == '=' || texto.charAt(0) == '+'
                || texto.charAt(0) == '-' || texto.charAt(0) == '@'
                || texto.charAt(0) == '\t' || texto.charAt(0) == '\r')) texto = "'" + texto;
        return '"' + texto.replace("\"", "\"\"") + '"';
    }
}
