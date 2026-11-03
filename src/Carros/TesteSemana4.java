package Carros;

import java.util.ArrayList;

public class TesteSemana4 {
    public static void main(String[] args){
        Passeio passeio1 = new Passeio("PIV2E97", "Volkswagen", "Fox Xtreme", 2022, 66000, 100);
        Passeio passeio2 = new Passeio("SLT7H47", "Toyota", "Corolla", 2024, 29000, 120);
        Passeio passeio3 = new Passeio("RVE7J42", "Chevrolet", "Onix sedã LTZ", 2023, 41000, 110);
        Passeio passeio4 = new Passeio("QOQ9G12", "Renault", "Sandero EXPRESSION", 2019, 84000, 90);
        SUV suv1 = new SUV("PIR9D53", "Nissan", "Kicks", 2017, 139000, 110);
        SUV suv2 = new SUV("QRX7J09", "Volkswagen", "Nivus", 2021, 31000, 120);
        Utilitario utilitario1 = new Utilitario("PIC9J59", "Chevrolet", "S10", 2014, 105000, 140, 1000);
        Utilitario utilitario2 = new Utilitario("SMQ7D94", "Fiat", "Strada CS", 2025, 90000, 140, 800);
        Eletrico eletrico1 = new Eletrico("SLS9H69", "Peugeot", "2008e GT", 2022, 43000, 120, 220);
        Eletrico eletrico2 = new Eletrico("RTG6J89", "BYD", "Dolphin MINI", 2027, 3000, 130, 250);

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

        System.out.println("=== Teste das diárias (valor bruto do calcularDiaria) ===");
        for (Veiculo v : frota){
            System.out.println(v.getClass().getSimpleName() + " | " + v.getModelo()
                    + " | base R$" + v.getValorDiaria()
                    + " | diária R$" + v.calcularDiaria());
        }

        System.out.println();
        System.out.println("=== Mesmas diárias formatadas ===");
        for (Veiculo v : frota){
            System.out.println(v.getModelo() + " -> " + String.format("R$ %.2f", v.calcularDiaria()));
        }

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
    }
}