package Telas;

import Alugueis.GerenciadorLocacoes;
import Carros.Locadora;
import Clientes.CadastroClientes;
import Clientes.CadastroFuncionarios;
import Persistencia.BackupDrive;
import Persistencia.BancoDados;
import Persistencia.EstadoSistema;
import Persistencia.ExportadorCsv;
import Persistencia.HistoricoAcoes;

import java.io.IOException;
import java.nio.file.Path;

public class Sistema {
    private Locadora locadora;
    private CadastroClientes clientes;
    private CadastroFuncionarios funcionarios;
    private GerenciadorLocacoes locacoes;
    private BancoDados banco;
    private HistoricoAcoes historico;
    private BackupDrive backup;
    private Path pastaDados;

    public Sistema(Path pastaDados){
        this.pastaDados = pastaDados;
        locadora = new Locadora();
        clientes = new CadastroClientes();
        funcionarios = new CadastroFuncionarios();
        locacoes = new GerenciadorLocacoes();
        banco = new BancoDados(pastaDados);
        historico = new HistoricoAcoes(pastaDados);
        backup = BackupDrive.daVariavelDeAmbiente();
    }

    // lê o arquivo dados/locadora.json e coloca tudo nos gerenciadores
    public void carregar(){
        try {
            EstadoSistema estado = banco.carregar();
            estado.aplicarEm(locadora, clientes, funcionarios, locacoes);
        } catch (IOException e){
            System.out.println("AVISO: Não foi possível carregar os dados salvos: " + e.getMessage());
        }
    }

    public void salvar(){
        try {
            banco.salvar(EstadoSistema.capturar(locadora, clientes, funcionarios, locacoes));
        } catch (IOException e){
            System.out.println("AVISO: Não foi possível salvar os dados: " + e.getMessage());
            return;
        }

        try {
            backup.fazerBackup(pastaDados); // se RODAFACIL_DRIVE_DIR não existir, não faz nada
        } catch (IOException e){
            System.out.println("AVISO: Backup no Drive falhou: " + e.getMessage());
        }
    }

    // opção do menu: força o backup agora e avisa o resultado
    public void fazerBackupAgora(){
        try {
            banco.salvar(EstadoSistema.capturar(locadora, clientes, funcionarios, locacoes));
            if (backup.fazerBackup(pastaDados)){
                System.out.println("Backup feito na pasta sincronizada do Google Drive.");
            } else {
                System.out.println("Backup não configurado. Defina a variável de ambiente RODAFACIL_DRIVE_DIR "
                        + "com o caminho da pasta do Google Drive e reabra o sistema.");
            }
        } catch (IOException e){
            System.out.println("ERRO: Não foi possível fazer o backup: " + e.getMessage());
        }
    }

    // opção do menu: gera carros.csv, clientes.csv, funcionarios.csv e alugueis.csv em dados/csv
    public void exportarCsv(){
        try {
            Path pastaCsv = pastaDados.resolve("csv");
            ExportadorCsv.exportarTodos(EstadoSistema.capturar(locadora, clientes, funcionarios, locacoes), pastaCsv);
            System.out.println("Planilhas geradas em: " + pastaCsv.toAbsolutePath());
        } catch (IOException e){
            System.out.println("ERRO: Não foi possível exportar os CSV: " + e.getMessage());
        }
    }

    public void registrarAcao(String funcionario, String acao, String detalhes){
        try {
            historico.registrar(funcionario, acao, detalhes);
        } catch (IOException e){
            System.out.println("AVISO: Não foi possível gravar o histórico: " + e.getMessage());
        }
    }

    // apaga tudo da memória (usado antes de carregar os dados de exemplo)
    public void limpar(){
        locadora = new Locadora();
        clientes = new CadastroClientes();
        funcionarios = new CadastroFuncionarios();
        locacoes = new GerenciadorLocacoes();
    }

    public Locadora getLocadora(){
        return locadora;
    }

    public CadastroClientes getClientes(){
        return clientes;
    }

    public CadastroFuncionarios getFuncionarios(){
        return funcionarios;
    }

    public GerenciadorLocacoes getLocacoes(){
        return locacoes;
    }

    public Path getPastaDados(){
        return pastaDados;
    }
}
