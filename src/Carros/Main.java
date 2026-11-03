package Carros;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args){
        Locadora locadora = new Locadora();

        Passeio passeio1 = new Passeio("RTK1G75", "Hyundai", "HB20s Evolution", 2022, 67000, 100);
        SUV suv1 = new SUV("RSK1J13", "Volkswagen", "Nivus Highline", 2022, 35000, 120);
        Utilitario utilitario1 = new Utilitario("UKF2G68", "RAM", "Rampage Rebel", 2026, 21000, 150, 1000);
        Eletrico eletrico1 = new Eletrico("SLS9H69", "Peugeot", "2008e GT", 2022, 45000, 120, 220);

        locadora.cadastrar(passeio1);
        locadora.cadastrar(suv1);
        locadora.cadastrar(utilitario1);
        locadora.cadastrar(eletrico1);

        locadora.listar();

        ArrayList<Veiculo> porTipo1 = locadora.buscarPorTipo("Carros.Passeio");
        System.out.println("--- Busca por tipo: Passeio ---");
        for (Veiculo v : porTipo1){
            System.out.println(v.getModelo() + " - R$" + v.calcularDiaria());
        }

        ArrayList<Veiculo> porTipo2 = locadora.buscarPorTipo("Carros.SUV");
        System.out.println("--- Busca por tipo: SUV ---");
        for (Veiculo v : porTipo2){
            System.out.println(v.getModelo() + " - R$" + v.calcularDiaria());
        }

        ArrayList<Veiculo> porTipo3 = locadora.buscarPorTipo("Carros.Utilitario");
        System.out.println("--- Busca por tipo: Utilitário ---");
        for(Veiculo v : porTipo3){
            System.out.println(v.getModelo() + " - R$" + v.calcularDiaria());
        }

        ArrayList<Veiculo> porTipo4 = locadora.buscarPorTipo("Carros.Eletrico");
        System.out.println("--- Busca por tipo: Elétrico ---");
        for(Veiculo v : porTipo4){
            System.out.println(v.getModelo() + " - R$" + v.calcularDiaria());
        }

        ArrayList<Veiculo> porTipo5 = locadora.buscarPorTipo("Caminhao");
        System.out.println("--- Buscar por tipo: Caminhão ---");
        if (porTipo5.isEmpty()){
            System.out.println("Não temos opções desse tipo de veículo!");
        } else {
            for (Veiculo v : porTipo5){
                System.out.println(v.getModelo() + " - R$" + v.calcularDiaria());
            }
        }

        ArrayList<Veiculo> porPreco1 = locadora.buscarPorPreco(50);
        System.out.println("--- Busca por preço: até R$50 ---");
        if (porPreco1.isEmpty()){
            System.out.println("Nenhum carro nessa faixa de preço!");
        } else {
            for (Veiculo v : porPreco1){
                System.out.println(v.getModelo() + " - R$" + v.calcularDiaria());
            }
        }

        ArrayList<Veiculo> porPreco2 = locadora.buscarPorPreco(100);
        System.out.println("--- Busca por preço: até R$100 ---");
        if (porPreco2.isEmpty()){
            System.out.println("Nenhum carro nessa faixa de preço!");
        } else {
            for (Veiculo v : porPreco2){
                System.out.println(v.getModelo() + " - R$" + v.calcularDiaria());
            }
        }

        ArrayList<Veiculo> porPreco3 = locadora.buscarPorPreco(120);
        System.out.println("--- Busca por preço: até R$120 ---");
        if (porPreco3.isEmpty()){
            System.out.println("Nenhum carro nessa faixa de preço!");
        } else {
            for (Veiculo v : porPreco3){
                System.out.println(v.getModelo() + " - R$" + v.calcularDiaria());
            }
        }

        ArrayList<Veiculo> porPreco4 = locadora.buscarPorPreco(200);
        System.out.println("--- Busca por preço: até R$200 ---");
        if (porPreco4.isEmpty()){
            System.out.println("Nenhum carro nessa faixa de preço!");
        } else {
            for (Veiculo v : porPreco4){
                System.out.println(v.getModelo() + " - R$" + v.calcularDiaria());
            }
        }

        suv1.setSituacao("Alugado");

        ArrayList<Veiculo> livres1 = locadora.buscarLivres();
        System.out.println("--- Carros livres ---");
        if (livres1.isEmpty()){      // Verifica se a lista de carros livres está vazia.
            System.out.println("Nenhum carro livre!");
        } else {
            for (Veiculo v : livres1){         // Faz uma lista de carros que estão com situação LIVRE.
                System.out.println(v.getModelo() + " - " + v.getSituacao());
            }
        }

        locadora.editar("RTK1G75", 70000, "Manutenção", 110.0);
        locadora.listar();  // Pega o carro com a respectiva placa, substitui o KM, troca a situação para MANUTENÇÃO e coloca o novo valor da diária.

        locadora.remover("RSK1J13");
        locadora.listar();

        locadora.mostrarHistorico("RTK1G75");   // 1. histórico vazio

        passeio1.registrarManutencao(new Manutencao("02/11/2026", "Troca de óleo", 250.0));
        passeio1.registrarManutencao(new Manutencao("02/11/2026", "Freios", 480.0));
        locadora.mostrarHistorico("RTK1G75");   // 2. duas manutenções

        locadora.mostrarHistorico("AAA1A11");   // 3. placa inexistente
        locadora.mostrarHistorico("SLS9H69");   // 4. outro carro, deve estar vazio

        Passeio t = new Passeio("ABC1D23", "Fiat", "Argo", 2022, 5000, 100);
        locadora.cadastrar(t);

        t.setKm(15000);
        locadora.alugarCarro("ABC1D23");        // 1

        t.realizarRevisao("02/11/2026", 300.0);
        locadora.alugarCarro("ABC1D23");        // 2

        locadora.mostrarHistorico("ABC1D23");   // 3

        t.setKm(25000);
        locadora.alugarCarro("ABC1D23");        // 4

        t.setSituacao("Manutenção");
        t.realizarRevisao("02/11/2026", 300.0);
        System.out.println(t.getSituacao());    // 5
    }
}