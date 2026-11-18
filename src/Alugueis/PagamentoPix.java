package Alugueis;

public class PagamentoPix extends FormaPagamento {
    public PagamentoPix(){
        super("Pix");
    }

    // 5% de desconto
    @Override
    public double calcularTotal(double valor){
        return valor * 0.95;
    }
}
