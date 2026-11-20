package Clientes;

import Carros.Eletrico;
import Carros.Passeio;
import Carros.SUV;
import Carros.Utilitario;

import java.time.LocalDate;

public class TesteSemana4 {
    private static int acertos = 0;
    private static int total = 0;

    private static void conferir(String descricao, boolean esperado, boolean obtido){
        total++;
        if (esperado == obtido){
            acertos++;
            System.out.println("[OK]    " + descricao);
        } else {
            System.out.println("[FALHA] " + descricao + " (esperado " + esperado + ", obtido " + obtido + ")");
        }
    }

    private static CNH cnhValida(int anosDeHabilitacao){
        return new CNH("12345678901", "B", LocalDate.now().minusYears(anosDeHabilitacao), LocalDate.now().plusYears(3));
    }

    public static void main(String[] args){
        // ================= CPF =================
        System.out.println("=== Teste de CPF ===");
        conferir("CPF válido com pontuação (111.444.777-35)", true, Pessoa.cpfValido("111.444.777-35"));
        conferir("CPF válido só números (12345678909)", true, Pessoa.cpfValido("12345678909"));
        conferir("CPF com dígito verificador errado", false, Pessoa.cpfValido("111.444.777-36"));
        conferir("CPF com todos os números iguais", false, Pessoa.cpfValido("111.111.111-11"));
        conferir("CPF com poucos dígitos", false, Pessoa.cpfValido("123"));
        conferir("CPF com letras", false, Pessoa.cpfValido("abc.def.ghi-jk"));
        conferir("CPF nulo", false, Pessoa.cpfValido(null));

        System.out.println("\n-- Encapsulamento: o CPF só muda depois de validado --");
        Pessoa p = new Pessoa("Maria Souza", "12345678909", "86999990000", "maria@email.com");
        p.setCpf("00000000000");
        conferir("CPF inválido não substitui o antigo", true, p.getCpf().equals("12345678909"));
        p.setCpf("111.444.777-35");
        conferir("CPF válido substitui o antigo", true, p.getCpf().equals("11144477735"));
        System.out.println("CPF formatado: " + p.getCpfFormatado());

        // ================= CNH =================
        System.out.println("\n=== Teste de CNH ===");
        CadastroClientes cadastro = new CadastroClientes();
        LocalDate nasc = LocalDate.now().minusYears(30);

        CNH vencida = new CNH("12345678901", "B", LocalDate.now().minusYears(10), LocalDate.now().minusDays(1));
        conferir("CNH que venceu ontem está vencida", true, vencida.isVencida());
        conferir("CNH com validade futura não está vencida", false, cnhValida(5).isVencida());

        Cliente comCnhVencida = new Cliente("Joao Vencido", "52998224725", "86988887777", "joao@email.com", nasc, vencida);
        conferir("Cadastro recusa CNH vencida", false, cadastro.cadastrar(comCnhVencida));

        Cliente cpfInvalido = new Cliente("Ana CPF Ruim", "11111111111", "86988887777", "ana@email.com", nasc, cnhValida(5));
        conferir("Cadastro recusa CPF inválido", false, cadastro.cadastrar(cpfInvalido));

        Cliente menor = new Cliente("Pedro Menor", "52998224725", "86988887777", "pedro@email.com",
                LocalDate.now().minusYears(17), cnhValida(0));
        conferir("Cadastro recusa menor de 18 anos", false, cadastro.cadastrar(menor));

        Cliente ok = new Cliente("Carlos Lima", "52998224725", "86988887777", "carlos@email.com", nasc, cnhValida(5));
        conferir("Cadastro aceita cliente válido", true, cadastro.cadastrar(ok));

        Cliente duplicado = new Cliente("Carlos Clone", "529.982.247-25", "86977776666", "clone@email.com", nasc, cnhValida(5));
        conferir("Cadastro recusa CPF repetido", false, cadastro.cadastrar(duplicado));

        // ============ CNH x tipo de carro ============
        System.out.println("\n=== Teste: pode dirigir o carro escolhido? ===");
        Passeio passeio = new Passeio("ABC1D23", "Fiat", "Argo", 2023, 20000, 100);
        SUV suv = new SUV("RSK1J13", "Volkswagen", "Nivus", 2022, 35000, 120);
        Eletrico eletrico = new Eletrico("SLS9H69", "Peugeot", "2008e GT", 2022, 45000, 120, 220);
        Utilitario utilitario = new Utilitario("UKF2G68", "RAM", "Rampage", 2026, 21000, 150, 1000);

        Cliente experiente = new Cliente("Experiente", "11144477735", "86999990000", "e@email.com", nasc, cnhValida(5));
        conferir("30 anos, CNH há 5 anos -> SUV", true, experiente.podeDirigir(suv));
        conferir("30 anos, CNH há 5 anos -> elétrico", true, experiente.podeDirigir(eletrico));

        Cliente jovem = new Cliente("Jovem 19", "12345678909", "86999990000", "j@email.com",
                LocalDate.now().minusYears(19), cnhValida(1));
        conferir("19 anos -> passeio", true, jovem.podeDirigir(passeio));
        conferir("19 anos -> utilitário", true, jovem.podeDirigir(utilitario));
        conferir("19 anos -> SUV (recusa)", false, jovem.podeDirigir(suv));
        conferir("19 anos -> elétrico (recusa)", false, jovem.podeDirigir(eletrico));
        System.out.println("Motivo: " + jovem.motivoNaoPodeDirigir(suv));

        Cliente novatoMaior = new Cliente("Novato 25", "52998224725", "86999990000", "n@email.com",
                LocalDate.now().minusYears(25), cnhValida(1));
        conferir("25 anos, CNH há 1 ano -> SUV (recusa)", false, novatoMaior.podeDirigir(suv));
        System.out.println("Motivo: " + novatoMaior.motivoNaoPodeDirigir(suv));
        conferir("25 anos, CNH há 1 ano -> passeio", true, novatoMaior.podeDirigir(passeio));

        Cliente limite = new Cliente("Limite", "11144477735", "86999990000", "l@email.com",
                LocalDate.now().minusYears(21), cnhValida(2));
        conferir("exatamente 21 anos e CNH há 2 anos -> SUV", true, limite.podeDirigir(suv));

        Cliente motoqueiro = new Cliente("Motoqueiro", "12345678909", "86999990000", "m@email.com", nasc,
                new CNH("12345678901", "A", LocalDate.now().minusYears(8), LocalDate.now().plusYears(2)));
        conferir("CNH categoria A -> passeio (recusa)", false, motoqueiro.podeDirigir(passeio));

        // ============ Fidelidade ============
        System.out.println("\n=== Teste de fidelidade ===");
        Cliente fiel = new Cliente("Fiel", "52998224725", "86999990000", "f@email.com", nasc, cnhValida(5));
        conferir("começa no Bronze", true, fiel.getNivel() == NivelFidelidade.BRONZE);
        conferir("Bronze não dá desconto", true, fiel.aplicarDesconto(1000) == 1000);

        fiel.registrarAluguel(4999.99);   // 499 pontos (1 ponto a cada R$ 10,00)
        conferir("R$ 4.999,99 pagos = 499 pontos", true, fiel.getPontos() == 499);
        conferir("499 pontos continua no Bronze", true, fiel.getNivel() == NivelFidelidade.BRONZE);
        fiel.registrarAluguel(10);        // 500 pontos
        conferir("500 pontos sobe para Prata", true, fiel.getNivel() == NivelFidelidade.PRATA);
        conferir("Prata dá 5% de desconto", true, Math.abs(fiel.aplicarDesconto(1000) - 950) < 0.001);
        fiel.registrarAluguel(10000);     // 1.500 pontos
        conferir("1.500 pontos sobe para Ouro", true, fiel.getNivel() == NivelFidelidade.OURO);
        conferir("Ouro dá 10% de desconto", true, Math.abs(fiel.aplicarDesconto(1000) - 900) < 0.001);
        fiel.registrarAluguel(15000);     // 3.000 pontos
        conferir("3.000 pontos sobe para Diamante", true, fiel.getNivel() == NivelFidelidade.DIAMANTE);
        conferir("Diamante dá 15% de desconto", true, Math.abs(fiel.aplicarDesconto(1000) - 850) < 0.001);
        fiel.registrarAluguel(0);
        conferir("R$ 0 pagos não muda os pontos", true, fiel.getPontos() == 3000);

        // ============ Bloqueio ============
        System.out.println("\n=== Teste de bloqueio ===");
        Cliente devedor = new Cliente("Devedor", "11144477735", "86999990000", "d@email.com", nasc, cnhValida(5));
        conferir("cliente em dia pode alugar", true, devedor.podeAlugar(passeio));

        devedor.registrarDebito(300, "Multa por atraso");
        conferir("cliente com multa fica bloqueado", true, devedor.isBloqueado());
        conferir("cliente bloqueado não pode alugar", false, devedor.podeAlugar(passeio));
        System.out.println("Motivo: " + devedor.motivoNaoPodeAlugar(passeio));

        devedor.quitarDebito(100);
        conferir("pagou só uma parte: continua bloqueado", true, devedor.isBloqueado());
        devedor.quitarDebito(200);
        conferir("quitou tudo: desbloqueado", false, devedor.isBloqueado());
        conferir("desbloqueado pode alugar de novo", true, devedor.podeAlugar(passeio));

        devedor.registrarDebito(800, "Avaria no para-choque");
        conferir("avaria também bloqueia", true, devedor.isBloqueado());

        // ============ CRUD ============
        System.out.println("\n=== Teste do cadastro (editar / remover / listar) ===");
        cadastro.cadastrar(experiente);
        cadastro.editar("529.982.247-25", "Carlos Lima Filho", "52998224725", "86911112222", "novo@email.com");
        conferir("editar alterou o nome", true, cadastro.buscarPorCpf("52998224725").getNome().equals("Carlos Lima Filho"));

        cadastro.editar("52998224725", "Nome Que Nao Deve Salvar", "11111111111", "86900000000", "outro@email.com");
        conferir("editar com CPF inválido não salva nada (nome)", true,
                cadastro.buscarPorCpf("52998224725").getNome().equals("Carlos Lima Filho"));
        conferir("editar com CPF inválido não salva nada (telefone)", true,
                cadastro.buscarPorCpf("52998224725").getTelefone().equals("86911112222"));

        cadastro.editar("52998224725", "Carlos Troca CPF", "123.456.789-09", "86911112222", "novo@email.com");
        conferir("editar com CPF válido troca o CPF", true, cadastro.buscarPorCpf("12345678909") != null);
        cadastro.editar("12345678909", "Carlos Lima Filho", "52998224725", "86911112222", "novo@email.com");

        cadastro.editar("00000000000", "Ninguem", "52998224725", "86911112222", "x@email.com");
        cadastro.listar();
        cadastro.remover("52998224725");
        conferir("remover tirou o cliente", true, cadastro.buscarPorCpf("52998224725") == null);
        cadastro.remover("52998224725");

        // ============ Funcionário ============
        System.out.println("\n=== Funcionário (herança de Pessoa) ===");
        Funcionario f = new Funcionario("Rita Atendente", "12345678909", "86999990000", "rita@roda.com", "F001", "Atendente");
        System.out.println(f.getNome() + " | " + f.getCargo() + " | CPF " + f.getCpfFormatado() + " | matrícula " + f.getMatricula());

        CadastroFuncionarios equipe = new CadastroFuncionarios();
        conferir("Cadastro aceita funcionário válido", true, equipe.cadastrar(f));
        conferir("Cadastro recusa CPF repetido", false, equipe.cadastrar(
                new Funcionario("Rita Clone", "123.456.789-09", "86999990000", "clone@roda.com", "F002", "Atendente")));
        conferir("Cadastro recusa matrícula repetida", false, equipe.cadastrar(
                new Funcionario("Paulo", "52998224725", "86999990000", "paulo@roda.com", "F001", "Atendente")));
        conferir("Cadastro recusa CPF inválido", false, equipe.cadastrar(
                new Funcionario("Ana", "11111111111", "86999990000", "ana@roda.com", "F003", "Atendente")));

        equipe.editar("12345678909", "Rita Silva", "00000000000", "86911112222", "rita2@roda.com", "Gerente");
        conferir("editar funcionário com CPF inválido não salva nada", true,
                equipe.buscarPorCpf("12345678909").getNome().equals("Rita Atendente"));
        equipe.editar("12345678909", "Rita Silva", "12345678909", "86911112222", "rita2@roda.com", "Gerente");
        conferir("editar funcionário com CPF válido salva", true,
                equipe.buscarPorCpf("12345678909").getCargo().equals("Gerente"));
        equipe.listar();
        equipe.remover("12345678909");
        conferir("remover tirou o funcionário", true, equipe.buscarPorCpf("12345678909") == null);

        System.out.println("\n=== RESULTADO: " + acertos + "/" + total + " testes passaram ===");
    }
}
