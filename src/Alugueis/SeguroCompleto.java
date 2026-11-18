package Alugueis;

public class SeguroCompleto extends Seguro {
    public SeguroCompleto(){
        super("Seguro completo");
    }

    // 25% da diária por dia + R$ 50 de taxa fixa
    @Override
    public double calcularValor(double diaria, int dias){
        return diaria * 0.25 * dias + 50;
    }

    @Override
    public String getCobertura(){
        return "Colisão, roubo, terceiros e vidros, com franquia baixa.";
    }
}
