package Carros;

public class SUV extends Veiculo {
    public SUV (String placa, String marca, String modelo, int ano, int km, double valorDiaria){
        super(placa, marca, modelo, ano, km, valorDiaria);
    }

    @Override
    public double calcularDiaria(){
        if (getKm() > 50000){
            return getValorDiaria() * 1.20;
        } else {
            return getValorDiaria() * 1.10;
        }
    }
}
