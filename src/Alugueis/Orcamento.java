package Alugueis;

import Carros.Veiculo;
import Clientes.Cliente;

import java.util.ArrayList;

public class Orcamento {
    public static final double DESCONTO_7_DIAS = 0.05;
    public static final double DESCONTO_15_DIAS = 0.10;
    public static final double DESCONTO_30_DIAS = 0.15;

    private int dias;
    private double diaria;
    private double valorDiarias;
    private double descontoDuracao;
    private double descontoFidelidade;
    private double valorAdicionais;
    private double valorSeguro;
    private double subtotal; // antes da forma de pagamento
    private double total;
    private ArrayList<Adicional> adicionais;
    private Seguro seguro;
    private FormaPagamento pagamento;

    // cliente pode ser null (simulação sem cadastro): aí não tem desconto de fidelidade
    public Orcamento(Cliente cliente, Veiculo veiculo, int dias, ArrayList<Adicional> adicionais,
                     Seguro seguro, FormaPagamento pagamento){
        this.dias = dias;
        this.adicionais = (adicionais != null) ? adicionais : new ArrayList<Adicional>();
        this.seguro = (seguro != null) ? seguro : new SemSeguro();
        this.pagamento = (pagamento != null) ? pagamento : new PagamentoDinheiro();

        diaria = veiculo.calcularDiaria();
        valorDiarias = diaria * dias;
        descontoDuracao = descontoPorDuracao(dias);

        if (cliente != null){
            descontoFidelidade = cliente.getDescontoFidelidade();
        }

        double valor = valorDiarias * (1 - descontoDuracao) * (1 - descontoFidelidade);

        for (Adicional a : this.adicionais){
            valorAdicionais += a.getValorDiario() * dias;
        }
        valorSeguro = this.seguro.calcularValor(diaria, dias);

        subtotal = valor + valorAdicionais + valorSeguro;
        total = this.pagamento.calcularTotal(subtotal);
    }

    public static double descontoPorDuracao(int dias){
        if (dias >= 30){
            return DESCONTO_30_DIAS;
        } else if (dias >= 15){
            return DESCONTO_15_DIAS;
        } else if (dias >= 7){
            return DESCONTO_7_DIAS;
        }
        return 0;
    }

    public static String dinheiro(double valor){
        return "R$ " + String.format("%.2f", valor);
    }

    public int getDias(){
        return dias;
    }

    public double getDiaria(){
        return diaria;
    }

    public double getValorDiarias(){
        return valorDiarias;
    }

    public double getDescontoDuracao(){
        return descontoDuracao;
    }

    public double getDescontoFidelidade(){
        return descontoFidelidade;
    }

    public double getValorAdicionais(){
        return valorAdicionais;
    }

    public double getValorSeguro(){
        return valorSeguro;
    }

    public double getSubtotal(){
        return subtotal;
    }

    public double getTotal(){
        return total;
    }

    public ArrayList<Adicional> getAdicionais(){
        return adicionais;
    }

    public Seguro getSeguro(){
        return seguro;
    }

    public FormaPagamento getPagamento(){
        return pagamento;
    }

    @Override
    public String toString(){
        String texto = "Diárias: " + dias + " x " + dinheiro(diaria) + " = " + dinheiro(valorDiarias) + "\n";

        if (descontoDuracao > 0){
            texto += "Desconto pela duração (" + (int) (descontoDuracao * 100) + "%)\n";
        }
        if (descontoFidelidade > 0){
            texto += "Desconto de fidelidade (" + (int) (descontoFidelidade * 100) + "%)\n";
        }
        for (Adicional a : adicionais){
            texto += a.getNome() + ": " + dinheiro(a.getValorDiario() * dias) + "\n";
        }
        texto += seguro.getNome() + ": " + dinheiro(valorSeguro) + "\n";
        texto += "Subtotal: " + dinheiro(subtotal) + "\n";
        texto += pagamento.descrever(subtotal) + "\n";
        texto += "TOTAL: " + dinheiro(total);
        return texto;
    }
}
