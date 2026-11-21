package Telas;

import Carros.Eletrico;
import Carros.Manutencao;
import Carros.Passeio;
import Carros.SUV;
import Carros.Utilitario;
import Carros.Veiculo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

// tela de carros: usa a Locadora (Parte 1)
public class TelaCarros extends Tela {
    private Sistema sistema;

    public TelaCarros(Sistema sistema){
        super("Carros");
        this.sistema = sistema;
    }

    @Override
    public String[] getOpcoes(){
        return new String[]{
                "Cadastrar carro",
                "Listar todos os carros",
                "Buscar por placa",
                "Buscar carros livres",
                "Buscar por tipo",
                "Buscar por faixa de preço",
                "Editar carro",
                "Registrar manutenção",
                "Realizar revisão",
                "Ver histórico de manutenção",
                "Remover carro"
        };
    }

    @Override
    public void executar(int opcao){
        switch (opcao){
            case 1: cadastrar(); break;
            case 2: listar(sistema.getLocadora().getFrota()); break;
            case 3: buscarPorPlaca(); break;
            case 4: listar(sistema.getLocadora().buscarLivres()); break;
            case 5: buscarPorTipo(); break;
            case 6: buscarPorFaixaDePreco(); break;
            case 7: editar(); break;
            case 8: registrarManutencao(); break;
            case 9: realizarRevisao(); break;
            case 10: verHistorico(); break;
            case 11: remover(); break;
        }
    }

    private void cadastrar(){
        System.out.println("Tipo do carro:");
        System.out.println(" 1 - Passeio   2 - SUV   3 - Utilitário   4 - Elétrico");
        int tipo = Entrada.lerInteiro("Tipo", 1, 4);

        String placa = lerPlaca();
        if (sistema.getLocadora().buscarPorPlaca(placa) != null){
            System.out.println("ERRO: Já existe um carro com essa placa!");
            return;
        }

        String marca = Entrada.lerTexto("Marca");
        String modelo = Entrada.lerTexto("Modelo");
        int ano = Entrada.lerInteiro("Ano", 1921, LocalDate.now().getYear());
        int km = Entrada.lerInteiro("Quilometragem", 1, 2000000);
        double diaria = Entrada.lerDecimal("Valor da diária (R$)");

        Veiculo veiculo;
        if (tipo == 1){
            veiculo = new Passeio(placa, marca, modelo, ano, km, diaria);
        } else if (tipo == 2){
            veiculo = new SUV(placa, marca, modelo, ano, km, diaria);
        } else if (tipo == 3){
            double carga = Entrada.lerDecimal("Capacidade de carga (kg)");
            veiculo = new Utilitario(placa, marca, modelo, ano, km, diaria, carga);
        } else {
            double autonomia = Entrada.lerDecimal("Autonomia com carga cheia (km)");
            veiculo = new Eletrico(placa, marca, modelo, ano, km, diaria, autonomia);
        }

        if (sistema.getLocadora().cadastrar(veiculo)){
            System.out.println("Carro cadastrado com sucesso!");
            sistema.registrarAcao("Sistema", "CADASTRO DE CARRO", placa + " - " + marca + " " + modelo);
        }
    }

    private void buscarPorPlaca(){
        Veiculo veiculo = escolherCarro();
        if (veiculo != null){
            mostrar(veiculo);
        }
    }

    private void buscarPorTipo(){
        System.out.println(" 1 - Passeio   2 - SUV   3 - Utilitário   4 - Elétrico");
        int tipo = Entrada.lerInteiro("Tipo", 1, 4);
        String[] nomes = {"Carros.Passeio", "Carros.SUV", "Carros.Utilitario", "Carros.Eletrico"};
        listar(sistema.getLocadora().buscarPorTipo(nomes[tipo - 1]));
    }

    private void buscarPorFaixaDePreco(){
        double minimo = Entrada.lerDecimal("Diária mínima (R$)");
        double maximo = Entrada.lerDecimal("Diária máxima (R$)");
        listar(sistema.getLocadora().buscarPorFaixaDePreco(minimo, maximo));
    }

    private void editar(){
        Veiculo veiculo = escolherCarro();
        if (veiculo == null){
            return;
        }
        mostrar(veiculo);

        int km = Entrada.lerInteiro("Nova quilometragem", veiculo.getKm(), 2000000);
        System.out.println("Situação: 1 - Livre   2 - Alugado   3 - Manutenção");
        String[] situacoes = {"Livre", "Alugado", "Manutenção"};
        String situacao = situacoes[Entrada.lerInteiro("Nova situação", 1, 3) - 1];
        double diaria = Entrada.lerDecimal("Novo valor da diária (R$)");

        sistema.getLocadora().editar(veiculo.getPlaca(), km, situacao, diaria);
    }

