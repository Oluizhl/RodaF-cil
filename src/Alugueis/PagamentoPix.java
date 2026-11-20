package Alugueis;

public class PagamentoPix extends FormaPagamento {
    public static final double DESCONTO = 0.05;

    public PagamentoPix(){
        super("Pix");
    }

    // 5% de desconto sobre o total do aluguel
    @Override
    public double calcularTotal(double valor){
        return valor * (1 - DESCONTO);
    }
}
