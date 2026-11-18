package Alugueis;

public class PagamentoDinheiro extends FormaPagamento {
    public PagamentoDinheiro(){
        super("Dinheiro");
    }

    @Override
    public double calcularTotal(double valor){
        return valor;
    }
}
