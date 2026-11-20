package Carros;

import java.time.LocalDateTime;

public class Utilitario extends Veiculo {
    public static final double TAXA_HIGIENIZACAO = 60.00;

    private double capacidadeCarga; // em kg

    public Utilitario(String placa, String marca, String modelo, int ano, int km, double valorDiaria, double capacidadeCarga){
        super(placa, marca, modelo, ano, km, valorDiaria);
        this.capacidadeCarga = capacidadeCarga;
    }

    public double getCapacidadeCarga(){
        return capacidadeCarga;
    }

    // as diárias não têm acréscimo; a taxa de higienização vem à parte (getTaxaFixa)
    @Override
    public double calcularValorDiarias(LocalDateTime retirada, int dias){
        return getValorDiaria() * dias;
    }

    @Override
    public double getTaxaFixa(){
        return TAXA_HIGIENIZACAO;
    }

    @Override
    public int getLimiteKmPorDia(){
        return 150;
    }
}
