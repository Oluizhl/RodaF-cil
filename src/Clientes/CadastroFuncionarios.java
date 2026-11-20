package Clientes;

import java.util.ArrayList;

public class CadastroFuncionarios {
    private ArrayList<Funcionario> funcionarios;

    public CadastroFuncionarios(){
        funcionarios = new ArrayList<Funcionario>();
    }

    // devolve true se cadastrou
    public boolean cadastrar(Funcionario funcionario){
        if (funcionario == null){
            System.out.println("ERRO: Funcionário inválido!");
            return false;
        }
        if (funcionario.getCpf() == null){
            System.out.println("ERRO: Não é possível cadastrar funcionário com CPF inválido!");
            return false;
        }
        if (funcionario.getMatricula() == null || funcionario.getMatricula().trim().isEmpty()){
            System.out.println("ERRO: O funcionário precisa de matrícula!");
            return false;
        }
        if (buscarPorCpf(funcionario.getCpf()) != null){
            System.out.println("ERRO: Já existe um funcionário com esse CPF!");
            return false;
        }
        if (buscarPorMatricula(funcionario.getMatricula()) != null){
            System.out.println("ERRO: Já existe um funcionário com essa matrícula!");
            return false;
        }

        funcionarios.add(funcionario);
        System.out.println("Funcionário cadastrado com sucesso!");
        return true;
    }

    public Funcionario buscarPorCpf(String cpf){
        String digitos = Pessoa.apenasDigitos(cpf);

        for (Funcionario f : funcionarios){
            if (f.getCpf().equals(digitos)){
                return f;
            }
        }
        return null;
    }

    public Funcionario buscarPorMatricula(String matricula){
        for (Funcionario f : funcionarios){
            if (f.getMatricula().equalsIgnoreCase(matricula.trim())){
                return f;
            }
        }
        return null;
    }

    public void listar(){
        if (funcionarios.isEmpty()){
            System.out.println("Nenhum funcionário cadastrado.");
            return;
        }

        for (Funcionario f : funcionarios){
            System.out.println("Nome: " + f.getNome());
            System.out.println("CPF: " + f.getCpfFormatado());
            System.out.println("Telefone: " + f.getTelefone());
            System.out.println("E-mail: " + f.getEmail());
            System.out.println("Matrícula: " + f.getMatricula() + " | Cargo: " + f.getCargo());
            System.out.println("-----------------------------");
        }
    }

    // o CPF novo é validado antes de salvar: se for inválido, NENHUM dado é alterado
    public void editar(String cpf, String novoNome, String novoCpf, String novoTelefone, String novoEmail, String novoCargo){
        Funcionario funcionario = buscarPorCpf(cpf);

        if (funcionario == null){
            System.out.println("ERRO: Funcionário com esse CPF não encontrado!");
            return;
        }
        if (!Pessoa.cpfValido(novoCpf)){
            System.out.println("ERRO: CPF inválido! Os dados não foram salvos.");
            return;
        }

        Funcionario outro = buscarPorCpf(novoCpf);
        if (outro != null && outro != funcionario){
            System.out.println("ERRO: Já existe um funcionário com esse CPF! Os dados não foram salvos.");
            return;
        }

        funcionario.setNome(novoNome);
        funcionario.setCpf(novoCpf);
        funcionario.setTelefone(novoTelefone);
        funcionario.setEmail(novoEmail);
        funcionario.setCargo(novoCargo);
        System.out.println("Dados alterados com sucesso!");
    }

    public void remover(String cpf){
        Funcionario funcionario = buscarPorCpf(cpf);

        if (funcionario != null){
            funcionarios.remove(funcionario);
            System.out.println("Funcionário removido com sucesso!");
        } else {
            System.out.println("ERRO: Funcionário com esse CPF não encontrado!");
        }
    }

    // para as Partes 4 e 5 usarem
    public ArrayList<Funcionario> getFuncionarios(){
        return new ArrayList<Funcionario>(funcionarios);
    }
}
