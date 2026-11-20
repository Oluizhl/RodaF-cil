package Alugueis;

public class PagamentoCartao extends FormaPagamento {
    public static final int MAX_PARCELAS = 6;
    public static final int MAX_PARCELAS_SEM_JUROS = 3;
    public static final double ACRESCIMO = 0.05;
    public static final double PARCELA_MINIMA = 100.00;

    private int parcelas;

    public PagamentoCartao(int parcelas) throws PagamentoInvalidoException {
        super("Cartão");
        if (parcelas < 1 || parcelas > MAX_PARCELAS){
            throw new PagamentoInvalidoException("O cartão aceita de 1 a " + MAX_PARCELAS + " parcelas!");
        }
        this.parcelas = parcelas;
    }

    public int getParcelas(){
        return parcelas;
    }

    // à vista ou até 3x: sem juros; de 4x a 6x: acréscimo de 5% sobre o total
    @Override
    public double calcularTotal(double valor){
        if (parcelas <= MAX_PARCELAS_SEM_JUROS){
            return valor;
        }
        return valor * (1 + ACRESCIMO);
    }

    public double calcularParcela(double valor){
        return calcularTotal(valor) / parcelas;
    }

    // cada parcela deve ser de pelo menos R$ 100,00 (no à vista não tem parcela)
    public boolean parcelaValida(double valor){
        return parcelas == 1 || calcularParcela(valor) >= PARCELA_MINIMA - 0.005;
    }

    @Override
    public void validar(double valor) throws PagamentoInvalidoException {
        if (!parcelaValida(valor)){
            throw new PagamentoInvalidoException("Cada parcela precisa ser de pelo menos R$ 100,00 ("
                    + parcelas + "x de " + Orcamento.dinheiro(calcularParcela(valor)) + ").");
        }
    }

    @Override
    public String descrever(double valor){
        if (parcelas == 1){
            return "Cartão à vista (total R$ " + String.format("%.2f", calcularTotal(valor)) + ")";
        }
        return "Cartão em " + parcelas + "x de R$ " + String.format("%.2f", calcularParcela(valor))
                + " (total R$ " + String.format("%.2f", calcularTotal(valor)) + ")";
    }
}
