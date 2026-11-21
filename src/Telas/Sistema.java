package Telas;

import Alugueis.GerenciadorLocacoes;
import Carros.Locadora;
import Clientes.CadastroClientes;
import Clientes.CadastroFuncionarios;
import Persistencia.BancoDados;
import Persistencia.EstadoSistema;
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
    private Path pastaDados;

    public Sistema(Path pastaDados){
        this.pastaDados = pastaDados;
        locadora = new Locadora();
        clientes = new CadastroClientes();
        funcionarios = new CadastroFuncionarios();
        locacoes = new GerenciadorLocacoes();
        banco = new BancoDados(pastaDados);
        historico = new HistoricoAcoes(pastaDados);
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
