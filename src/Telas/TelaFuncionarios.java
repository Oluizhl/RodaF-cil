package Telas;

import Clientes.Funcionario;

public class TelaFuncionarios extends Tela {
    private Sistema sistema;

    public TelaFuncionarios(Sistema sistema){
        super("Funcionários");
        this.sistema = sistema;
    }

    @Override
    public String[] getOpcoes(){
        return new String[]{
                "Cadastrar funcionário",
                "Listar funcionários",
                "Editar funcionário",
                "Remover funcionário"
        };
    }

    @Override
    public void executar(int opcao){
        switch (opcao){
            case 1: cadastrar(); break;
            case 2: sistema.getFuncionarios().listar(); break;
            case 3: editar(); break;
            case 4: remover(); break;
        }
    }

    private void cadastrar(){
        String cpf = Entrada.lerCpf("CPF");
        if (sistema.getFuncionarios().buscarPorCpf(cpf) != null){
            System.out.println("ERRO: Já existe um funcionário com esse CPF!");
            return;
        }

        String matricula = Entrada.lerTexto("Matrícula (ex.: F001)").toUpperCase();
        if (sistema.getFuncionarios().buscarPorMatricula(matricula) != null){
            System.out.println("ERRO: Já existe um funcionário com essa matrícula!");
            return;
        }

        String nome = Entrada.lerTexto("Nome completo");
        String telefone = Entrada.lerTelefone("Telefone");
        String email = Entrada.lerEmail("E-mail");
        String cargo = Entrada.lerTexto("Cargo");

        Funcionario funcionario = new Funcionario(nome, cpf, telefone, email, matricula, cargo);
        if (sistema.getFuncionarios().cadastrar(funcionario)){
            sistema.registrarAcao(nome, "CADASTRO DE FUNCIONÁRIO", "Matrícula " + matricula + " - " + cargo);
        }
    }

    private void editar(){
        Funcionario funcionario = escolherPorCpf();
        if (funcionario == null){
            return;
        }
        System.out.println("Editando: " + funcionario.getNome() + " (" + funcionario.getMatricula() + ")");
        String nome = Entrada.lerTexto("Nome");
        String telefone = Entrada.lerTelefone("Telefone");
        String email = Entrada.lerEmail("E-mail");
        String cargo = Entrada.lerTexto("Cargo");

        sistema.getFuncionarios().editar(funcionario.getCpf(), nome, funcionario.getCpf(), telefone, email, cargo);
    }

    private void remover(){
        Funcionario funcionario = escolherPorCpf();
        if (funcionario != null && Entrada.lerSimNao("Remover " + funcionario.getNome() + "?")){
            sistema.getFuncionarios().remover(funcionario.getCpf());
        }
    }

    private Funcionario escolherPorCpf(){
        String cpf = Entrada.lerCpf("CPF do funcionário");
        Funcionario funcionario = sistema.getFuncionarios().buscarPorCpf(cpf);
        if (funcionario == null){
            System.out.println("ERRO: Funcionário com esse CPF não encontrado!");
        }
        return funcionario;
    }
}
