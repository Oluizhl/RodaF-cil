package Alugueis;

import Carros.Eletrico;
import Carros.Passeio;
import Carros.SUV;
import Carros.Utilitario;
import Carros.Veiculo;
import Clientes.CNH;
import Clientes.Cliente;
import Clientes.Funcionario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class TesteSemana4 {
    private static int acertos = 0;
    private static int total = 0;
    // todo aluguel começa hoje às 10h; cada dia conta 24 horas a partir daí
    private static final LocalDateTime HOJE = LocalDate.now().atTime(10, 0);
    // quinta-feira, para testar o acréscimo do SUV (sexta, sábado e domingo)
    private static final LocalDateTime QUINTA = LocalDateTime.of(2026, 11, 19, 10, 0);

    private static final Funcionario RITA =
            new Funcionario("Rita Atendente", "12345678909", "86999990000", "rita@roda.com", "F001", "Atendente");

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
        CNH cnh = new CNH("12345678901", "B", LocalDate.now().minusYears(anosDeCnh), LocalDate.now().plusYears(3));
        return new Cliente("Cliente Teste", cpf, "86999990000", "teste@email.com", LocalDate.now().minusYears(idade), cnh);
    }

    private static Cliente maria(){
        return novoCliente("12345678909", 30, 5);
    }

    // cliente com os pontos de fidelidade já acumulados
    private static Cliente comNivel(String cpf, double valorJaPago){
        Cliente c = novoCliente(cpf, 35, 10);
        c.registrarAluguel(valorJaPago);
        return c;
    }

    private static Passeio argo(){
        return new Passeio("ABC1D23", "Fiat", "Argo", 2023, 20000, 120);
    }

    private static Passeio onix(){
        return new Passeio("QOQ9G12", "Chevrolet", "Onix", 2023, 41000, 120);
    }

    private static SUV nivus(){
        return new SUV("PIR9D53", "Volkswagen", "Nivus", 2022, 30000, 200);
    }

    private static Utilitario rampage(){
        return new Utilitario("UKF2G68", "RAM", "Rampage", 2026, 21000, 180, 1000);
    }

    private static Eletrico byd(){
        return new Eletrico("SLS9H69", "BYD", "Dolphin", 2024, 43000, 250, 400);
    }

    // valor total de um aluguel que começa na data informada
    private static double valor(Cliente c, Veiculo v, LocalDateTime ini, int dias, Seguro s, FormaPagamento p,
                                ArrayList<Adicional> adicionais) throws LocadoraException {
        return SimuladorOrcamento.simular(c, v, ini, ini.plusDays(dias), s, p, adicionais).getTotal();
    }

    private static double valor(Cliente c, Veiculo v, int dias, Seguro s, FormaPagamento p) throws LocadoraException {
        return valor(c, v, HOJE, dias, s, p, new ArrayList<Adicional>());
    }

    // ---------- os métodos abaixo tentam a operação e devolvem o nome do erro ----------

    private static String erroAoSimular(Cliente c, Veiculo v, int dias, Seguro s, FormaPagamento p, ArrayList<Adicional> ad){
        try {
            valor(c, v, HOJE, dias, s, p, ad);
            return "nenhum";
        } catch (LocadoraException e){
            return e.getClass().getSimpleName();
        }
    }

    private static String erroAoReservar(GerenciadorLocacoes g, Cliente c, Veiculo v, LocalDateTime ini, LocalDateTime fim){
        try {
            g.reservar(c, v, ini, fim, new SemSeguro(), new PagamentoDinheiro());
            return "nenhum";
        } catch (LocadoraException e){
            return e.getClass().getSimpleName();
        }
    }

    private static String erroAoReservarComCondutor(GerenciadorLocacoes g, Cliente c, Veiculo v, Cliente condutor,
                                                    ArrayList<Adicional> adicionais){
        try {
            g.reservar(c, v, HOJE, HOJE.plusDays(3), new SemSeguro(), new PagamentoDinheiro(), adicionais, condutor);
            return "nenhum";
        } catch (LocadoraException e){
            return e.getClass().getSimpleName();
        }
    }

    private static String erroAoRetirar(GerenciadorLocacoes g, int id){
        try {
            g.retirar(id, RITA);
            return "nenhum";
        } catch (LocadoraException e){
            return e.getClass().getSimpleName();
        }
    }

    private static String erroAoDevolver(GerenciadorLocacoes g, int id, Funcionario f, LocalDateTime data, int km, double litros){
        try {
            g.devolver(id, f, data, km, litros);
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

    // aluguel de 5 dias já retirado, pronto para devolver (carro de R$ 120/dia: 500 km de limite)
    private static Locacao locacaoAtiva(GerenciadorLocacoes g, Cliente c, Veiculo v, Seguro s, FormaPagamento p) throws LocadoraException {
        Locacao l = g.reservar(c, v, HOJE, HOJE.plusDays(5), s, p);
        g.retirar(l.getId(), RITA);
        return l;
    }

    private static Avaria avaria(TipoAvaria tipo, double conserto) throws LocadoraException {
        return new Avaria(tipo, "dano de teste", conserto);
    }

    private static ArrayList<Avaria> lista(Avaria... avarias){
        ArrayList<Avaria> l = new ArrayList<>();
        for (Avaria a : avarias){
            l.add(a);
        }
        return l;
    }

    public static void main(String[] args) throws LocadoraException {
        testarDatasQueSeCruzam();
        testarValorDoAluguel();
        testarFormasDePagamento();
        testarDevolucao();
        testarBateriaDoEletrico();
        testarAvarias();
        testarCondutorExtra();
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
        conferirErro("Retira uma hora antes de a outra devolver", "ReservaConflitanteException",
                erroAoReservar(g, cliente, carro, HOJE.plusDays(15).minusHours(1), HOJE.plusDays(20)));

        conferirErro("Devolve no momento em que a outra retira (+5 a +10)", "nenhum",
                erroAoReservar(g, cliente, carro, HOJE.plusDays(5), HOJE.plusDays(10)));
        conferirErro("Retira no momento em que a outra devolve (+15 a +20)", "nenhum",
                erroAoReservar(g, cliente, carro, HOJE.plusDays(15), HOJE.plusDays(20)));
        conferirErro("Outro carro nas mesmas datas", "nenhum",
                erroAoReservar(g, cliente, onix(), HOJE.plusDays(10), HOJE.plusDays(15)));

        conferir("estaDisponivel em +11 a +13", false, g.estaDisponivel(carro, HOJE.plusDays(11), HOJE.plusDays(13)));
        conferir("estaDisponivel em +30 a +35", true, g.estaDisponivel(carro, HOJE.plusDays(30), HOJE.plusDays(35)));

        // reserva cancelada não segura mais o carro
        g.cancelar(base.getId());
        conferir("Reserva cancelada fica CANCELADA", true, base.getSituacao() == SituacaoLocacao.CANCELADA);
        conferirErro("Mesmas datas depois de cancelar", "nenhum",
                erroAoReservar(g, cliente, carro, HOJE.plusDays(10), HOJE.plusDays(15)));
    }

    // ================= valor do aluguel =================
    private static void testarValorDoAluguel() throws LocadoraException {
        System.out.println();
        System.out.println("=== Teste do valor (Passeio de R$ 120 por dia, pagando em dinheiro) ===");
        Cliente cliente = maria();
        Passeio carro = argo();
        Seguro sem = new SemSeguro();
        FormaPagamento dinheiro = new PagamentoDinheiro();

        conferirValor("1 dia", 120, valor(cliente, carro, 1, sem, dinheiro));
        conferirValor("6 dias (sem desconto)", 720, valor(cliente, carro, 6, sem, dinheiro));
        conferirValor("7 dias (5% de desconto)", 798, valor(cliente, carro, 7, sem, dinheiro));
        conferirValor("14 dias (5% de desconto)", 1596, valor(cliente, carro, 14, sem, dinheiro));
        conferirValor("15 dias (10% de desconto)", 1620, valor(cliente, carro, 15, sem, dinheiro));
        conferirValor("29 dias (10% de desconto)", 3132, valor(cliente, carro, 29, sem, dinheiro));
        conferirValor("30 dias (15% de desconto)", 3060, valor(cliente, carro, 30, sem, dinheiro));

        System.out.println();
        System.out.println("-- Regra de cada tipo de carro --");
        SUV suv = nivus();
        Eletrico eletrico = byd();
        Utilitario util = rampage();
        conferirValor("5 dias de SUV, quinta a segunda (200 + 3 x 230 + 200)", 1090, valor(cliente, suv, QUINTA, 5, sem, dinheiro, new ArrayList<Adicional>()));
        conferirValor("3 dias de SUV, segunda a quarta (sem acréscimo)", 600, valor(cliente, suv, QUINTA.plusDays(4), 3, sem, dinheiro, new ArrayList<Adicional>()));
        conferirValor("1 dia de SUV na sexta (200 + 15%)", 230, valor(cliente, suv, QUINTA.plusDays(1), 1, sem, dinheiro, new ArrayList<Adicional>()));
        conferirValor("3 dias de Utilitário (540 + R$ 60 de higienização)", 600, valor(cliente, util, 3, sem, dinheiro));
        conferirValor("7 dias de Utilitário (1.260 - 5% + R$ 60 sem desconto)", 1257, valor(cliente, util, 7, sem, dinheiro));
        conferirValor("2 dias de Elétrico (sem desconto)", 500, valor(cliente, eletrico, 2, sem, dinheiro));
        conferirValor("3 dias de Elétrico (10% de desconto)", 675, valor(cliente, eletrico, 3, sem, dinheiro));
        conferirValor("7 dias de Elétrico (10% e depois 5% do período)", 1496.25, valor(cliente, eletrico, 7, sem, dinheiro));

        System.out.println();
        conferirValor("Seguro básico (5 dias: 600 + 5 x 35)", 775, valor(cliente, carro, 5, new SeguroBasico(), dinheiro));
        conferirValor("Seguro completo (5 dias: 600 + 5 x 70)", 950, valor(cliente, carro, 5, new SeguroCompleto(), dinheiro));

        System.out.println();
        ArrayList<Adicional> gps = new ArrayList<>();
        gps.add(Adicional.GPS);
        ArrayList<Adicional> todos = new ArrayList<>();
        todos.add(Adicional.GPS);
        todos.add(Adicional.CADEIRINHA);
        todos.add(Adicional.CONDUTOR_EXTRA);
        ArrayList<Adicional> duasCadeirinhas = new ArrayList<>();
        duasCadeirinhas.add(Adicional.CADEIRINHA);
        duasCadeirinhas.add(Adicional.CADEIRINHA);
        ArrayList<Adicional> tresCadeirinhas = new ArrayList<>(duasCadeirinhas);
        tresCadeirinhas.add(Adicional.CADEIRINHA);
        conferirValor("3 dias com GPS (360 + 45)", 405, valor(cliente, carro, HOJE, 3, sem, dinheiro, gps));
        conferirValor("3 dias com GPS, cadeirinha e condutor extra (360 + 45 + 60 + 90)", 555, valor(cliente, carro, HOJE, 3, sem, dinheiro, todos));
        conferirValor("3 dias com 2 cadeirinhas (360 + 120)", 480, valor(cliente, carro, HOJE, 3, sem, dinheiro, duasCadeirinhas));
        conferirErro("3 cadeirinhas (o limite é 2)", "OperacaoInvalidaException",
                erroAoSimular(cliente, carro, 3, sem, dinheiro, tresCadeirinhas));

        System.out.println();
        Cliente diamante = comNivel("11144477735", 30000); // 3.000 pontos
        conferirValor("5 dias para cliente Diamante (600 - 15%)", 510, valor(diamante, carro, 5, sem, dinheiro));
        ArrayList<Adicional> soGps = new ArrayList<>(gps);
        conferirValor("Fidelidade vale também sobre seguro e adicionais ((600 + 175 + 75) x 0,85)", 722.50,
                valor(diamante, carro, HOJE, 5, new SeguroBasico(), dinheiro, soGps));
        Cliente ouro = comNivel("52998224725", 15000); // 1.500 pontos
        conferirValor("5 dias para cliente Ouro (600 - 10%)", 540, valor(ouro, carro, 5, sem, dinheiro));

        // o exemplo completo do documento: Passeio, 7 dias, GPS, seguro básico, cliente Prata, Pix
        System.out.println();
        Cliente prata = comNivel("12345678909", 5000); // 500 pontos
        conferir("Cliente de 500 pontos é Prata", true, prata.getNivel().getNome().equals("Prata"));
        conferirValor("Exemplo do documento (7 dias, GPS, básico, Prata, Pix)", 1036.07,
                valor(prata, carro, HOJE, 7, new SeguroBasico(), new PagamentoPix(), gps));
    }

    // ================= formas de pagamento =================
    private static void testarFormasDePagamento() throws LocadoraException {
        System.out.println();
        System.out.println("=== Teste de cada forma de pagamento (5 dias, R$ 600) ===");
        Cliente cliente = maria();
        Passeio carro = argo();
        Seguro sem = new SemSeguro();

        conferirValor("Dinheiro", 600, valor(cliente, carro, 5, sem, new PagamentoDinheiro()));
        conferirValor("Pix (5% de desconto)", 570, valor(cliente, carro, 5, sem, new PagamentoPix()));
        conferirValor("Cartão à vista", 600, valor(cliente, carro, 5, sem, new PagamentoCartao(1)));
        conferirValor("Cartão em 3x (sem juros)", 600, valor(cliente, carro, 5, sem, new PagamentoCartao(3)));
        conferirValor("Cartão em 4x (acréscimo de 5%)", 630, valor(cliente, carro, 5, sem, new PagamentoCartao(4)));
        conferirValor("Cartão em 6x (acréscimo de 5%)", 630, valor(cliente, carro, 5, sem, new PagamentoCartao(6)));
        conferirValor("Parcela de R$ 600 em 3x", 200, new PagamentoCartao(3).calcularParcela(600));
        conferirValor("Parcela de R$ 600 em 6x (630 / 6)", 105, new PagamentoCartao(6).calcularParcela(600));

        conferirErro("Cartão em 0 parcelas", "PagamentoInvalidoException", erroAoCriarCartao(0));
        conferirErro("Cartão em 7 parcelas (o limite é 6)", "PagamentoInvalidoException", erroAoCriarCartao(7));

        // cada parcela precisa ter pelo menos R$ 100,00
        ArrayList<Adicional> nenhum = new ArrayList<>();
        conferirErro("1 dia (R$ 120) em 2x: parcela de R$ 60", "PagamentoInvalidoException",
                erroAoSimular(cliente, carro, 1, sem, new PagamentoCartao(2), nenhum));
        conferirErro("4 dias (R$ 480) em 6x: parcela de R$ 84", "PagamentoInvalidoException",
                erroAoSimular(cliente, carro, 4, sem, new PagamentoCartao(6), nenhum));
        conferirErro("4 dias (R$ 480) em 4x: parcela de R$ 126", "nenhum",
                erroAoSimular(cliente, carro, 4, sem, new PagamentoCartao(4), nenhum));
        conferirErro("1 dia (R$ 120) à vista no cartão", "nenhum",
                erroAoSimular(cliente, carro, 1, sem, new PagamentoCartao(1), nenhum));

        // mesma chamada, resultado diferente em cada forma (polimorfismo)
        System.out.println();
        FormaPagamento[] formas = { new PagamentoPix(), new PagamentoDinheiro(), new PagamentoCartao(1), new PagamentoCartao(6) };
        for (FormaPagamento f : formas){
            System.out.println(f.descrever(1000));
        }
    }

    // ================= devolução: atraso, km e combustível =================
    private static void testarDevolucao() throws LocadoraException {
        System.out.println();
        System.out.println("=== Teste da devolução (5 dias, R$ 120 por dia, km inicial 20000, limite de 500 km) ===");

        // no prazo
        Cliente c1 = maria();
        Passeio carro1 = argo();
        GerenciadorLocacoes g1 = new GerenciadorLocacoes();
        Locacao l1 = locacaoAtiva(g1, c1, carro1, new SemSeguro(), new PagamentoDinheiro());
        conferir("Depois de retirar, o carro fica Alugado", true, carro1.getSituacao().equals("Alugado"));
        conferir("Depois de retirar, a locação fica ATIVA", true, l1.getSituacao() == SituacaoLocacao.ATIVA);
        conferir("A retirada registra o funcionário da vistoria", true, l1.getVistoriaRetirada().getFuncionario() == RITA);

        Funcionario paulo = new Funcionario("Paulo Balcão", "52998224725", "86999990000", "paulo@roda.com", "F002", "Atendente");
        Fechamento f1 = g1.devolver(l1.getId(), paulo, HOJE.plusDays(5), 20500, 0);
        conferirValor("No prazo: dias de atraso", 0, f1.getDiasAtraso());
        conferirValor("No prazo: extras", 0, f1.getTotalExtras());
        conferirValor("No prazo: total geral", 600, f1.getTotalGeral());
        conferir("Depois de devolver, o carro fica Livre", true, carro1.getSituacao().equals("Livre"));
        conferirValor("O km do carro é atualizado", 20500, carro1.getKm());
        conferir("A locação fica FINALIZADA", true, l1.getSituacao() == SituacaoLocacao.FINALIZADA);
        conferir("A devolução registra o funcionário da vistoria", true, l1.getVistoriaDevolucao().getFuncionario() == paulo);
        conferirValor("Pontos do cliente (1 a cada R$ 10: R$ 600)", 60, c1.getPontos());

        // atraso: tolerância de 1 hora; depois cada dia ou fração cobra 1 diária + 20%
        System.out.println();
        conferirValor("Atraso de 1 hora exata: sem multa", 0, devolverAtrasado(60).getMultaAtraso());
        conferirValor("Atraso de 61 minutos: 1 dia (120 x 1,2)", 144, devolverAtrasado(61).getMultaAtraso());
        conferirValor("Atraso de 24 horas: 1 dia", 1, devolverAtrasado(24 * 60).getDiasAtraso());
        conferirValor("Atraso de 25 horas: 2 dias (fração conta como dia)", 2, devolverAtrasado(25 * 60).getDiasAtraso());
        conferirValor("Atraso de 2 dias: multa (2 x 120 x 1,2)", 288, devolverAtrasado(48 * 60).getMultaAtraso());
        conferirValor("Atraso de 2 dias: total geral (600 + 288)", 888, devolverAtrasado(48 * 60).getTotalGeral());

        // devolveu antes
        Cliente c3 = maria();
        GerenciadorLocacoes g3 = new GerenciadorLocacoes();
        Locacao l3 = locacaoAtiva(g3, c3, argo(), new SemSeguro(), new PagamentoDinheiro());
        Fechamento f3 = g3.devolver(l3.getId(), RITA, HOJE.plusDays(4), 20300, 0);
        conferirValor("Devolveu 1 dia antes: multa", 0, f3.getMultaAtraso());

        // km a mais (limite de 100 km por dia = 500 km em 5 dias, R$ 1,00 por km)
        System.out.println();
        conferirValor("Rodou 600 km: km a mais", 100, devolverComKm(argo(), 20600).getKmExtra());
        conferirValor("Rodou 600 km: cobrança (100 x R$ 1,00)", 100, devolverComKm(argo(), 20600).getValorKmExtra());
        conferirValor("Rodou exatamente 500 km: cobrança", 0, devolverComKm(argo(), 20500).getValorKmExtra());
        conferirValor("Utilitário rodou 800 km (limite de 750): cobrança", 50, devolverComKm(rampage(), 21800).getValorKmExtra());
        conferirValor("Utilitário rodou 750 km (limite de 150 por dia): cobrança", 0, devolverComKm(rampage(), 21750).getValorKmExtra());

        // combustível: R$ 7,50 por litro + R$ 30,00 de taxa se faltar qualquer quantidade
        System.out.println();
        conferirValor("Tanque cheio: sem cobrança", 0, devolverComCombustivel(0).getValorCombustivel());
        conferirValor("Faltaram 10 litros (10 x 7,50 + 30)", 105, devolverComCombustivel(10).getValorCombustivel());
        conferirValor("Faltou meio litro (3,75 + 30 de taxa)", 33.75, devolverComCombustivel(0.5).getValorCombustivel());

        // tudo junto, pago no Pix: as cobranças da devolução não recebem desconto
        System.out.println();
        Cliente c7 = maria();
        GerenciadorLocacoes g7 = new GerenciadorLocacoes();
        Locacao l7 = locacaoAtiva(g7, c7, argo(), new SemSeguro(), new PagamentoPix());
        Fechamento f7 = g7.devolver(l7.getId(), RITA, HOJE.plusDays(5).plusHours(2), 20600, 10);
        conferirValor("Atraso + km + combustível (144 + 100 + 105)", 349, f7.getTotalExtras());
        conferirValor("Locação paga no Pix (600 - 5%)", 570, f7.getValorLocacao());
        conferirValor("Total geral (570 + 349)", 919, f7.getTotalGeral());
        conferirValor("Pontos só do aluguel (R$ 570 = 57; a multa não conta)", 57, c7.getPontos());
        System.out.println(f7);

        // cobranças que o cliente não pagou: ele fica bloqueado
        System.out.println();
        Cliente c8 = maria();
        Passeio carro8 = argo();
        GerenciadorLocacoes g8 = new GerenciadorLocacoes();
        Locacao l8 = locacaoAtiva(g8, c8, carro8, new SemSeguro(), new PagamentoDinheiro());
        g8.devolver(l8.getId(), RITA, HOJE.plusDays(7), 20400, 0, new ArrayList<Avaria>(), false);
        conferir("Quem não pagou a multa fica bloqueado", true, c8.isBloqueado());
        conferirValor("Débito do cliente (2 dias de atraso)", 288, c8.getDebito());
        conferirErro("Cliente bloqueado tenta reservar", "ClienteBloqueadoException",
                erroAoReservar(g8, c8, carro8, HOJE.plusDays(10), HOJE.plusDays(12)));
        c8.quitarDebito(288);
        conferirErro("Reservar depois de quitar o débito", "nenhum",
                erroAoReservar(g8, c8, carro8, HOJE.plusDays(10), HOJE.plusDays(12)));
    }

    private static Fechamento devolverAtrasado(int minutosDeAtraso) throws LocadoraException {
        GerenciadorLocacoes g = new GerenciadorLocacoes();
        Locacao l = locacaoAtiva(g, maria(), argo(), new SemSeguro(), new PagamentoDinheiro());
        return g.devolver(l.getId(), RITA, HOJE.plusDays(5).plusMinutes(minutosDeAtraso), 20400, 0);
    }

    private static Fechamento devolverComKm(Veiculo carro, int kmFinal) throws LocadoraException {
        GerenciadorLocacoes g = new GerenciadorLocacoes();
        Locacao l = locacaoAtiva(g, maria(), carro, new SemSeguro(), new PagamentoDinheiro());
        return g.devolver(l.getId(), RITA, HOJE.plusDays(5), kmFinal, 0);
    }

    private static Fechamento devolverComCombustivel(double litros) throws LocadoraException {
        GerenciadorLocacoes g = new GerenciadorLocacoes();
        Locacao l = locacaoAtiva(g, maria(), argo(), new SemSeguro(), new PagamentoDinheiro());
        return g.devolver(l.getId(), RITA, HOJE.plusDays(5), 20400, litros);
    }

    // ================= bateria do elétrico =================
    private static void testarBateriaDoEletrico() throws LocadoraException {
        System.out.println();
        System.out.println("=== Teste da bateria do elétrico (sai com pelo menos 80%) ===");
        GerenciadorLocacoes g = new GerenciadorLocacoes();
        Cliente cliente = maria();

        Eletrico e1 = byd();
        Locacao l1 = g.reservar(cliente, e1, HOJE, HOJE.plusDays(5), new SemSeguro(), new PagamentoDinheiro());
        conferirErro("Retirar o elétrico sem informar a carga", "OperacaoInvalidaException", erroAoRetirar(g, l1.getId()));
        try {
            g.retirar(l1.getId(), RITA, 79);
            conferirErro("Retirar o elétrico com 79% de carga", "OperacaoInvalidaException", "nenhum");
        } catch (LocadoraException e){
            conferirErro("Retirar o elétrico com 79% de carga", "OperacaoInvalidaException", e.getClass().getSimpleName());
        }
        g.retirar(l1.getId(), RITA, 90);
        conferirValor("Carga de saída registrada na vistoria", 90, l1.getVistoriaRetirada().getCargaBateria());

        // saiu com 90%
        conferirValor("Voltou com 90%: sem cobrança", 0, devolverEletrico(90).getValorBateria());
        conferirValor("Voltou com 85% (5 pontos abaixo: 1 bloco)", 25, devolverEletrico(85).getValorBateria());
        conferirValor("Voltou com 80% (10 pontos abaixo: 1 bloco)", 25, devolverEletrico(80).getValorBateria());
        conferirValor("Voltou com 79% (11 pontos abaixo: 2 blocos)", 50, devolverEletrico(79).getValorBateria());
        conferirValor("Voltou com 55% (35 pontos abaixo: 4 blocos)", 100, devolverEletrico(55).getValorBateria());
        conferirValor("Voltou com 10% (80 pontos abaixo: 8 blocos, sem taxa)", 200, devolverEletrico(10).getValorBateria());
        conferirValor("Voltou com 9% (9 blocos + R$ 80 de recarga urgente)", 305, devolverEletrico(9).getValorBateria());
        conferirValor("Elétrico não paga combustível", 0, devolverEletrico(90).getValorCombustivel());
        // elétrico: 5 dias = 1.250 - 10% = 1.125 (km: limite de 500)
        conferirValor("Total do elétrico que voltou com 55% (1.125 + 100)", 1225, devolverEletrico(55).getTotalGeral());
    }

    private static Fechamento devolverEletrico(int cargaFinal) throws LocadoraException {
        GerenciadorLocacoes g = new GerenciadorLocacoes();
        Eletrico e = byd();
        Locacao l = g.reservar(maria(), e, HOJE, HOJE.plusDays(5), new SemSeguro(), new PagamentoDinheiro());
        g.retirar(l.getId(), RITA, 90);
        return g.devolver(l.getId(), RITA, HOJE.plusDays(5), 43300, 0, cargaFinal, new ArrayList<Avaria>(), true);
    }

    // ================= avarias x seguro =================
    private static void testarAvarias() throws LocadoraException {
        System.out.println();
        System.out.println("=== Teste de avarias com cada seguro ===");
        Avaria batida = avaria(TipoAvaria.LATARIA_MECANICA, 3500);
        Avaria arranhao = avaria(TipoAvaria.LATARIA_MECANICA, 800);
        Avaria vidro = avaria(TipoAvaria.VIDRO, 900);
        Avaria pneu = avaria(TipoAvaria.PNEU, 600);
        Avaria banco = avaria(TipoAvaria.INTERIOR, 450);

        Seguro sem = new SemSeguro();
        Seguro basico = new SeguroBasico();
        Seguro completo = new SeguroCompleto();

        conferirValor("Sem seguro: batida de 3.500 (100%)", 3500, sem.calcularAvaria(batida));
        conferirValor("Básico: batida de 3.500 (franquia de 2.000)", 2000, basico.calcularAvaria(batida));
        conferirValor("Básico: arranhão de 800 (abaixo da franquia)", 800, basico.calcularAvaria(arranhao));
        conferirValor("Completo: batida de 3.500", 0, completo.calcularAvaria(batida));

        conferirValor("Sem seguro: vidro", 900, sem.calcularAvaria(vidro));
        conferirValor("Básico: vidro (não coberto, 100%)", 900, basico.calcularAvaria(vidro));
        conferirValor("Completo: vidro", 0, completo.calcularAvaria(vidro));
        conferirValor("Básico: pneu (não coberto, 100%)", 600, basico.calcularAvaria(pneu));
        conferirValor("Básico: interior (não coberto, 100%)", 450, basico.calcularAvaria(banco));
        conferirValor("Completo: pneu e interior", 0, completo.calcularAvaria(pneu) + completo.calcularAvaria(banco));

        System.out.println();
        conferirValor("Devolução sem seguro: batida + vidro", 4400, devolverComAvarias(sem, batida, vidro).getValorAvarias());
        conferirValor("Devolução com básico: batida + vidro (2.000 + 900)", 2900, devolverComAvarias(basico, batida, vidro).getValorAvarias());
        conferirValor("Devolução com completo: batida + vidro + pneu + interior", 0,
                devolverComAvarias(completo, batida, vidro, pneu, banco).getValorAvarias());
        conferirValor("Avaria entra no total de extras (sem desconto)", 2900, devolverComAvarias(basico, batida, vidro).getTotalExtras());

        // quem não paga a avaria na devolução fica bloqueado; os pontos contam só o aluguel
        Cliente c = maria();
        GerenciadorLocacoes g = new GerenciadorLocacoes();
        Locacao l = locacaoAtiva(g, c, argo(), basico, new PagamentoDinheiro());
        g.devolver(l.getId(), RITA, HOJE.plusDays(5), 20400, 0, lista(batida), false);
        conferir("Avaria que não foi paga bloqueia o cliente", true, c.isBloqueado());
        conferirValor("Débito é o que o seguro não cobre", 2000, c.getDebito());
        conferirValor("Pontos só do aluguel (R$ 775 = 77; a avaria não conta)", 77, c.getPontos());
        c.quitarDebito(2000);
        conferir("Pagou a avaria: desbloqueado", false, c.isBloqueado());

        Cliente c2 = maria();
        GerenciadorLocacoes g2 = new GerenciadorLocacoes();
        Locacao l2 = locacaoAtiva(g2, c2, argo(), basico, new PagamentoDinheiro());
        g2.devolver(l2.getId(), RITA, HOJE.plusDays(5), 20400, 0, lista(batida), true);
        conferir("Avaria paga na devolução não bloqueia", false, c2.isBloqueado());

        conferirErro("Avaria com conserto zero", "OperacaoInvalidaException", erroAoCriarAvaria(0));
    }

    private static String erroAoCriarAvaria(double conserto){
        try {
            new Avaria(TipoAvaria.VIDRO, "teste", conserto);
            return "nenhum";
        } catch (LocadoraException e){
            return e.getClass().getSimpleName();
        }
    }

    private static Fechamento devolverComAvarias(Seguro seguro, Avaria... avarias) throws LocadoraException {
        GerenciadorLocacoes g = new GerenciadorLocacoes();
        Locacao l = locacaoAtiva(g, maria(), argo(), seguro, new PagamentoDinheiro());
        return g.devolver(l.getId(), RITA, HOJE.plusDays(5), 20400, 0, lista(avarias), true);
    }

    // ================= condutor extra =================
    private static void testarCondutorExtra() throws LocadoraException {
        System.out.println();
        System.out.println("=== Teste do condutor extra (mesmas regras de idade e CNH) ===");
        GerenciadorLocacoes g = new GerenciadorLocacoes();
        Cliente titular = maria();
        ArrayList<Adicional> nenhum = new ArrayList<>();

        Cliente condutorOk = novoCliente("11144477735", 25, 3);
        Cliente condutorJovem = novoCliente("52998224725", 19, 1);
        Cliente condutorNovato = novoCliente("12345678909", 25, 1);
        CNH cnhVencida = new CNH("12345678901", "B", LocalDate.now().minusYears(10), LocalDate.now().minusDays(1));
        Cliente condutorVencido = new Cliente("Condutor Vencido", "11144477735", "86999990000", "c@email.com",
                LocalDate.now().minusYears(30), cnhVencida);

        conferirErro("Passeio com condutor extra válido", "nenhum",
                erroAoReservarComCondutor(g, titular, argo(), condutorOk, nenhum));
        conferirErro("Passeio com condutor de 19 anos (passeio libera)", "nenhum",
                erroAoReservarComCondutor(new GerenciadorLocacoes(), titular, argo(), condutorJovem, nenhum));
        conferirErro("Condutor com CNH vencida", "ClienteNaoHabilitadoException",
                erroAoReservarComCondutor(new GerenciadorLocacoes(), titular, argo(), condutorVencido, nenhum));
        conferirErro("SUV com condutor de 19 anos", "ClienteNaoHabilitadoException",
                erroAoReservarComCondutor(new GerenciadorLocacoes(), titular, nivus(), condutorJovem, nenhum));
        conferirErro("SUV com condutor de 25 anos e CNH há 1 ano", "ClienteNaoHabilitadoException",
                erroAoReservarComCondutor(new GerenciadorLocacoes(), titular, nivus(), condutorNovato, nenhum));
        conferirErro("Elétrico com condutor de 19 anos", "ClienteNaoHabilitadoException",
                erroAoReservarComCondutor(new GerenciadorLocacoes(), titular, byd(), condutorJovem, nenhum));
        conferirErro("SUV com condutor de 25 anos e CNH há 3 anos", "nenhum",
                erroAoReservarComCondutor(new GerenciadorLocacoes(), titular, nivus(), condutorOk, nenhum));

        ArrayList<Adicional> pedeCondutor = new ArrayList<>();
        pedeCondutor.add(Adicional.CONDUTOR_EXTRA);
        conferirErro("Pediu o adicional mas não informou o condutor", "OperacaoInvalidaException",
                erroAoReservarComCondutor(new GerenciadorLocacoes(), titular, argo(), null, pedeCondutor));

        // o condutor informado entra no orçamento (3 dias x R$ 30)
        Locacao l = g.reservar(titular, onix(), HOJE, HOJE.plusDays(3), new SemSeguro(), new PagamentoDinheiro(), nenhum, condutorOk);
        conferirValor("Orçamento com condutor extra (360 + 90)", 450, l.getOrcamento().getTotal());
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
        conferirErro("Devolução no mesmo momento da retirada", "DatasInvalidasException",
                erroAoReservar(g, cliente, carro, HOJE.plusDays(5), HOJE.plusDays(5)));
        conferirErro("Data vazia", "DatasInvalidasException",
                erroAoReservar(g, cliente, carro, null, HOJE.plusDays(5)));

        System.out.println();
        Cliente devedor = novoCliente("52998224725", 40, 15);
        devedor.registrarDebito(200, "multa de trânsito");
        conferirErro("Cliente com multa", "ClienteBloqueadoException",
                erroAoReservar(g, devedor, carro, HOJE.plusDays(1), HOJE.plusDays(3)));

        Cliente novato = novoCliente("11144477735", 19, 1);
        SUV suv = nivus();
        conferirErro("Cliente de 19 anos num SUV", "ClienteNaoHabilitadoException",
                erroAoReservar(g, novato, suv, HOJE.plusDays(1), HOJE.plusDays(3)));
        conferirErro("Cliente de 19 anos num Passeio", "nenhum",
                erroAoReservar(g, novato, carro, HOJE.plusDays(20), HOJE.plusDays(22)));

        CNH cnhCurta = new CNH("12345678901", "B", LocalDate.now().minusYears(10), LocalDate.now().plusDays(3));
        Cliente cnhVencendo = new Cliente("Cliente CNH", "12345678909", "86999990000", "teste@email.com",
                LocalDate.now().minusYears(30), cnhCurta);
        conferirErro("CNH vence antes da devolução", "ClienteNaoHabilitadoException",
                erroAoReservar(g, cnhVencendo, onix(), HOJE, HOJE.plusDays(5)));

        System.out.println();
        Passeio naOficina = new Passeio("RVE7J42", "Chevrolet", "Onix LTZ", 2023, 41000, 120);
        naOficina.setSituacao("Manutenção");
        conferirErro("Carro em manutenção", "CarroIndisponivelException",
                erroAoReservar(g, cliente, naOficina, HOJE.plusDays(1), HOJE.plusDays(3)));

        Passeio precisaRevisao = new Passeio("SLT7H47", "Toyota", "Corolla", 2024, 29000, 120);
        precisaRevisao.setKm(39000); // 10000 km desde a última revisão
        conferirErro("Carro que rodou 10.000 km desde a revisão", "CarroIndisponivelException",
                erroAoReservar(g, cliente, precisaRevisao, HOJE.plusDays(1), HOJE.plusDays(3)));

        System.out.println();
        Passeio fox = new Passeio("PIV2E97", "Volkswagen", "Fox", 2022, 66000, 120);
        GerenciadorLocacoes g2 = new GerenciadorLocacoes();
        Locacao a = g2.reservar(cliente, fox, HOJE.plusDays(1), HOJE.plusDays(3), new SemSeguro(), new PagamentoDinheiro());
        Locacao b = g2.reservar(cliente, fox, HOJE.plusDays(3), HOJE.plusDays(5), new SemSeguro(), new PagamentoDinheiro());

        conferirErro("Devolver uma reserva que não foi retirada", "OperacaoInvalidaException",
                erroAoDevolver(g2, a.getId(), RITA, HOJE.plusDays(3), 66100, 0));
        g2.retirar(a.getId(), RITA);
        conferirErro("Retirar duas vezes", "OperacaoInvalidaException", erroAoRetirar(g2, a.getId()));
        conferirErro("Cancelar uma locação ativa", "OperacaoInvalidaException", erroAoCancelar(g2, a.getId()));
        conferirErro("Retirar a próxima reserva com o carro ainda na rua", "CarroIndisponivelException",
                erroAoRetirar(g2, b.getId()));
        conferirErro("Devolver com km menor que o da retirada", "OperacaoInvalidaException",
                erroAoDevolver(g2, a.getId(), RITA, HOJE.plusDays(3), 65000, 0));
        conferirErro("Devolver antes da data de retirada", "DatasInvalidasException",
                erroAoDevolver(g2, a.getId(), RITA, HOJE.minusDays(1), 66100, 0));
        conferirErro("Combustível faltando negativo", "OperacaoInvalidaException",
                erroAoDevolver(g2, a.getId(), RITA, HOJE.plusDays(3), 66100, -5));
        conferirErro("Devolver sem informar o funcionário", "OperacaoInvalidaException",
                erroAoDevolver(g2, a.getId(), null, HOJE.plusDays(3), 66100, 0));

        g2.devolver(a.getId(), RITA, HOJE.plusDays(3), 66100, 0);
        conferirErro("Devolver duas vezes", "OperacaoInvalidaException",
                erroAoDevolver(g2, a.getId(), RITA, HOJE.plusDays(3), 66100, 0));

        // retirar sem funcionário
        try {
            g2.retirar(b.getId(), null);
            conferirErro("Retirar sem informar o funcionário", "OperacaoInvalidaException", "nenhum");
        } catch (LocadoraException e){
            conferirErro("Retirar sem informar o funcionário", "OperacaoInvalidaException", e.getClass().getSimpleName());
        }
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

        // 960 - 5% = 912 | adicionais (15 + 20) x 8 = 280 | seguro 8 x 35 = 280 | subtotal 1472 | Pix = 1398,40
        conferirValor("8 dias, seguro básico, GPS, cadeirinha e Pix", 1398.40, o.getTotal());
        conferir("Simular não muda a situação do carro", true, carro.getSituacao().equals("Livre"));

        // cliente ainda sem cadastro: sem fidelidade; cliente Prata: 5% sobre o subtotal
        Orcamento semCadastro = SimuladorOrcamento.simular(null, carro, HOJE, HOJE.plusDays(5),
                new SemSeguro(), new PagamentoDinheiro());
        conferirValor("Simulação sem cadastro (sem fidelidade)", 600, semCadastro.getTotal());
        Cliente prata = comNivel("11144477735", 5000);
        Orcamento comPrata = SimuladorOrcamento.simular(prata, carro, HOJE, HOJE.plusDays(5),
                new SemSeguro(), new PagamentoDinheiro());
        conferirValor("Simulação usa o nível do cliente cadastrado (Prata)", 570, comPrata.getTotal());

        // simular não cria reserva nem bloqueia o carro
        GerenciadorLocacoes g = new GerenciadorLocacoes();
        SimuladorOrcamento.simular(cliente, carro, HOJE, HOJE.plusDays(5), new SemSeguro(), new PagamentoDinheiro());
        conferirValor("Simular não cria locação", 0, g.getLocacoes().size());
        conferirErro("Depois de simular, o mesmo carro ainda pode ser reservado", "nenhum",
                erroAoReservar(g, cliente, carro, HOJE, HOJE.plusDays(5)));

        System.out.println();
        SimuladorOrcamento.compararPagamentos(cliente, carro, HOJE.plusDays(2), HOJE.plusDays(10), new SeguroBasico(), adicionais);
    }
}
