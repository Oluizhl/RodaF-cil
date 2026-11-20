package Alugueis;

import Carros.Veiculo;
import Clientes.Cliente;

import java.time.LocalDate;
import java.util.ArrayList;

public class GerenciadorLocacoes {
    private ArrayList<Locacao> locacoes;
    private int proximoId;

    public GerenciadorLocacoes(){
        locacoes = new ArrayList<Locacao>();
        proximoId = 1;
    }

    // reservar(sem adicionais) e reservar(com adicionais) têm o mesmo nome: sobrecarga
    public Locacao reservar(Cliente cliente, Veiculo veiculo, LocalDate retirada, LocalDate devolucao,
                            Seguro seguro, FormaPagamento pagamento) throws LocadoraException {
        return reservar(cliente, veiculo, retirada, devolucao, seguro, pagamento, new ArrayList<Adicional>());
    }

    public Locacao reservar(Cliente cliente, Veiculo veiculo, LocalDate retirada, LocalDate devolucao,
                            Seguro seguro, FormaPagamento pagamento, ArrayList<Adicional> adicionais)
            throws LocadoraException {
        if (retirada != null && retirada.isBefore(LocalDate.now())){
            throw new DatasInvalidasException("A retirada não pode ser no passado!");
        }

        // o construtor da Locacao confere datas, cliente e carro
        Locacao nova = new Locacao(proximoId, cliente, veiculo, retirada, devolucao, seguro, pagamento, adicionais);

        Locacao conflito = buscarConflito(veiculo, retirada, devolucao);
        if (conflito != null){
            throw new ReservaConflitanteException("O carro já está reservado nessas datas!");
        }

        proximoId++;
        locacoes.add(nova);
        return nova;
    }

    private Locacao buscarConflito(Veiculo veiculo, LocalDate retirada, LocalDate devolucao){
        for (Locacao l : locacoes){
            if (l.isEmAndamento() && l.isDoVeiculo(veiculo) && l.cruzaCom(retirada, devolucao)){
                return l;
            }
        }
        return null;
    }

    // para a tela de busca da Parte 5
    public boolean estaDisponivel(Veiculo veiculo, LocalDate retirada, LocalDate devolucao){
        if (!devolucao.isAfter(retirada)){
            return false;
        }
        if (veiculo.getSituacao().equals("Manutenção") || veiculo.precisaRevisao()){
            return false;
        }
        return buscarConflito(veiculo, retirada, devolucao) == null;
    }

    public Locacao retirar(int id) throws LocadoraException {
        Locacao l = buscarPorId(id);
        l.retirar();
        return l;
    }

    // devolver(4 parâmetros) paga os extras na hora; o de 5 deixa escolher
    public Fechamento devolver(int id, LocalDate dataReal, int kmFinal, double litrosFaltando) throws LocadoraException {
        return devolver(id, dataReal, kmFinal, litrosFaltando, true);
    }

    public Fechamento devolver(int id, LocalDate dataReal, int kmFinal, double litrosFaltando, boolean pagarExtrasAgora)
            throws LocadoraException {
        return buscarPorId(id).devolver(dataReal, kmFinal, litrosFaltando, pagarExtrasAgora);
    }

    public void cancelar(int id) throws LocadoraException {
        buscarPorId(id).cancelar();
    }

    public Locacao buscarPorId(int id) throws LocacaoNaoEncontradaException {
        for (Locacao l : locacoes){
            if (l.getId() == id){
                return l;
            }
        }
        throw new LocacaoNaoEncontradaException(id);
    }

    public ArrayList<Locacao> buscarPorSituacao(SituacaoLocacao situacao){
        ArrayList<Locacao> resultado = new ArrayList<Locacao>();

        for (Locacao l : locacoes){
            if (l.getSituacao() == situacao){
                resultado.add(l);
            }
        }

        return resultado;
    }

    public ArrayList<Locacao> buscarPorCliente(Cliente cliente){
        ArrayList<Locacao> resultado = new ArrayList<Locacao>();

        for (Locacao l : locacoes){
            if (l.getCliente().getCpf().equals(cliente.getCpf())){
                resultado.add(l);
            }
        }

        return resultado;
    }

    public void listar(){
        if (locacoes.isEmpty()){
            System.out.println("Nenhuma locação registrada.");
            return;
        }

        for (Locacao l : locacoes){
            System.out.println(l);
        }
    }

    // para as Partes 4 e 5 usarem (salvar em JSON, relatórios)
    public ArrayList<Locacao> getLocacoes(){
        return new ArrayList<Locacao>(locacoes);
    }
}
