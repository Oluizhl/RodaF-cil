package Alugueis;

public class PagamentoCartao extends FormaPagamento {
    private int parcelas;

    public PagamentoCartao(int parcelas) throws PagamentoInvalidoException {
        super("Cartão");
        if (parcelas < 1 || parcelas > 12){
            throw new PagamentoInvalidoException("O cartão aceita de 1 a 12 parcelas!");
        }
        this.parcelas = parcelas;
    }

    public int getParcelas(){
        return parcelas;
    }

    // até 3x sem juros; acima disso, 1,5% de juros por parcela
    @Override
    public double calcularTotal(double valor){
        if (parcelas <= 3){
            return valor;
        }
        return valor * (1 + 0.015 * parcelas);
    }

    public double calcularParcela(double valor){
        return calcularTotal(valor) / parcelas;
    }

    @Override
    public String descrever(double valor){
        return "Cartão em " + parcelas + "x de R$ " + String.format("%.2f", calcularParcela(valor))
                + " (total R$ " + String.format("%.2f", calcularTotal(valor)) + ")";
    }
}
