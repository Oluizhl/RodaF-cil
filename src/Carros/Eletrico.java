package Carros;

public class Eletrico extends Veiculo {
    private double autonomiaKm; // alcance com carga cheia

    public Eletrico(String placa, String marca, String modelo, int ano, int km, double valorDiaria, double autonomiaKm){
        super(placa, marca, modelo, ano, km, valorDiaria);
        this.autonomiaKm = autonomiaKm;
    }

    @Override
    public double calcularDiaria(){
        if (autonomiaKm > 400){
            return getValorDiaria() * 0.85; // carro mais eficiente, mais desconto
        } else {
            return getValorDiaria() * 0.92;
        }
    }
}
