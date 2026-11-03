package Carros;

public class Utilitario extends Veiculo {
    private double capacidadeCarga; // em kg

    public Utilitario(String placa, String marca, String modelo, int ano, int km, double valorDiaria, double capacidadeCarga){
        super(placa, marca, modelo, ano, km, valorDiaria);
        this.capacidadeCarga = capacidadeCarga;
    }

    @Override
    public double calcularDiaria(){
        if (capacidadeCarga > 1000){
            return getValorDiaria() * 1.40;
        } else {
            return getValorDiaria() * 1.25;
        }
    }
}