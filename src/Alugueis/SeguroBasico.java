package Alugueis;

public class SeguroBasico extends Seguro {
    public SeguroBasico(){
        super("Seguro básico");
    }

    // 10% da diária por dia
    @Override
    public double calcularValor(double diaria, int dias){
        return diaria * 0.10 * dias;
    }

    @Override
    public String getCobertura(){
        return "Colisão e roubo, com franquia alta.";
    }
}
