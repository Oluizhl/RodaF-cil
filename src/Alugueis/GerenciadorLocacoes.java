package Alugueis;

import Carros.Veiculo;
import Clientes.Cliente;
import Clientes.Funcionario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class GerenciadorLocacoes {
    private ArrayList<Locacao> locacoes;
    private int proximoId;

    public GerenciadorLocacoes(){
        locacoes = new ArrayList<Locacao>();
        proximoId = 1;
    }

    // reservar tem 3 versões com o mesmo nome (sobrecarga): sem adicionais, com adicionais e com condutor extra
    public Locacao reservar(Cliente cliente, Veiculo veiculo, LocalDateTime retirada, LocalDateTime devolucao,
                            Seguro seguro, FormaPagamento pagamento) throws LocadoraException {
        return reservar(cliente, veiculo, retirada, devolucao, seguro, pagamento, new ArrayList<Adicional>(), null);
    }

    public Locacao reservar(Cliente cliente, Veiculo veiculo, LocalDateTime retirada, LocalDateTime devolucao,
                            Seguro seguro, FormaPagamento pagamento, ArrayList<Adicional> adicionais)
            throws LocadoraException {
        return reservar(cliente, veiculo, retirada, devolucao, seguro, pagamento, adicionais, null);
    }

    // condutorExtra: precisa de CNH válida e passa pelas mesmas regras de idade do cliente
    public Locacao reservar(Cliente cliente, Veiculo veiculo, LocalDateTime retirada, LocalDateTime devolucao,
                            Seguro seguro, FormaPagamento pagamento, ArrayList<Adicional> adicionais, Cliente condutorExtra)
            throws LocadoraException {
        if (retirada != null && retirada.toLocalDate().isBefore(LocalDate.now())){
            throw new DatasInvalidasException("A retirada não pode ser no passado!");
        }

        // o construtor da Locacao confere datas, cliente, condutor extra e carro
        Locacao nova = new Locacao(proximoId, cliente, veiculo, retirada, devolucao, seguro, pagamento, adicionais, condutorExtra);

        Locacao conflito = buscarConflito(veiculo, retirada, devolucao);
        if (conflito != null){
            throw new ReservaConflitanteException("O carro já está reservado nessas datas!");
        }

        proximoId++;
        locacoes.add(nova);
        return nova;
    }

    private Locacao buscarConflito(Veiculo veiculo, LocalDateTime retirada, LocalDateTime devolucao){
        for (Locacao l : locacoes){
            if (l.isEmAndamento() && l.isDoVeiculo(veiculo) && l.cruzaCom(retirada, devolucao)){
                return l;
            }
        }
        return null;
    }

    // para a tela de busca da Parte 5
    public boolean estaDisponivel(Veiculo veiculo, LocalDateTime retirada, LocalDateTime devolucao){
        if (!devolucao.isAfter(retirada)){
            return false;
        }
        if (veiculo.getSituacao().equals("Manutenção") || veiculo.precisaRevisao()){
            return false;
        }
        return buscarConflito(veiculo, retirada, devolucao) == null;
    }

    // toda retirada registra o funcionário que fez a vistoria
    public Locacao retirar(int id, Funcionario funcionario) throws LocadoraException {
        Locacao l = buscarPorId(id);
        l.retirar(funcionario);
        return l;
    }

    // elétrico: informar a carga da bateria na saída (mínimo 80%)
    public Locacao retirar(int id, Funcionario funcionario, int cargaBateria) throws LocadoraException {
        Locacao l = buscarPorId(id);
        l.retirar(funcionario, cargaBateria);
        return l;
    }

    // toda devolução registra o funcionário que fez a vistoria (km, combustível ou bateria e avarias)
    // devolver tem versões com o mesmo nome (sobrecarga): a mais simples paga os extras na hora
    public Fechamento devolver(int id, Funcionario funcionario, LocalDateTime dataReal, int kmFinal, double litrosFaltando)
            throws LocadoraException {
        return devolver(id, funcionario, dataReal, kmFinal, litrosFaltando, -1, new ArrayList<Avaria>(), true);
    }

    public Fechamento devolver(int id, Funcionario funcionario, LocalDateTime dataReal, int kmFinal, double litrosFaltando,
                               ArrayList<Avaria> avarias, boolean pagarExtrasAgora) throws LocadoraException {
        return devolver(id, funcionario, dataReal, kmFinal, litrosFaltando, -1, avarias, pagarExtrasAgora);
    }

    public Fechamento devolver(int id, Funcionario funcionario, LocalDateTime dataReal, int kmFinal, double litrosFaltando,
                               int cargaBateria, ArrayList<Avaria> avarias, boolean pagarExtrasAgora)
            throws LocadoraException {
        return buscarPorId(id).devolver(funcionario, dataReal, kmFinal, litrosFaltando, cargaBateria, avarias, pagarExtrasAgora);
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
