package Persistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Salva e recupera o projeto inteiro em dados/locadora.json. */
public class BancoDados {
    private static final int VERSAO = 1;
    private static final long TAMANHO_MAXIMO = 20L * 1024L * 1024L;
    private final Path arquivo;

    public BancoDados(Path pastaDeDados) {
        if (pastaDeDados == null) throw new IllegalArgumentException("Pasta nao informada");
        this.arquivo = pastaDeDados.resolve("locadora.json");
    }

    public Path getArquivo() { return arquivo; }

    /** Grava todas as colecoes juntas para preservar as referencias compartilhadas. */
    public synchronized void salvar(EstadoSistema dados) throws IOException {
        if (dados == null) throw new IllegalArgumentException("Dados nao informados");
        ConversorObjetos serializador = new ConversorObjetos();
        Map<String,Object> raiz = new LinkedHashMap<>();
        raiz.put("versao", VERSAO);
        raiz.put("salvoEm", LocalDateTime.now().toString());
        raiz.put("proximoIdLocacao", dados.getProximoIdLocacao());
        raiz.put("carros", serializador.codificar(dados.getCarros()));
        raiz.put("clientes", serializador.codificar(dados.getClientes()));
        raiz.put("funcionarios", serializador.codificar(dados.getFuncionarios()));
        raiz.put("alugueis", serializador.codificar(dados.getAlugueis()));

        String json = JsonSimples.escrever(raiz);
        if (json.getBytes(StandardCharsets.UTF_8).length > TAMANHO_MAXIMO)
            throw new IOException("O banco JSON excede 20 MB");
        Files.createDirectories(arquivo.getParent());
        Path temporario = Files.createTempFile(arquivo.getParent(), "locadora-", ".tmp");
        try {
            Files.writeString(temporario, json, StandardCharsets.UTF_8);
            try {
                Files.move(temporario, arquivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporario);
        }
    }

    /**
     * Se o arquivo nao existir, cria um banco vazio.
     * Se estiver corrompido, preserva o original com sufixo .corrompido-... e
     * inicia outro banco vazio, sem encerrar o sistema.
     */
    public synchronized EstadoSistema carregar() throws IOException {
        Files.createDirectories(arquivo.getParent());
        if (Files.notExists(arquivo)) {
            EstadoSistema vazio = new EstadoSistema();
            salvar(vazio);
            return vazio;
        }
        try {
            if (Files.size(arquivo) > TAMANHO_MAXIMO) throw new IOException("Arquivo JSON muito grande");
            String json = Files.readString(arquivo, StandardCharsets.UTF_8);
            Object raizObj = JsonSimples.ler(json);
            if (!(raizObj instanceof Map<?,?>)) throw new IOException("Formato do banco invalido");
            @SuppressWarnings("unchecked") Map<String,Object> raiz = (Map<String,Object>) raizObj;
            if (!(raiz.get("versao") instanceof Number) || ((Number) raiz.get("versao")).intValue() != VERSAO)
                throw new IOException("Versao de banco nao suportada");
            ConversorObjetos leitor = new ConversorObjetos();
            EstadoSistema dados = new EstadoSistema(
                exigirLista(leitor.decodificar(raiz.get("carros"))),
                exigirLista(leitor.decodificar(raiz.get("clientes"))),
                exigirLista(leitor.decodificar(raiz.get("funcionarios"))),
                exigirLista(leitor.decodificar(raiz.get("alugueis")))
            );
            Object proximo = raiz.get("proximoIdLocacao");
            if (!(proximo instanceof Number) || ((Number) proximo).intValue() <= 0)
                throw new IOException("Proximo id invalido");
            dados.setProximoIdLocacao(((Number) proximo).intValue());
            return dados;
        } catch (IOException | RuntimeException erro) {
            String sufixo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
            Path copia = arquivo.resolveSibling("locadora.corrompido-" + sufixo + ".json");
            Files.move(arquivo, copia);
            System.err.println("AVISO: JSON invalido. Copia preservada em: " + copia);
            System.err.println("Detalhe: " + erro.getMessage());
            EstadoSistema vazio = new EstadoSistema();
            salvar(vazio);
            return vazio;
        }
    }

    private static List<?> exigirLista(Object valor) throws IOException {
        if (!(valor instanceof List<?>)) throw new IOException("Colecao invalida no banco JSON");
        return (List<?>) valor;
    }
}
