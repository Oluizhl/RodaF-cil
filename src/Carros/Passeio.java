package Carros;

public class Passeio extends Veiculo {
    public Passeio(String placa, String marca, String modelo, int ano, int km, double valorDiaria){
        super(placa, marca, modelo, ano, km, valorDiaria);
        }

    @Override
    public double calcularDiaria(){
        return getValorDiaria();
    }
}
