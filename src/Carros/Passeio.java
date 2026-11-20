package Carros;

import java.time.LocalDateTime;

public class Passeio extends Veiculo {
    public Passeio(String placa, String marca, String modelo, int ano, int km, double valorDiaria){
        super(placa, marca, modelo, ano, km, valorDiaria);
    }

    // diária × número de dias, sem acréscimo
    @Override
    public double calcularValorDiarias(LocalDateTime retirada, int dias){
        return getValorDiaria() * dias;
    }
}
