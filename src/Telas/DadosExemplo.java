package Telas;

import Alugueis.Adicional;
import Alugueis.Avaria;
import Alugueis.GerenciadorLocacoes;
import Alugueis.Locacao;
import Alugueis.LocadoraException;
import Alugueis.PagamentoCartao;
import Alugueis.PagamentoDinheiro;
import Alugueis.PagamentoPix;
import Alugueis.SeguroBasico;
import Alugueis.SeguroCompleto;
import Alugueis.SemSeguro;
import Alugueis.TipoAvaria;
import Carros.Eletrico;
import Carros.Locadora;
import Carros.Manutencao;
import Carros.Passeio;
import Carros.SUV;
import Carros.Utilitario;
import Carros.Veiculo;
import Clientes.CNH;
import Clientes.CadastroClientes;
import Clientes.CadastroFuncionarios;
import Clientes.Cliente;
import Clientes.Funcionario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

// "carregar dados de exemplo": enche o sistema com carros, clientes, funcionários e aluguéis
// para a demonstração na apresentação (o painel gerencial já aparece preenchido)
public class DadosExemplo {

    public static void carregar(Sistema sistema){
        sistema.limpar();
        Locadora locadora = sistema.getLocadora();
        CadastroClientes clientes = sistema.getClientes();
        CadastroFuncionarios funcionarios = sistema.getFuncionarios();
        GerenciadorLocacoes locacoes = sistema.getLocacoes();

        // ---------- carros (Parte 1) ----------
        Passeio hb20 = new Passeio("RTK1G75", "Hyundai", "HB20s Evolution", 2022, 67000, 120);
        Passeio argo = new Passeio("ABC1D23", "Fiat", "Argo Drive", 2023, 30000, 120);
        SUV nivus = new SUV("RSK1J13", "Volkswagen", "Nivus Highline", 2022, 35000, 200);
        SUV compass = new SUV("JEP2A34", "Jeep", "Compass Longitude", 2024, 18000, 200);
        Utilitario rampage = new Utilitario("UKF2G68", "RAM", "Rampage Rebel", 2026, 21000, 180, 1000);
        Utilitario strada = new Utilitario("FIA3B45", "Fiat", "Strada Freedom", 2023, 40000, 180, 650);
        Eletrico peugeot = new Eletrico("SLS9H69", "Peugeot", "2008e GT", 2022, 45000, 250, 220);
        Eletrico dolphin = new Eletrico("BYD4C56", "BYD", "Dolphin", 2024, 12000, 250, 400);

        Veiculo[] carros = {hb20, argo, nivus, compass, rampage, strada, peugeot, dolphin};
        for (Veiculo v : carros){
            locadora.cadastrar(v);
        }
        argo.setSituacao("Manutenção");
        argo.registrarManutencao(new Manutencao("10/11/2026", "Troca de embreagem", 1200.00));

        // ---------- clientes e funcionários (Parte 2) ----------
        Cliente ana = new Cliente("Ana Souza", "529.982.247-25", "86999998888", "ana.souza@email.com",
                LocalDate.of(1995, 3, 2), new CNH("12345678901", "B", LocalDate.of(2015, 6, 10), LocalDate.of(2031, 6, 10)));
        Cliente bruno = new Cliente("Bruno Lima", "123.456.789-09", "86988887777", "bruno.lima@email.com",
                LocalDate.of(1988, 8, 15), new CNH("23456789012", "AB", LocalDate.of(2008, 1, 20), LocalDate.of(2030, 1, 20)));
        Cliente carla = new Cliente("Carla Mendes", "987.654.321-00", "86977776666", "carla.mendes@email.com",
                LocalDate.of(2000, 11, 5), new CNH("34567890123", "B", LocalDate.of(2019, 4, 1), LocalDate.of(2029, 4, 1)));
        Cliente diego = new Cliente("Diego Rocha", "321.654.987-91", "86966665555", "diego.rocha@email.com",
                LocalDate.of(2006, 2, 14), new CNH("45678901234", "B", LocalDate.of(2025, 3, 3), LocalDate.of(2028, 3, 3)));
        Cliente elisa = new Cliente("Elisa Martins", "456.123.789-55", "86955554444", "elisa.martins@email.com",
                LocalDate.of(1979, 7, 30), new CNH("56789012345", "AB", LocalDate.of(1999, 9, 9), LocalDate.of(2032, 9, 9)));

        Cliente[] listaClientes = {ana, bruno, carla, diego, elisa};
        for (Cliente c : listaClientes){
            clientes.cadastrar(c);
        }

        Funcionario carlos = new Funcionario("Carlos Pereira", "111.444.777-35", "86944443333", "carlos@rodafacil.com", "F001", "Atendente");
        Funcionario mariana = new Funcionario("Mariana Alves", "741.852.963-55", "86933332222", "mariana@rodafacil.com", "F002", "Gerente");
        funcionarios.cadastrar(carlos);
        funcionarios.cadastrar(mariana);

        // ---------- aluguéis (Parte 3) ----------
        // a Parte 3 não deixa reservar no passado, então as datas começam daqui a 1 minuto
        LocalDateTime agora = LocalDateTime.now().plusMinutes(1).truncatedTo(ChronoUnit.MINUTES);

        try {
            // 1) Ana - SUV com seguro completo e GPS, devolvido sem cobranças extras
            ArrayList<Adicional> gps = new ArrayList<Adicional>();
            gps.add(Adicional.GPS);
            Locacao l1 = locacoes.reservar(ana, nivus, agora, agora.plusDays(3), new SeguroCompleto(), new PagamentoPix(), gps);
            locacoes.retirar(l1.getId(), carlos);
            locacoes.devolver(l1.getId(), carlos, agora.plusHours(2), nivus.getKm() + 250, 0);

            // 2) Bruno - utilitário devolvido com atraso, km a mais, tanque sem encher e pneu furado (pagou na hora)
            Locacao l2 = locacoes.reservar(bruno, rampage, agora, agora.plusDays(1), new SeguroBasico(), new PagamentoCartao(1));
            locacoes.retirar(l2.getId(), mariana);
            ArrayList<Avaria> pneu = new ArrayList<Avaria>();
            pneu.add(new Avaria(TipoAvaria.PNEU, "Pneu dianteiro furado", 450.00));
            locacoes.devolver(l2.getId(), mariana, agora.plusDays(1).plusHours(5), rampage.getKm() + 250, 10, pneu, true);

            // 3) Carla - elétrico com batida (franquia do seguro básico) e débito não pago: ela fica bloqueada
            Locacao l3 = locacoes.reservar(carla, dolphin, agora, agora.plusDays(4), new SeguroBasico(), new PagamentoDinheiro());
            locacoes.retirar(l3.getId(), carlos, 95);
            ArrayList<Avaria> batida = new ArrayList<Avaria>();
            batida.add(new Avaria(TipoAvaria.LATARIA_MECANICA, "Para-choque traseiro amassado", 2500.00));
            locacoes.devolver(l3.getId(), carlos, agora.plusHours(6), dolphin.getKm() + 180, 0, 60, batida, false);

            // 4) Ana de novo - passeio sem seguro
            Locacao l4 = locacoes.reservar(ana, hb20, agora, agora.plusDays(2), new SemSeguro(), new PagamentoPix());
            locacoes.retirar(l4.getId(), mariana);
            locacoes.devolver(l4.getId(), mariana, agora.plusHours(3), hb20.getKm() + 120, 0);

            // 5) Elisa - SUV retirado (em andamento)
            Locacao l5 = locacoes.reservar(elisa, compass, agora, agora.plusDays(5), new SeguroCompleto(), new PagamentoCartao(3));
            locacoes.retirar(l5.getId(), carlos);

            // 6) Bruno - elétrico com cadeirinha, retirado (em andamento)
            ArrayList<Adicional> cadeirinha = new ArrayList<Adicional>();
            cadeirinha.add(Adicional.CADEIRINHA);
            Locacao l6 = locacoes.reservar(bruno, peugeot, agora, agora.plusDays(7), new SeguroBasico(), new PagamentoPix(), cadeirinha);
            locacoes.retirar(l6.getId(), mariana, 90);

            // 7) Diego - reserva para daqui a 2 dias (ainda não retirou)
            locacoes.reservar(diego, hb20, agora.plusDays(2), agora.plusDays(4), new SeguroBasico(), new PagamentoDinheiro());

            // 8) Ana - reserva cancelada
            Locacao l8 = locacoes.reservar(ana, strada, agora.plusDays(1), agora.plusDays(2), new SemSeguro(), new PagamentoPix());
            locacoes.cancelar(l8.getId());
        } catch (LocadoraException e){
            System.out.println("ERRO ao criar os aluguéis de exemplo: " + e.getMessage());
        }

        sistema.registrarAcao("Sistema", "DADOS DE EXEMPLO", "Carregados para demonstração");
        System.out.println("\nDados de exemplo carregados: " + locadora.getFrota().size() + " carros, "
                + clientes.getClientes().size() + " clientes, " + funcionarios.getFuncionarios().size()
                + " funcionários e " + locacoes.getLocacoes().size() + " aluguéis.");
        System.out.println("Funcionários para as vistorias: F001 (Carlos) e F002 (Mariana).");
    }
}
