package Alugueis;

import Carros.Eletrico;
import Carros.Passeio;
import Carros.SUV;
import Carros.Veiculo;
import Clientes.CNH;
import Clientes.Cliente;

import java.time.LocalDate;
import java.util.ArrayList;

public class TesteSemana4 {
    private static int acertos = 0;
    private static int total = 0;
    private static final LocalDate HOJE = LocalDate.now();

    private static void conferir(String descricao, boolean esperado, boolean obtido){
        total++;
        if (esperado == obtido){
            acertos++;
            System.out.println("[OK]    " + descricao);
        } else {
            System.out.println("[FALHA] " + descricao + " (esperado " + esperado + ", obtido " + obtido + ")");
        }
    }

    private static void conferirValor(String descricao, double esperado, double obtido){
        total++;
        if (Math.abs(esperado - obtido) < 0.01){
            acertos++;
            System.out.println("[OK]    " + descricao + " = " + String.format("%.2f", obtido));
        } else {
            System.out.println("[FALHA] " + descricao + " (esperado " + String.format("%.2f", esperado)
                    + ", obtido " + String.format("%.2f", obtido) + ")");
        }
    }

    // compara o nome do erro que aconteceu com o que era esperado ("nenhum" se não deu erro)
    private static void conferirErro(String descricao, String esperado, String obtido){
        total++;
        if (esperado.equals(obtido)){
            acertos++;
            System.out.println("[OK]    " + descricao + " -> " + obtido);
        } else {
            System.out.println("[FALHA] " + descricao + " (esperado " + esperado + ", obtido " + obtido + ")");
        }
    }

    // ---------- objetos de teste ----------

    private static Cliente novoCliente(String cpf, int idade, int anosDeCnh){
        CNH cnh = new CNH("12345678901", "B", HOJE.minusYears(anosDeCnh), HOJE.plusYears(3));
        return new Cliente("Cliente Teste", cpf, "86999990000", "teste@email.com", HOJE.minusYears(idade), cnh);
    }

    private static Cliente maria(){
        return novoCliente("12345678909", 30, 5);
    }

    private static Passeio argo(){
        return new Passeio("ABC1D23", "Fiat", "Argo", 2023, 20000, 100);
    }

    private static Passeio onix(){
        return new Passeio("QOQ9G12", "Chevrolet", "Onix", 2023, 41000, 100);
    }

    // valor total de um aluguel que começa hoje
    private static double valor(Cliente c, Veiculo v, int dias, Seguro s, FormaPagamento p, ArrayList<Adicional> adicionais)
            throws LocadoraException {
        return SimuladorOrcamento.simular(c, v, HOJE, HOJE.plusDays(dias), s, p, adicionais).getTotal();
    }

    private static double valor(Cliente c, Veiculo v, int dias, Seguro s, FormaPagamento p) throws LocadoraException {
        return valor(c, v, dias, s, p, new ArrayList<Adicional>());
    }

    // ---------- os métodos abaixo tentam a operação e devolvem o nome do erro ----------

    private static String erroAoReservar(GerenciadorLocacoes g, Cliente c, Veiculo v, LocalDate ini, LocalDate fim){
        try {
            g.reservar(c, v, ini, fim, new SemSeguro(), new PagamentoDinheiro());
            return "nenhum";
        } catch (LocadoraException e){
            return e.getClass().getSimpleName();
        }
    }

    private static String erroAoRetirar(GerenciadorLocacoes g, int id){
        try {
            g.retirar(id);
            return "nenhum";
        } catch (LocadoraException e){
            return e.getClass().getSimpleName();
        }
    }

    private static String erroAoDevolver(GerenciadorLocacoes g, int id, LocalDate data, int km, double litros){
        try {
            g.devolver(id, data, km, litros);
            return "nenhum";
        } catch (LocadoraException e){
            return e.getClass().getSimpleName();
        }
    }

    private static String erroAoCancelar(GerenciadorLocacoes g, int id){
        try {
            g.cancelar(id);
            return "nenhum";
        } catch (LocadoraException e){
            return e.getClass().getSimpleName();
        }
    }

    private static String erroAoCriarCartao(int parcelas){
        try {
            new PagamentoCartao(parcelas);
            return "nenhum";
        } catch (LocadoraException e){
            return e.getClass().getSimpleName();
        }
    }

