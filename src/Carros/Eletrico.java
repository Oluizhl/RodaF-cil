package Carros;

import java.time.LocalDateTime;

public class Eletrico extends Veiculo {
    public static final double DESCONTO_3_DIAS = 0.10;

    private double autonomiaKm; // alcance com carga cheia

    public Eletrico(String placa, String marca, String modelo, int ano, int km, double valorDiaria, double autonomiaKm){
        super(placa, marca, modelo, ano, km, valorDiaria);
        this.autonomiaKm = autonomiaKm;
    }

    public double getAutonomiaKm(){
        return autonomiaKm;
    }

    // desconto de 10% nas diárias em aluguéis de 3 dias ou mais
    @Override
    public double calcularValorDiarias(LocalDateTime retirada, int dias){
        double total = getValorDiaria() * dias;

        if (dias >= 3){
            total = total * (1 - DESCONTO_3_DIAS);
        }

        return total;
    }
}
