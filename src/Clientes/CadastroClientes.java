package Clientes;

import java.util.ArrayList;

public class CadastroClientes {
    private ArrayList<Cliente> clientes;

    public CadastroClientes(){
        clientes = new ArrayList<Cliente>();
    }

    // devolve true se cadastrou (as outras partes podem usar o resultado)
    public boolean cadastrar(Cliente cliente){
        if (cliente == null){
            System.out.println("ERRO: Cliente inválido!");
            return false;
        }
        if (cliente.getCpf() == null){
            System.out.println("ERRO: Não é possível cadastrar cliente com CPF inválido!");
            return false;
        }
        if (cliente.getDataNascimento() == null){
            System.out.println("ERRO: Não é possível cadastrar cliente sem data de nascimento válida!");
            return false;
        }
        if (cliente.getCnh() == null){
            System.out.println("ERRO: Não é possível cadastrar cliente sem CNH!");
            return false;
        }
        if (cliente.getCnh().isVencida()){
            System.out.println("ERRO: Não é possível cadastrar cliente com CNH vencida!");
            return false;
        }
        if (buscarPorCpf(cliente.getCpf()) != null){
            System.out.println("ERRO: Já existe um cliente com esse CPF!");
            return false;
        }

        clientes.add(cliente);
        System.out.println("Cliente cadastrado com sucesso!");
        return true;
    }

    public Cliente buscarPorCpf(String cpf){
        String digitos = Pessoa.apenasDigitos(cpf);

        for (Cliente c : clientes){
            if (c.getCpf().equals(digitos)){
                return c;
            }
        }
        return null;
    }

    public void listar(){
        if (clientes.isEmpty()){
            System.out.println("Nenhum cliente cadastrado.");
            return;
        }

        for (Cliente c : clientes){
            System.out.println("Nome: " + c.getNome());
            System.out.println("CPF: " + c.getCpfFormatado());
            System.out.println("Telefone: " + c.getTelefone());
            System.out.println("E-mail: " + c.getEmail());
            System.out.println("Idade: " + c.getIdade());
            System.out.println("CNH: " + c.getCnh().getNumero() + " | Cat. " + c.getCnh().getCategoria()
                    + " | Validade: " + c.getCnh().getValidade());
            System.out.println("Fidelidade: " + c.getNivel().getNome() + " (" + c.getPontos() + " pontos)");
            System.out.println("Situação: " + (c.isBloqueado() ? "BLOQUEADO - " + c.getMotivoDebito() : "Liberado"));
            System.out.println("-----------------------------");
        }
    }

    // mantém o CPF e muda os outros dados
    public void editar(String cpf, String novoNome, String novoTelefone, String novoEmail){
        editar(cpf, novoNome, cpf, novoTelefone, novoEmail);
    }

    // o CPF novo é validado antes de salvar: se for inválido, NENHUM dado é alterado
    public void editar(String cpf, String novoNome, String novoCpf, String novoTelefone, String novoEmail){
        Cliente cliente = buscarPorCpf(cpf);

        if (cliente == null){
            System.out.println("ERRO: Cliente com esse CPF não encontrado!");
            return;
        }
        if (!Pessoa.cpfValido(novoCpf)){
            System.out.println("ERRO: CPF inválido! Os dados não foram salvos.");
            return;
        }

        Cliente outro = buscarPorCpf(novoCpf);
        if (outro != null && outro != cliente){
            System.out.println("ERRO: Já existe um cliente com esse CPF! Os dados não foram salvos.");
            return;
        }

        cliente.setNome(novoNome);
        cliente.setCpf(novoCpf);
        cliente.setTelefone(novoTelefone);
        cliente.setEmail(novoEmail);
        System.out.println("Dados alterados com sucesso!");
    }

    // renovação de CNH: só aceita se a nova não estiver vencida
    public void atualizarCnh(String cpf, CNH novaCnh){
        Cliente cliente = buscarPorCpf(cpf);

        if (cliente != null){
            cliente.setCnh(novaCnh);
        } else {
            System.out.println("ERRO: Cliente com esse CPF não encontrado!");
        }
    }

    public void remover(String cpf){
        Cliente cliente = buscarPorCpf(cpf);

        if (cliente != null){
            clientes.remove(cliente);
            System.out.println("Cliente removido com sucesso!");
        } else {
            System.out.println("ERRO: Cliente com esse CPF não encontrado!");
        }
    }

    // para as Partes 3, 4 e 5 usarem (ex.: salvar em JSON)
    public ArrayList<Cliente> getClientes(){
        return new ArrayList<Cliente>(clientes);
    }
}
