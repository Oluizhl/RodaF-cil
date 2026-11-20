package Alugueis;

import Carros.Veiculo;
import Clientes.Cliente;

import java.time.LocalDateTime;
import java.util.ArrayList;

// ordem do cálculo:
// 1) diárias (regra do tipo de carro)  2) desconto por período, só sobre as diárias
// 3) + taxa fixa, adicionais e seguro  4) desconto de fidelidade sobre esse subtotal
// 5) regra da forma de pagamento (desconto do Pix ou acréscimo do cartão)
public class Orcamento {
    public static final double DESCONTO_7_DIAS = 0.05;
    public static final double DESCONTO_15_DIAS = 0.10;
    public static final double DESCONTO_30_DIAS = 0.15;

    private int dias;
    private double diaria;
    private double valorDiarias;        // passo 1
    private double descontoDuracao;     // 0.05 = 5%
    private double valorDescontoDuracao;
    private double taxaFixa;            // higienização do Utilitário
    private double valorAdicionais;
    private double valorSeguro;
    private double subtotalSemFidelidade;
    private double descontoFidelidade;
    private double valorDescontoFidelidade;
    private double subtotal;            // antes da forma de pagamento
    private double total;
    private ArrayList<Adicional> adicionais;
    private Seguro seguro;
    private FormaPagamento pagamento;

    // cliente pode ser null (simulação sem cadastro): aí não tem desconto de fidelidade
    public Orcamento(Cliente cliente, Veiculo veiculo, LocalDateTime retirada, int dias, ArrayList<Adicional> adicionais,
                     Seguro seguro, FormaPagamento pagamento) throws LocadoraException {
        this.dias = dias;
        this.adicionais = (adicionais != null) ? new ArrayList<Adicional>(adicionais) : new ArrayList<Adicional>();
        this.seguro = (seguro != null) ? seguro : new SemSeguro();
        this.pagamento = (pagamento != null) ? pagamento : new PagamentoDinheiro();

        validarAdicionais(this.adicionais);

        // 1) diárias, com a regra do tipo de carro
        diaria = veiculo.calcularDiaria();
        valorDiarias = veiculo.calcularValorDiarias(retirada, dias);

        // 2) desconto por período, só sobre as diárias
        descontoDuracao = descontoPorDuracao(dias);
        valorDescontoDuracao = valorDiarias * descontoDuracao;

        // 3) taxa fixa, adicionais e seguro
        taxaFixa = veiculo.getTaxaFixa();
        for (Adicional a : this.adicionais){
            valorAdicionais += a.getValorDiario() * dias;
        }
        valorSeguro = this.seguro.calcularValor(dias);
        subtotalSemFidelidade = valorDiarias - valorDescontoDuracao + taxaFixa + valorAdicionais + valorSeguro;

        // 4) desconto de fidelidade sobre esse subtotal
        if (cliente != null){
            descontoFidelidade = cliente.getDescontoFidelidade();
        }
        valorDescontoFidelidade = subtotalSemFidelidade * descontoFidelidade;
        subtotal = subtotalSemFidelidade - valorDescontoFidelidade;

        // 5) forma de pagamento
        this.pagamento.validar(subtotal);
        total = this.pagamento.calcularTotal(subtotal);
    }

    // até 2 cadeirinhas por aluguel
    public static void validarAdicionais(ArrayList<Adicional> lista) throws OperacaoInvalidaException {
        int cadeirinhas = 0;
        for (Adicional a : lista){
            if (a == Adicional.CADEIRINHA){
                cadeirinhas++;
            }
        }
        if (cadeirinhas > Adicional.MAX_CADEIRINHAS){
            throw new OperacaoInvalidaException("Só dá para levar até " + Adicional.MAX_CADEIRINHAS + " cadeirinhas por aluguel!");
        }
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

    public double getValorDescontoDuracao(){
        return valorDescontoDuracao;
    }

    public double getTaxaFixa(){
        return taxaFixa;
    }

    public double getDescontoFidelidade(){
        return descontoFidelidade;
    }

    public double getValorDescontoFidelidade(){
        return valorDescontoFidelidade;
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
        String texto = "Diárias (" + dias + " dia(s), diária base " + dinheiro(diaria) + "): " + dinheiro(valorDiarias) + "\n";

        if (descontoDuracao > 0){
            texto += "Desconto pelo período (" + (int) Math.round(descontoDuracao * 100) + "%): - " + dinheiro(valorDescontoDuracao) + "\n";
        }
        if (taxaFixa > 0){
            texto += "Taxa de higienização: " + dinheiro(taxaFixa) + "\n";
        }
        for (Adicional a : adicionais){
            texto += a.getNome() + ": " + dinheiro(a.getValorDiario() * dias) + "\n";
        }
        texto += seguro.getNome() + ": " + dinheiro(valorSeguro) + "\n";
        if (descontoFidelidade > 0){
            texto += "Desconto de fidelidade (" + (int) Math.round(descontoFidelidade * 100) + "%): - " + dinheiro(valorDescontoFidelidade) + "\n";
        }
        texto += "Subtotal: " + dinheiro(subtotal) + "\n";
        texto += pagamento.descrever(subtotal) + "\n";
        texto += "TOTAL: " + dinheiro(total);
        return texto;
    }
}