    public static void main(String[] args) throws LocadoraException {
        testarDatasQueSeCruzam();
        testarValorDoAluguel();
        testarFormasDePagamento();
        testarDevolucao();
        testarErros();
        testarSimulador();

        System.out.println();
        System.out.println(acertos + " de " + total + " testes passaram.");
    }

    // ================= datas que se cruzam =================
    private static void testarDatasQueSeCruzam() throws LocadoraException {
        System.out.println("=== Teste de datas que se cruzam ===");
        Cliente cliente = maria();
        Passeio carro = argo();
        GerenciadorLocacoes g = new GerenciadorLocacoes();

        // reserva-base: do dia +10 ao dia +15
        Locacao base = g.reservar(cliente, carro, HOJE.plusDays(10), HOJE.plusDays(15), new SemSeguro(), new PagamentoDinheiro());

        conferirErro("Começa antes e termina no meio (+5 a +12)", "ReservaConflitanteException",
                erroAoReservar(g, cliente, carro, HOJE.plusDays(5), HOJE.plusDays(12)));
        conferirErro("Começa no meio e termina depois (+12 a +20)", "ReservaConflitanteException",
                erroAoReservar(g, cliente, carro, HOJE.plusDays(12), HOJE.plusDays(20)));
        conferirErro("Fica dentro da reserva (+11 a +13)", "ReservaConflitanteException",
                erroAoReservar(g, cliente, carro, HOJE.plusDays(11), HOJE.plusDays(13)));
        conferirErro("Pega a reserva inteira (+5 a +20)", "ReservaConflitanteException",
                erroAoReservar(g, cliente, carro, HOJE.plusDays(5), HOJE.plusDays(20)));
        conferirErro("Mesmas datas (+10 a +15)", "ReservaConflitanteException",
                erroAoReservar(g, cliente, carro, HOJE.plusDays(10), HOJE.plusDays(15)));

        conferirErro("Devolve no dia em que a outra retira (+5 a +10)", "nenhum",
                erroAoReservar(g, cliente, carro, HOJE.plusDays(5), HOJE.plusDays(10)));
        conferirErro("Retira no dia em que a outra devolve (+15 a +20)", "nenhum",
                erroAoReservar(g, cliente, carro, HOJE.plusDays(15), HOJE.plusDays(20)));
        conferirErro("Outro carro nas mesmas datas", "nenhum",
                erroAoReservar(g, cliente, onix(), HOJE.plusDays(10), HOJE.plusDays(15)));

        conferir("estaDisponivel em +11 a +13", false, g.estaDisponivel(carro, HOJE.plusDays(11), HOJE.plusDays(13)));
        conferir("estaDisponivel em +20 a +25", true, g.estaDisponivel(carro, HOJE.plusDays(20), HOJE.plusDays(25)));

        // reserva cancelada não segura mais o carro
        g.cancelar(base.getId());
        conferir("Reserva cancelada fica CANCELADA", true, base.getSituacao() == SituacaoLocacao.CANCELADA);
        conferirErro("Mesmas datas depois de cancelar", "nenhum",
                erroAoReservar(g, cliente, carro, HOJE.plusDays(10), HOJE.plusDays(15)));
    }

