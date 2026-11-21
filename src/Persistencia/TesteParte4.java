package Persistencia;

import Carros.Eletrico;
import Carros.Passeio;
import Carros.SUV;
import Carros.Veiculo;
import Clientes.CNH;
import Clientes.Cliente;
import Clientes.Funcionario;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/** Testes da Parte 4 sem depender das classes de aluguel ainda inconsistentes. */
public final class TesteParte4 {
    private static int acertos = 0;
    private static void checar(boolean condicao, String descricao) {
        if (!condicao) throw new AssertionError("FALHOU: " + descricao);
        acertos++;
        System.out.println("OK: " + descricao);
    }

    public static void main(String[] args) throws Exception {
        Path pasta = Files.createTempDirectory("rodafacil-parte4-");
        BancoDados banco = new BancoDados(pasta);
        EstadoSistema vazio = banco.carregar();
        checar(Files.exists(banco.getArquivo()), "Arquivo JSON criado quando ausente");
        checar(vazio.getCarros().isEmpty(), "Carregamento inicial vazio");

        Passeio passeio = new Passeio("ABC1D23", "Fiat", "Argo", 2023, 10000, 120);
        passeio.realizarRevisao("19/11/2026", 250);
        passeio.setKm(11000);
        passeio.setSituacao("Alugado");
        SUV suv = new SUV("DEF2G45", "Jeep", "Renegade", 2024, 25000, 200);
        Eletrico eletrico = new Eletrico("XYZ3H67", "BYD", "Dolphin", 2024, 5000, 250, 400);
        CNH cnh = new CNH("12345678901", "B", LocalDate.of(2020, 1, 1), LocalDate.of(2035, 1, 1));
        Cliente cliente = new Cliente("Ana", "52998224725", "86999998888", "ana@exemplo.com",
                LocalDate.of(1995, 3, 2), cnh);
        cliente.registrarAluguel(6000);
        cliente.registrarDebito(75, "avaria");
        Funcionario funcionario = new Funcionario("Carlos", "11144477735", "86999997777", "carlos@exemplo.com", "F001", "Atendente");
        EstadoSistema original = new EstadoSistema(Arrays.asList(passeio, suv, eletrico), List.of(cliente),
                List.of(funcionario), List.of());
        original.setProximoIdLocacao(8);
        banco.salvar(original);

        String json = Files.readString(banco.getArquivo(), StandardCharsets.UTF_8);
        checar(json.contains("Carros.Eletrico"), "Tipo concreto do eletrico registrado no JSON");
        checar(json.contains("Clientes.Cliente"), "JSON com dados do cliente");
        EstadoSistema recuperado = banco.carregar();
        checar(recuperado.getCarros().size() == 3, "Tres carros recuperados");
        checar(recuperado.carros(Veiculo.class).get(1) instanceof SUV, "Tipo SUV preservado");
        checar(recuperado.carros(Veiculo.class).get(2) instanceof Eletrico, "Tipo Eletrico preservado");
        checar(((Eletrico) recuperado.carros(Veiculo.class).get(2)).getAutonomiaKm() == 400, "Autonomia preservada");
        Veiculo restaurado = recuperado.carros(Veiculo.class).get(0);
        checar(restaurado.getKm() == 11000 && restaurado.getKmUltimaRevisao() == 10000,
                "Quilometragem e revisao preservadas");
        checar("Alugado".equals(restaurado.getSituacao()), "Situacao do carro preservada");
        var campoHistorico = Veiculo.class.getDeclaredField("historico");
        campoHistorico.setAccessible(true);
        checar(((List<?>) campoHistorico.get(restaurado)).size() == 1,
                "Historico de manutencao preservado");
        checar(recuperado.clientes(Cliente.class).get(0).getPontos() == 600, "Fidelidade preservada");
        checar(recuperado.clientes(Cliente.class).get(0).isBloqueado(), "Debito preservado");
        checar(recuperado.getProximoIdLocacao() == 8, "Proximo ID de aluguel preservado");
        checar(recuperado.funcionarios(Funcionario.class).get(0).getMatricula().equals("F001"), "Funcionario recuperado");

        HistoricoAcoes historico = new HistoricoAcoes(pasta);
        historico.registrar("Carlos", "DEVOLUCAO", "Carro ABC1D23\nSegunda linha");
        checar(historico.ler().contains("DEVOLUCAO"), "Historico TXT criado");
        checar(historico.ler().lines().count() == 1, "Historico protege contra quebra de linha");

        Path csv = pasta.resolve("csv");
        ExportadorCsv.exportarTodos(recuperado, csv);
        checar(Files.exists(csv.resolve("carros.csv")) && Files.exists(csv.resolve("alugueis.csv")),
                "Exportou quatro arquivos CSV");
        checar(Files.readString(csv.resolve("clientes.csv")).contains("Ana"), "CSV de clientes contem os dados");
        checar(!new BackupDrive(null).fazerBackup(pasta), "Sem Drive configurado, nao trava");
        Path copia = Files.createTempDirectory("rodafacil-drive-teste-");
        checar(new BackupDrive(copia).fazerBackup(pasta) && Files.exists(copia.resolve("locadora.json")),
                "Backup em pasta sincronizada (simulada)");

        Files.writeString(banco.getArquivo(), "{ arquivo estragado", StandardCharsets.UTF_8);
        EstadoSistema limpo = banco.carregar();
        checar(limpo.getCarros().isEmpty(), "Arquivo corrompido nao derruba o sistema");
        try (var listaArquivos = Files.list(pasta)) {
            checar(listaArquivos.anyMatch(p -> p.getFileName().toString().contains("corrompido")),
                    "Preserva copia do JSON corrompido");
        }

        Repositorio<Veiculo> repo = new Repositorio<>(Veiculo::getPlaca);
        repo.salvar(passeio);
        checar(repo.buscar("ABC1D23") == passeio, "Repositorio: salvar e buscar");
        checar(repo.listar().size() == 1, "Repositorio: listar");
        checar(repo.remover("ABC1D23") && repo.quantidade() == 0, "Repositorio: remover");

        System.out.println("\nTOTAL: " + acertos + " verificacoes aprovadas.");
    }
}
