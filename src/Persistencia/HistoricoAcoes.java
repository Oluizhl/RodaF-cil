package Persistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Historico TXT com data, funcionario, acao e detalhes. */
public class HistoricoAcoes {
    private final Path arquivo;

    public HistoricoAcoes(Path pastaDados) { this.arquivo = pastaDados.resolve("historico.txt"); }

    public synchronized void registrar(String funcionario, String acao, String detalhes) throws IOException {
        Files.createDirectories(arquivo.getParent());
        String linha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                + " | Funcionario: " + seguro(funcionario)
                + " | Acao: " + seguro(acao)
                + " | Detalhes: " + seguro(detalhes) + System.lineSeparator();
        Files.writeString(arquivo, linha, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public String ler() throws IOException {
        return Files.exists(arquivo) ? Files.readString(arquivo, StandardCharsets.UTF_8) : "";
    }

    public Path getArquivo() { return arquivo; }

    private static String seguro(String s) {
        return s == null ? "Nao informado" : s.replace('\r', ' ').replace('\n', ' ').replace('|', '/');
    }
}