    // ================= valor do aluguel =================
    private static void testarValorDoAluguel() throws LocadoraException {
        System.out.println();
        System.out.println("=== Teste do valor (carro de R$ 100 por dia, pagando em dinheiro) ===");
        Cliente cliente = maria();
        Passeio carro = argo();
        Seguro sem = new SemSeguro();
        FormaPagamento dinheiro = new PagamentoDinheiro();

        conferirValor("1 dia", 100, valor(cliente, carro, 1, sem, dinheiro));
        conferirValor("6 dias (sem desconto)", 600, valor(cliente, carro, 6, sem, dinheiro));
        conferirValor("7 dias (5% de desconto)", 665, valor(cliente, carro, 7, sem, dinheiro));
        conferirValor("15 dias (10% de desconto)", 1350, valor(cliente, carro, 15, sem, dinheiro));
        conferirValor("29 dias (10% de desconto)", 2610, valor(cliente, carro, 29, sem, dinheiro));
        conferirValor("30 dias (15% de desconto)", 2550, valor(cliente, carro, 30, sem, dinheiro));

        System.out.println();
        SUV suv = new SUV("PIR9D53", "Nissan", "Kicks", 2022, 30000, 100);
        Eletrico eletrico = new Eletrico("SLS9H69", "Peugeot", "2008e GT", 2022, 43000, 100, 500);
        conferirValor("5 dias de Passeio", 500, valor(cliente, carro, 5, sem, dinheiro));
        conferirValor("5 dias de SUV (diária 110)", 550, valor(cliente, suv, 5, sem, dinheiro));
        conferirValor("5 dias de Elétrico (diária 85)", 425, valor(cliente, eletrico, 5, sem, dinheiro));

        System.out.println();
        conferirValor("Seguro básico (5 dias)", 550, valor(cliente, carro, 5, new SeguroBasico(), dinheiro));
        conferirValor("Seguro completo (5 dias)", 675, valor(cliente, carro, 5, new SeguroCompleto(), dinheiro));

        System.out.println();
        ArrayList<Adicional> gps = new ArrayList<>();
        gps.add(Adicional.GPS);
        ArrayList<Adicional> todos = new ArrayList<>();
        todos.add(Adicional.GPS);
        todos.add(Adicional.CADEIRINHA);
        todos.add(Adicional.CONDUTOR_EXTRA);
        conferirValor("3 dias com GPS", 345, valor(cliente, carro, 3, sem, dinheiro, gps));
        conferirValor("3 dias com GPS, cadeirinha e condutor extra", 441, valor(cliente, carro, 3, sem, dinheiro, todos));

        System.out.println();
        Cliente fiel = novoCliente("11144477735", 35, 10);
        fiel.registrarAluguel(60); // 600 pontos = Diamante
        conferirValor("5 dias para cliente Diamante (15% de fidelidade)", 425, valor(fiel, carro, 5, sem, dinheiro));
        conferirValor("30 dias para cliente Diamante", 2167.50, valor(fiel, carro, 30, sem, dinheiro));
    }

    // ================= formas de pagamento =================
    private static void testarFormasDePagamento() throws LocadoraException {
        System.out.println();
        System.out.println("=== Teste de cada forma de pagamento (5 dias, R$ 500) ===");
        Cliente cliente = maria();
        Passeio carro = argo();
        Seguro sem = new SemSeguro();

        conferirValor("Dinheiro", 500, valor(cliente, carro, 5, sem, new PagamentoDinheiro()));
        conferirValor("Pix (5% de desconto)", 475, valor(cliente, carro, 5, sem, new PagamentoPix()));
        conferirValor("Cartão em 1x", 500, valor(cliente, carro, 5, sem, new PagamentoCartao(1)));
        conferirValor("Cartão em 3x (sem juros)", 500, valor(cliente, carro, 5, sem, new PagamentoCartao(3)));
        conferirValor("Cartão em 6x (9% de juros)", 545, valor(cliente, carro, 5, sem, new PagamentoCartao(6)));
        conferirValor("Cartão em 12x (18% de juros)", 590, valor(cliente, carro, 5, sem, new PagamentoCartao(12)));
        conferirValor("Parcela de R$ 500 em 3x", 166.67, new PagamentoCartao(3).calcularParcela(500));

        conferirErro("Cartão em 0 parcelas", "PagamentoInvalidoException", erroAoCriarCartao(0));
        conferirErro("Cartão em 13 parcelas", "PagamentoInvalidoException", erroAoCriarCartao(13));

        // mesma chamada, resultado diferente em cada forma (polimorfismo)
        System.out.println();
        FormaPagamento[] formas = { new PagamentoPix(), new PagamentoDinheiro(), new PagamentoCartao(12) };
        for (FormaPagamento f : formas){
            System.out.println(f.descrever(1000));
        }
    }

