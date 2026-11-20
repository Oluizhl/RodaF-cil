package Alugueis;

public abstract class FormaPagamento {
    private String nome;

    public FormaPagamento(String nome){
        this.nome = nome;
    }

    public String getNome(){
        return nome;
    }

    // cada forma de pagamento calcula o total do seu jeito (polimorfismo)
    public abstract double calcularTotal(double valor);

    // confere se a forma de pagamento aceita esse valor (só o cartão tem regra)
    public void validar(double valor) throws PagamentoInvalidoException {
    }

    public String descrever(double valor){
        return nome + ": R$ " + String.format("%.2f", calcularTotal(valor));
    }
}
