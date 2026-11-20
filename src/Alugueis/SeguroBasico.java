package Alugueis;

public class SeguroBasico extends Seguro {
    public static final double VALOR_POR_DIA = 35.00;
    public static final double FRANQUIA = 2000.00;

    public SeguroBasico(){
        super("Seguro básico");
    }

    @Override
    public double calcularValor(int dias){
        return VALOR_POR_DIA * dias;
    }

    // lataria e mecânica: o cliente paga o conserto até o limite da franquia (R$ 2.000,00)
    // vidros, pneus e interior não são cobertos: o cliente paga 100%
    @Override
    public double calcularAvaria(Avaria avaria){
        if (avaria.getTipo() == TipoAvaria.LATARIA_MECANICA){
            return Math.min(avaria.getValorConserto(), FRANQUIA);
        }
        return avaria.getValorConserto();
    }

    @Override
    public String getCobertura(){
        return "Lataria e mecânica (batidas), com franquia de R$ 2.000,00. Não cobre vidros, pneus e interior.";
    }
}