    // ================= devolução: atraso, km e combustível =================
    private static void testarDevolucao() throws LocadoraException {
        System.out.println();
        System.out.println("=== Teste da devolução (5 dias, R$ 100 por dia, km inicial 20000) ===");

        // no prazo
        Cliente c1 = maria();
        Passeio carro1 = argo();
        GerenciadorLocacoes g1 = new GerenciadorLocacoes();
        Locacao l1 = g1.reservar(c1, carro1, HOJE, HOJE.plusDays(5), new SemSeguro(), new PagamentoDinheiro());
        g1.retirar(l1.getId());
        conferir("Depois de retirar, o carro fica Alugado", true, carro1.getSituacao().equals("Alugado"));
        conferir("Depois de retirar, a locação fica ATIVA", true, l1.getSituacao() == SituacaoLocacao.ATIVA);

        Fechamento f1 = g1.devolver(l1.getId(), HOJE.plusDays(5), 20500, 0);
        conferirValor("No prazo: dias de atraso", 0, f1.getDiasAtraso());
        conferirValor("No prazo: extras", 0, f1.getTotalExtras());
        conferirValor("No prazo: total geral", 500, f1.getTotalGeral());
        conferir("Depois de devolver, o carro fica Livre", true, carro1.getSituacao().equals("Livre"));
        conferirValor("O km do carro é atualizado", 20500, carro1.getKm());
        conferir("A locação fica FINALIZADA", true, l1.getSituacao() == SituacaoLocacao.FINALIZADA);
        conferirValor("Pontos do cliente (10 por diária)", 50, c1.getPontos());

        // atraso de 2 dias
        System.out.println();
        Cliente c2 = maria();
        GerenciadorLocacoes g2 = new GerenciadorLocacoes();
        Locacao l2 = g2.reservar(c2, argo(), HOJE, HOJE.plusDays(5), new SemSeguro(), new PagamentoDinheiro());
        g2.retirar(l2.getId());
        Fechamento f2 = g2.devolver(l2.getId(), HOJE.plusDays(7), 20500, 0);
        conferirValor("Atraso de 2 dias: dias", 2, f2.getDiasAtraso());
        conferirValor("Atraso de 2 dias: multa (2 x 100 x 1,5)", 300, f2.getMultaAtraso());
        conferirValor("Atraso de 2 dias: total geral", 800, f2.getTotalGeral());

        // devolveu antes
        Cliente c3 = maria();
        GerenciadorLocacoes g3 = new GerenciadorLocacoes();
        Locacao l3 = g3.reservar(c3, argo(), HOJE, HOJE.plusDays(5), new SemSeguro(), new PagamentoDinheiro());
        g3.retirar(l3.getId());
        Fechamento f3 = g3.devolver(l3.getId(), HOJE.plusDays(4), 20500, 0);
        conferirValor("Devolveu 1 dia antes: multa", 0, f3.getMultaAtraso());

        // km a mais (franquia de 200 km por dia = 1000 km)
        System.out.println();
        Cliente c4 = maria();
        GerenciadorLocacoes g4 = new GerenciadorLocacoes();
        Locacao l4 = g4.reservar(c4, argo(), HOJE, HOJE.plusDays(5), new SemSeguro(), new PagamentoDinheiro());
        g4.retirar(l4.getId());
        Fechamento f4 = g4.devolver(l4.getId(), HOJE.plusDays(5), 21100, 0);
        conferirValor("Rodou 1100 km: km a mais", 100, f4.getKmExtra());
        conferirValor("Rodou 1100 km: cobrança (100 x 0,80)", 80, f4.getValorKmExtra());

        Cliente c5 = maria();
        GerenciadorLocacoes g5 = new GerenciadorLocacoes();
        Locacao l5 = g5.reservar(c5, argo(), HOJE, HOJE.plusDays(5), new SemSeguro(), new PagamentoDinheiro());
        g5.retirar(l5.getId());
        Fechamento f5 = g5.devolver(l5.getId(), HOJE.plusDays(5), 21000, 0);
        conferirValor("Rodou exatamente 1000 km: cobrança", 0, f5.getValorKmExtra());

        // combustível
        System.out.println();
        Cliente c6 = maria();
        GerenciadorLocacoes g6 = new GerenciadorLocacoes();
        Locacao l6 = g6.reservar(c6, argo(), HOJE, HOJE.plusDays(5), new SemSeguro(), new PagamentoDinheiro());
        g6.retirar(l6.getId());
        Fechamento f6 = g6.devolver(l6.getId(), HOJE.plusDays(5), 20500, 10);
        conferirValor("Faltaram 10 litros (10 x 7,50)", 75, f6.getValorCombustivel());

        // tudo junto, pago no Pix
        System.out.println();
        Cliente c7 = maria();
        GerenciadorLocacoes g7 = new GerenciadorLocacoes();
        Locacao l7 = g7.reservar(c7, argo(), HOJE, HOJE.plusDays(5), new SemSeguro(), new PagamentoPix());
        g7.retirar(l7.getId());
        Fechamento f7 = g7.devolver(l7.getId(), HOJE.plusDays(6), 21100, 10);
        conferirValor("Atraso + km + combustível (150 + 80 + 75)", 305, f7.getTotalExtras());
        conferirValor("Locação paga no Pix (500 - 5%)", 475, f7.getValorLocacao());
        conferirValor("Total geral (475 + 305)", 780, f7.getTotalGeral());
        System.out.println(f7);

        // extras que o cliente não pagou: ele fica bloqueado
        System.out.println();
        Cliente c8 = maria();
        Passeio carro8 = argo();
        GerenciadorLocacoes g8 = new GerenciadorLocacoes();
        Locacao l8 = g8.reservar(c8, carro8, HOJE, HOJE.plusDays(5), new SemSeguro(), new PagamentoDinheiro());
        g8.retirar(l8.getId());
        g8.devolver(l8.getId(), HOJE.plusDays(7), 20500, 0, false);
        conferir("Quem não pagou a multa fica bloqueado", true, c8.isBloqueado());
        conferirValor("Débito do cliente", 300, c8.getDebito());
        conferirErro("Cliente bloqueado tenta reservar", "ClienteBloqueadoException",
                erroAoReservar(g8, c8, carro8, HOJE.plusDays(10), HOJE.plusDays(12)));
        c8.quitarDebito(300);
        conferirErro("Reservar depois de quitar o débito", "nenhum",
                erroAoReservar(g8, c8, carro8, HOJE.plusDays(10), HOJE.plusDays(12)));
    }

