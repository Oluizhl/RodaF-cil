package Telas;

import Alugueis.Locacao;
import Clientes.CNH;
import Clientes.Cliente;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TelaClientes extends Tela {
    private Sistema sistema;

    public TelaClientes(Sistema sistema){
        super("Clientes");
        this.sistema = sistema;
    }

    @Override
    public String[] getOpcoes(){
        return new String[]{
                "Cadastrar cliente",
                "Listar clientes",
                "Buscar por CPF",
                "Editar dados",
                "Renovar CNH",
                "Quitar débito (desbloquear)",
                "Ver aluguéis do cliente",
                "Remover cliente"
        };
    }

    @Override
    public void executar(int opcao){
        switch (opcao){
            case 1: cadastrar(); break;
            case 2: sistema.getClientes().listar(); break;
            case 3: buscar(); break;
            case 4: editar(); break;
            case 5: renovarCnh(); break;
            case 6: quitarDebito(); break;
            case 7: verAlugueis(); break;
            case 8: remover(); break;
        }
    }

    private void cadastrar(){
        String cpf = Entrada.lerCpf("CPF");
        if (sistema.getClientes().buscarPorCpf(cpf) != null){
            System.out.println("ERRO: Já existe um cliente com esse CPF!");
            return;
        }

        String nome = Entrada.lerTexto("Nome completo");
        String telefone = Entrada.lerTelefone("Telefone");
        String email = Entrada.lerEmail("E-mail");
        LocalDate nascimento = lerNascimento();

        System.out.println("--- Dados da CNH ---");
        CNH cnh = lerCnh();

        Cliente cliente = new Cliente(nome, cpf, telefone, email, nascimento, cnh);
        if (sistema.getClientes().cadastrar(cliente)){
            sistema.registrarAcao("Sistema", "CADASTRO DE CLIENTE", nome + " - CPF " + cliente.getCpfFormatado());
        }
    }

    private void buscar(){
        Cliente cliente = escolherCliente("CPF do cliente");
        if (cliente != null){
            mostrar(cliente);
        }
    }

    private void editar(){
        Cliente cliente = escolherCliente("CPF do cliente");
        if (cliente == null){
            return;
        }
        mostrar(cliente);
        System.out.println("--- Novos dados ---");
        String nome = Entrada.lerTexto("Nome");
        String telefone = Entrada.lerTelefone("Telefone");
        String email = Entrada.lerEmail("E-mail");

        sistema.getClientes().editar(cliente.getCpf(), nome, telefone, email);
    }

    private void renovarCnh(){
        Cliente cliente = escolherCliente("CPF do cliente");
        if (cliente == null){
            return;
        }
        System.out.println("--- Dados da nova CNH ---");
        sistema.getClientes().atualizarCnh(cliente.getCpf(), lerCnh());
        System.out.println("CNH atualizada!");
    }

    private void quitarDebito(){
        Cliente cliente = escolherCliente("CPF do cliente");
        if (cliente == null){
            return;
        }
        if (!cliente.isBloqueado()){
            System.out.println("Esse cliente não tem débitos.");
            return;
        }
        System.out.println("Débito: R$ " + String.format("%.2f", cliente.getDebito()) + " (" + cliente.getMotivoDebito() + ")");
        double valor = Entrada.lerDecimal("Valor pago (R$)");
        cliente.quitarDebito(valor);
        if (cliente.isBloqueado()){
            System.out.println("Ainda falta: R$ " + String.format("%.2f", cliente.getDebito()));
        }
    }

    private void verAlugueis(){
        Cliente cliente = escolherCliente("CPF do cliente");
        if (cliente == null){
            return;
        }
        ArrayList<Locacao> lista = sistema.getLocacoes().buscarPorCliente(cliente);
        if (lista.isEmpty()){
            System.out.println("Esse cliente ainda não alugou nenhum carro.");
        }
        for (Locacao l : lista){
            System.out.println(l);
        }
    }

    private void remover(){
        Cliente cliente = escolherCliente("CPF do cliente");
        if (cliente == null){
            return;
        }
        for (Locacao l : sistema.getLocacoes().buscarPorCliente(cliente)){
            if (l.isEmAndamento()){
                System.out.println("ERRO: O cliente tem uma locação reservada ou ativa (nº " + l.getId() + ")!");
                return;
            }
        }
        if (Entrada.lerSimNao("Remover " + cliente.getNome() + "?")){
            sistema.getClientes().remover(cliente.getCpf());
        }
    }

    public Cliente escolherCliente(String pergunta){
        String cpf = Entrada.lerCpf(pergunta);
        Cliente cliente = sistema.getClientes().buscarPorCpf(cpf);
        if (cliente == null){
            System.out.println("ERRO: Cliente com esse CPF não encontrado!");
        }
        return cliente;
    }

    private LocalDate lerNascimento(){
        while (true){
            LocalDate data = Entrada.lerData("Data de nascimento");
            if (data.isAfter(LocalDate.now())){
                System.out.println("ERRO: A data de nascimento não pode ser no futuro!");
            } else if (Period.between(data, LocalDate.now()).getYears() < 18){
                System.out.println("ERRO: O cliente precisa ter 18 anos ou mais!");
            } else {
                return data;
            }
        }
    }

    private CNH lerCnh(){
        String numero;
        while (true){
            numero = Entrada.lerTexto("Número da CNH (11 dígitos)").replaceAll("[^0-9]", "");
            if (numero.length() == 11){
                break;
            }
            System.out.println("ERRO: O número da CNH deve ter 11 dígitos!");
        }

        String categoria;
        while (true){
            categoria = Entrada.lerTexto("Categoria (A, B, C, D, E, AB, AC, AD ou AE)").toUpperCase();
            if (categoria.matches("^(A|B|C|D|E|AB|AC|AD|AE)$")){
                break;
            }
            System.out.println("ERRO: Categoria inválida!");
        }

        LocalDate primeira;
        while (true){
            primeira = Entrada.lerData("Data da primeira habilitação");
            if (!primeira.isAfter(LocalDate.now())){
                break;
            }
            System.out.println("ERRO: A data não pode ser no futuro!");
        }

        LocalDate validade;
        while (true){
            validade = Entrada.lerData("Validade da CNH");
            if (!validade.isBefore(LocalDate.now())){
                break;
            }
            System.out.println("ERRO: Essa CNH está vencida!");
        }

        return new CNH(numero, categoria, primeira, validade);
    }

    private void mostrar(Cliente c){
        System.out.println("Nome: " + c.getNome() + " | CPF: " + c.getCpfFormatado());
        System.out.println("Telefone: " + c.getTelefone() + " | E-mail: " + c.getEmail());
        System.out.println("Idade: " + c.getIdade() + " anos");
        System.out.println("CNH: " + c.getCnh().getNumero() + " | Cat. " + c.getCnh().getCategoria()
                + " | Validade: " + c.getCnh().getValidade().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + (c.getCnh().isVencida() ? " (VENCIDA)" : ""));
        System.out.println("Fidelidade: " + c.getNivel().getNome() + " (" + c.getPontos() + " pontos, "
                + (int) Math.round(c.getDescontoFidelidade() * 100) + "% de desconto)");
        System.out.println("Situação: " + (c.isBloqueado()
                ? "BLOQUEADO - deve R$ " + String.format("%.2f", c.getDebito()) + " (" + c.getMotivoDebito() + ")"
                : "Liberado"));
    }
}
