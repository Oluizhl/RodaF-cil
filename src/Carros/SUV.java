package Carros;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class SUV extends Veiculo {
    public static final double ACRESCIMO_FIM_DE_SEMANA = 0.15;

    public SUV (String placa, String marca, String modelo, int ano, int km, double valorDiaria){
        super(placa, marca, modelo, ano, km, valorDiaria);
    }

    // acréscimo de 15% nas diárias de sexta, sábado e domingo
    // (cada diária começa 24 horas depois da anterior, a partir do horário da retirada)
    @Override
    public double calcularValorDiarias(LocalDateTime retirada, int dias){
        double total = 0;

        for (int i = 0; i < dias; i++){
            DayOfWeek dia = retirada.plusDays(i).getDayOfWeek();
            if (dia == DayOfWeek.FRIDAY || dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY){
                total += getValorDiaria() * (1 + ACRESCIMO_FIM_DE_SEMANA);
            } else {
                total += getValorDiaria();
            }
        }

        return total;
    }
}
