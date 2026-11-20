package Carros;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TesteSemana4 {
    private static void conferirValor(String descricao, double esperado, double obtido){
        if (Math.abs(esperado - obtido) < 0.01){
            System.out.println("[OK]    " + descricao + " = " + String.format("%.2f", obtido));
        } else {
            System.out.println("[FALHA] " + descricao + " (esperado " + esperado + ", obtido " + obtido + ")");
        }
    }

    private static void conferirBool(String descricao, boolean esperado, boolean obtido){
        System.out.println((esperado == obtido ? "[OK]    " : "[FALHA] ") + descricao);
    }

    public static void main(String[] args){
        Passeio passeio1 = new Passeio("PIV2E97", "Volkswagen", "Fox Xtreme", 2022, 66000, 120);
        Passeio passeio2 = new Passeio("SLT7H47", "Toyota", "Corolla", 2024, 29000, 120);
        Passeio passeio3 = new Passeio("RVE7J42", "Chevrolet", "Onix sedã LTZ", 2023, 41000, 120);
        Passeio passeio4 = new Passeio("QOQ9G12", "Renault", "Sandero EXPRESSION", 2019, 84000, 120);
        SUV suv1 = new SUV("PIR9D53", "Nissan", "Kicks", 2017, 139000, 200);
        SUV suv2 = new SUV("QRX7J09", "Volkswagen", "Nivus", 2021, 31000, 200);
        Utilitario utilitario1 = new Utilitario("PIC9J59", "Chevrolet", "S10", 2014, 105000, 180, 1000);
        Utilitario utilitario2 = new Utilitario("SMQ7D94", "Fiat", "Strada CS", 2025, 90000, 180, 800);
        Eletrico eletrico1 = new Eletrico("SLS9H69", "Peugeot", "2008e GT", 2022, 43000, 250, 220);
        Eletrico eletrico2 = new Eletrico("RTG6J89", "BYD", "Dolphin MINI", 2026, 3000, 250, 250);

        // Lista do tipo Veiculo: cada objeto responde do seu jeito (polimorfismo)
        ArrayList<Veiculo> frota = new ArrayList<>();
        frota.add(passeio1);
        frota.add(passeio2);
        frota.add(passeio3);
        frota.add(passeio4);
        frota.add(suv1);
        frota.add(suv2);
        frota.add(utilitario1);
        frota.add(utilitario2);
        frota.add(eletrico1);
        frota.add(eletrico2);

        System.out.println("=== Diária de cada tipo (valor de 1 diária) ===");
        for (Veiculo v : frota){
            System.out.println(v.getClass().getSimpleName() + " | " + v.getModelo()
                    + " | diária R$" + String.format("%.2f", v.calcularDiaria()));
        }

        System.out.println();
        System.out.println("=== Regra de cada tipo (diárias × dias, quinta 10h de retirada) ===");
        // 19/11/2026 é quinta-feira; as diárias são contadas de 24 em 24 horas a partir da retirada
        LocalDateTime quinta = LocalDateTime.of(2026, 11, 19, 10, 0);
        Passeio p120 = new Passeio("ABC1D23", "Fiat", "Argo", 2023, 20000, 120);
        SUV s200 = new SUV("RSK1J13", "Volkswagen", "Nivus", 2022, 35000, 200);
        Utilitario u180 = new Utilitario("UKF2G68", "RAM", "Rampage", 2026, 21000, 180, 1000);
        Eletrico e250 = new Eletrico("SLS9H69", "Peugeot", "2008e GT", 2022, 45000, 250, 220);

        conferirValor("Passeio 5 dias (5 x 120)", 600, p120.calcularValorDiarias(quinta, 5));
        conferirValor("SUV 2 dias qui-sex (200 + 230)", 430, s200.calcularValorDiarias(quinta, 2));
        conferirValor("SUV 5 dias qui a seg (200 + 3 x 230 + 200)", 1090, s200.calcularValorDiarias(quinta, 5));
        conferirValor("SUV 3 dias seg a qua (sem fim de semana)", 600, s200.calcularValorDiarias(quinta.plusDays(4), 3));
        conferirValor("Utilitário 3 dias (diárias sem acréscimo)", 540, u180.calcularValorDiarias(quinta, 3));
        conferirValor("Utilitário: taxa fixa de higienização", 60, u180.getTaxaFixa());
        conferirValor("Utilitário: limite de km por dia", 150, u180.getLimiteKmPorDia());
        conferirValor("Passeio: limite de km por dia", 100, p120.getLimiteKmPorDia());
        conferirValor("Elétrico 2 dias (sem desconto)", 500, e250.calcularValorDiarias(quinta, 2));
        conferirValor("Elétrico 3 dias (10% de desconto)", 675, e250.calcularValorDiarias(quinta, 3));

        System.out.println();
        System.out.println("=== Teste do bloqueio por km ===");
        Passeio teste = new Passeio("ABC1D23", "Fiat", "Argo", 2023, 20000, 100);
        System.out.println("km 20000 (recém-cadastrado): " + teste.precisaRevisao());

        teste.setKm(29999);
        System.out.println("km 29999 (diferença 9999):   " + teste.precisaRevisao());

        teste.setKm(30000);
        System.out.println("km 30000 (diferença 10000):  " + teste.precisaRevisao());

        teste.setKm(40000);
        System.out.println("km 40000 (diferença 20000):  " + teste.precisaRevisao());

        teste.realizarRevisao("03/11/2026", 450.0);
        System.out.println("após realizarRevisao:        " + teste.precisaRevisao());

        System.out.println();
        System.out.println("=== Cadastro: placa e busca por faixa de preço ===");
        Locadora locadora = new Locadora();
        conferirBool("Cadastro aceita placa Mercosul (ABC1D23)", true, locadora.cadastrar(p120));
        conferirBool("Cadastro recusa placa fora do padrão (ABC-1234)", false,
                locadora.cadastrar(new Passeio("ABC-1234", "Fiat", "Uno", 2020, 30000, 120)));
        conferirBool("Cadastro recusa placa repetida", false,
                locadora.cadastrar(new Passeio("ABC1D23", "Fiat", "Mobi", 2021, 20000, 120)));
        locadora.cadastrar(s200);
        locadora.cadastrar(u180);
        locadora.cadastrar(e250);
        conferirValor("Faixa 150 a 200: SUV e Utilitário", 2, locadora.buscarPorFaixaDePreco(150, 200).size());
        conferirValor("Faixa 120 a 120: só o Passeio", 1, locadora.buscarPorFaixaDePreco(120, 120).size());
        conferirValor("Faixa 300 a 400: nenhum", 0, locadora.buscarPorFaixaDePreco(300, 400).size());
    }
}