    private void registrarManutencao(){
        Veiculo veiculo = escolherCarro();
        if (veiculo == null){
            return;
        }
        String data = Entrada.lerData("Data do serviço").format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String servico = Entrada.lerTexto("Serviço realizado");
        double custo = Entrada.lerDecimal("Custo (R$)");

        veiculo.registrarManutencao(new Manutencao(data, servico, custo));
        System.out.println("Manutenção registrada!");
    }

    private void realizarRevisao(){
        Veiculo veiculo = escolherCarro();
        if (veiculo == null){
            return;
        }
        String data = Entrada.lerData("Data da revisão").format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        double custo = Entrada.lerDecimal("Custo (R$)");

        veiculo.realizarRevisao(data, custo);
        System.out.println("Revisão registrada! O carro está liberado para aluguel.");
    }

    private void verHistorico(){
        Veiculo veiculo = escolherCarro();
        if (veiculo != null){
            veiculo.mostrarHistorico();
        }
    }

    private void remover(){
        Veiculo veiculo = escolherCarro();
        if (veiculo == null){
            return;
        }
        if (veiculo.getSituacao().equals("Alugado")){
            System.out.println("ERRO: Não dá para remover um carro que está alugado!");
            return;
        }
        if (Entrada.lerSimNao("Remover " + veiculo.getModelo() + " (" + veiculo.getPlaca() + ")?")){
            sistema.getLocadora().remover(veiculo.getPlaca());
        }
    }

    // ---------- métodos de apoio ----------

    private String lerPlaca(){
        while (true){
            String placa = Entrada.lerTexto("Placa (padrão Mercosul, ex.: ABC1D23)").toUpperCase().replace("-", "");
            if (placa.matches("^[A-Z]{3}[0-9][A-Z][0-9]{2}$")){
                return placa;
            }
            System.out.println("ERRO: Placa fora do padrão Mercosul!");
        }
    }

    // devolve o carro ou null se a placa não existir
    public Veiculo escolherCarro(){
        String placa = Entrada.lerTexto("Placa do carro").toUpperCase().replace("-", "");
        Veiculo veiculo = sistema.getLocadora().buscarPorPlaca(placa);
        if (veiculo == null){
            System.out.println("ERRO: Carro com essa placa não encontrado!");
        }
        return veiculo;
    }

    public static String tipo(Veiculo v){
        if (v instanceof SUV) return "SUV";
        if (v instanceof Utilitario) return "Utilitário";
        if (v instanceof Eletrico) return "Elétrico";
        return "Passeio";
    }

    public static void listar(ArrayList<Veiculo> carros){
        if (carros.isEmpty()){
            System.out.println("Nenhum carro encontrado.");
            return;
        }
        System.out.printf("%-8s %-11s %-28s %-5s %-9s %-11s %s%n", "PLACA", "TIPO", "MARCA/MODELO", "ANO", "KM", "SITUAÇÃO", "DIÁRIA");
        for (Veiculo v : carros){
            String situacao = v.precisaRevisao() ? "Revisão!" : v.getSituacao();
            System.out.printf("%-8s %-11s %-28s %-5d %-9d %-11s R$ %.2f%n", v.getPlaca(), tipo(v),
                    v.getMarca() + " " + v.getModelo(), v.getAno(), v.getKm(), situacao, v.getValorDiaria());
        }
    }

    private void mostrar(Veiculo v){
        System.out.println("Placa: " + v.getPlaca() + " | Tipo: " + tipo(v));
        System.out.println("Carro: " + v.getMarca() + " " + v.getModelo() + " (" + v.getAno() + ")");
        System.out.println("Km: " + v.getKm() + " | Última revisão: " + v.getKmUltimaRevisao() + " km"
                + (v.precisaRevisao() ? " | PRECISA DE REVISÃO" : ""));
        System.out.println("Situação: " + v.getSituacao());
        System.out.println("Diária: R$ " + String.format("%.2f", v.getValorDiaria())
                + " | Limite: " + v.getLimiteKmPorDia() + " km/dia");
        if (v instanceof Utilitario){
            System.out.println("Capacidade de carga: " + ((Utilitario) v).getCapacidadeCarga() + " kg");
        }
        if (v instanceof Eletrico){
            System.out.println("Autonomia: " + ((Eletrico) v).getAutonomiaKm() + " km");
        }
    }
}
