package Alugueis;

import Carros.Veiculo;
import Clientes.Cliente;

import java.time.LocalDateTime;
import java.util.ArrayList;

// mostra quanto ficaria o aluguel sem reservar nada e sem bloquear nenhum carro
public class SimuladorOrcamento {

    // cliente pode ser null (ainda não cadastrado): sem desconto de fidelidade
    public static Orcamento simular(Cliente cliente, Veiculo veiculo, LocalDateTime retirada, LocalDateTime devolucao,
                                    Seguro seguro, FormaPagamento pagamento, ArrayList<Adicional> adicionais)
            throws LocadoraException {
        if (veiculo == null){
            throw new OperacaoInvalidaException("Escolha um carro para simular!");
        }
        int dias = Locacao.contarDias(retirada, devolucao);
        return new Orcamento(cliente, veiculo, retirada, dias, adicionais, seguro, pagamento);
    }

    public static Orcamento simular(Cliente cliente, Veiculo veiculo, LocalDateTime retirada, LocalDateTime devolucao,
                                    Seguro seguro, FormaPagamento pagamento) throws LocadoraException {
        return simular(cliente, veiculo, retirada, devolucao, seguro, pagamento, new ArrayList<Adicional>());
    }

    // a mesma viagem em cada forma de pagamento
    public static void compararPagamentos(Cliente cliente, Veiculo veiculo, LocalDateTime retirada, LocalDateTime devolucao,
                                          Seguro seguro, ArrayList<Adicional> adicionais) throws LocadoraException {
        Orcamento base = simular(cliente, veiculo, retirada, devolucao, seguro, new PagamentoDinheiro(), adicionais);

        FormaPagamento[] formas = {
                new PagamentoPix(),
                new PagamentoDinheiro(),
                new PagamentoCartao(1),
                new PagamentoCartao(3),
                new PagamentoCartao(6)
        };

        System.out.println(veiculo.getModelo() + " | " + base.getDias() + " dia(s) | subtotal " + Orcamento.dinheiro(base.getSubtotal()));
        for (FormaPagamento f : formas){
            String aviso = "";
            try {
                f.validar(base.getSubtotal());
            } catch (PagamentoInvalidoException e){
                aviso = "  <- indisponível: parcela abaixo de R$ 100,00";
            }
            System.out.println(" - " + f.descrever(base.getSubtotal()) + aviso);
        }
    }
}