    // ================= erros =================
    private static void testarErros() throws LocadoraException {
        System.out.println();
        System.out.println("=== Teste dos erros ===");
        Cliente cliente = maria();
        Passeio carro = argo();
        GerenciadorLocacoes g = new GerenciadorLocacoes();

        conferirErro("Retirada no passado", "DatasInvalidasException",
                erroAoReservar(g, cliente, carro, HOJE.minusDays(1), HOJE.plusDays(3)));
        conferirErro("Devolução antes da retirada", "DatasInvalidasException",
                erroAoReservar(g, cliente, carro, HOJE.plusDays(5), HOJE.plusDays(3)));
        conferirErro("Devolução no mesmo dia da retirada", "DatasInvalidasException",
                erroAoReservar(g, cliente, carro, HOJE.plusDays(5), HOJE.plusDays(5)));
        conferirErro("Data vazia", "DatasInvalidasException",
                erroAoReservar(g, cliente, carro, null, HOJE.plusDays(5)));

        System.out.println();
        Cliente devedor = novoCliente("52998224725", 40, 15);
        devedor.registrarDebito(200, "multa de trânsito");
        conferirErro("Cliente com multa", "ClienteBloqueadoException",
                erroAoReservar(g, devedor, carro, HOJE.plusDays(1), HOJE.plusDays(3)));

        Cliente novato = novoCliente("11144477735", 19, 1);
        SUV suv = new SUV("PIR9D53", "Nissan", "Kicks", 2022, 30000, 100);
        conferirErro("Cliente de 19 anos num SUV", "ClienteNaoHabilitadoException",
                erroAoReservar(g, novato, suv, HOJE.plusDays(1), HOJE.plusDays(3)));
        conferirErro("Cliente de 19 anos num Passeio", "nenhum",
                erroAoReservar(g, novato, carro, HOJE.plusDays(20), HOJE.plusDays(22)));

        CNH cnhCurta = new CNH("12345678901", "B", HOJE.minusYears(10), HOJE.plusDays(3));
        Cliente cnhVencendo = new Cliente("Cliente CNH", "12345678909", "86999990000", "teste@email.com", HOJE.minusYears(30), cnhCurta);
        conferirErro("CNH vence antes da devolução", "ClienteNaoHabilitadoException",
                erroAoReservar(g, cnhVencendo, onix(), HOJE, HOJE.plusDays(5)));

        System.out.println();
        Passeio naOficina = new Passeio("RVE7J42", "Chevrolet", "Onix LTZ", 2023, 41000, 110);
        naOficina.setSituacao("Manutenção");
        conferirErro("Carro em manutenção", "CarroIndisponivelException",
                erroAoReservar(g, cliente, naOficina, HOJE.plusDays(1), HOJE.plusDays(3)));

        Passeio precisaRevisao = new Passeio("SLT7H47", "Toyota", "Corolla", 2024, 29000, 120);
        precisaRevisao.setKm(39000); // 10000 km desde a última revisão
        conferirErro("Carro que rodou 10.000 km desde a revisão", "CarroIndisponivelException",
                erroAoReservar(g, cliente, precisaRevisao, HOJE.plusDays(1), HOJE.plusDays(3)));

        System.out.println();
        Passeio fox = new Passeio("PIV2E97", "Volkswagen", "Fox", 2022, 66000, 100);
        GerenciadorLocacoes g2 = new GerenciadorLocacoes();
        Locacao a = g2.reservar(cliente, fox, HOJE.plusDays(1), HOJE.plusDays(3), new SemSeguro(), new PagamentoDinheiro());
        Locacao b = g2.reservar(cliente, fox, HOJE.plusDays(3), HOJE.plusDays(5), new SemSeguro(), new PagamentoDinheiro());

        conferirErro("Devolver uma reserva que não foi retirada", "OperacaoInvalidaException",
                erroAoDevolver(g2, a.getId(), HOJE.plusDays(3), 66100, 0));
        g2.retirar(a.getId());
        conferirErro("Retirar duas vezes", "OperacaoInvalidaException", erroAoRetirar(g2, a.getId()));
        conferirErro("Cancelar uma locação ativa", "OperacaoInvalidaException", erroAoCancelar(g2, a.getId()));
        conferirErro("Retirar a próxima reserva com o carro ainda na rua", "CarroIndisponivelException",
                erroAoRetirar(g2, b.getId()));
        conferirErro("Devolver com km menor que o da retirada", "OperacaoInvalidaException",
                erroAoDevolver(g2, a.getId(), HOJE.plusDays(3), 65000, 0));
        conferirErro("Devolver antes da data de retirada", "DatasInvalidasException",
                erroAoDevolver(g2, a.getId(), HOJE.minusDays(1), 66100, 0));
        conferirErro("Combustível faltando negativo", "OperacaoInvalidaException",
                erroAoDevolver(g2, a.getId(), HOJE.plusDays(3), 66100, -5));

        g2.devolver(a.getId(), HOJE.plusDays(3), 66100, 0);
        conferirErro("Devolver duas vezes", "OperacaoInvalidaException",
                erroAoDevolver(g2, a.getId(), HOJE.plusDays(3), 66100, 0));
        conferirErro("Retirar a próxima reserva depois que o carro voltou", "nenhum", erroAoRetirar(g2, b.getId()));

        Locacao c = g2.reservar(cliente, onix(), HOJE.plusDays(1), HOJE.plusDays(3), new SemSeguro(), new PagamentoDinheiro());
        g2.cancelar(c.getId());
        conferirErro("Retirar uma locação cancelada", "OperacaoInvalidaException", erroAoRetirar(g2, c.getId()));
        conferirErro("Cancelar duas vezes", "OperacaoInvalidaException", erroAoCancelar(g2, c.getId()));
        conferirErro("Locação que não existe", "LocacaoNaoEncontradaException", erroAoRetirar(g2, 999));

        System.out.println();
        conferirValor("Locações do cliente", 3, g2.buscarPorCliente(cliente).size());
        conferirValor("Locações ativas", 1, g2.buscarPorSituacao(SituacaoLocacao.ATIVA).size());
        conferirValor("Locações canceladas", 1, g2.buscarPorSituacao(SituacaoLocacao.CANCELADA).size());
        g2.listar();
    }

    // ================= simulador =================
    private static void testarSimulador() throws LocadoraException {
        System.out.println();
        System.out.println("=== Teste do simulador de orçamento ===");
        Cliente cliente = maria();
        Passeio carro = argo();

        ArrayList<Adicional> adicionais = new ArrayList<>();
        adicionais.add(Adicional.GPS);
        adicionais.add(Adicional.CADEIRINHA);

        Orcamento o = SimuladorOrcamento.simular(cliente, carro, HOJE.plusDays(2), HOJE.plusDays(10),
                new SeguroBasico(), new PagamentoPix(), adicionais);
        System.out.println(o);

        // 800 - 5% = 760 | adicionais 27 x 8 = 216 | seguro 80 | subtotal 1056 | Pix = 1003,20
        conferirValor("8 dias, seguro básico, GPS, cadeirinha e Pix", 1003.20, o.getTotal());
        conferir("Simular não muda a situação do carro", true, carro.getSituacao().equals("Livre"));

        System.out.println();
        SimuladorOrcamento.compararPagamentos(cliente, carro, HOJE.plusDays(2), HOJE.plusDays(10), new SeguroBasico(), adicionais);
    }
}
