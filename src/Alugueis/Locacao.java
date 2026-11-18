package Alugueis;

import Carros.Veiculo;
import Clientes.Cliente;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Locacao {
    private int id;
    private Cliente cliente;
    private Veiculo veiculo;
    private LocalDate dataRetirada;
    private LocalDate dataPrevistaDevolucao;
    private SituacaoLocacao situacao;
    private Orcamento orcamento;

    // preenchidos na retirada e na devolução
    private int kmRetirada;
    private LocalDate dataDevolucaoReal;
    private Fechamento fechamento;

    // cria uma reserva; a situação só muda por retirar(), devolver() e cancelar() (encapsulamento)
    public Locacao(int id, Cliente cliente, Veiculo veiculo, LocalDate dataRetirada, LocalDate dataPrevistaDevolucao,
                   Seguro seguro, FormaPagamento pagamento, ArrayList<Adicional> adicionais) throws LocadoraException {
        if (cliente == null || veiculo == null){
            throw new OperacaoInvalidaException("A locação precisa de um cliente e de um carro!");
        }

        int dias = contarDias(dataRetirada, dataPrevistaDevolucao);

        this.id = id;
        this.cliente = cliente;
        this.veiculo = veiculo;
        this.dataRetirada = dataRetirada;
        this.dataPrevistaDevolucao = dataPrevistaDevolucao;

        verificarCliente();
        verificarCarro(false);

        this.orcamento = new Orcamento(cliente, veiculo, dias, adicionais, seguro, pagamento);
        this.situacao = SituacaoLocacao.RESERVADA;
    }

    public static int contarDias(LocalDate retirada, LocalDate devolucao) throws DatasInvalidasException {
        if (retirada == null || devolucao == null){
            throw new DatasInvalidasException("Informe as duas datas!");
        }
        if (!devolucao.isAfter(retirada)){
            throw new DatasInvalidasException("A devolução precisa ser depois da retirada!");
        }
        return (int) ChronoUnit.DAYS.between(retirada, devolucao);
    }

    public static String data(LocalDate data){
        if (data == null){
            return "-";
        }
        return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    // ---------- o cliente pode alugar e o carro pode ser alugado? ----------

    private void verificarCliente() throws LocadoraException {
        if (cliente.isBloqueado()){
            throw new ClienteBloqueadoException("Cliente bloqueado: deve " + cliente.getMotivoDebito() + ".");
        }

        String motivo = cliente.motivoNaoPodeDirigir(veiculo);
        if (motivo != null){
            throw new ClienteNaoHabilitadoException(motivo);
        }

        // a CNH precisa valer até a devolução
        if (cliente.getCnh().getValidade().isBefore(dataPrevistaDevolucao)){
            throw new ClienteNaoHabilitadoException("A CNH vence antes da devolução.");
        }
    }

    // na reserva o carro pode estar alugado agora; na retirada ele precisa estar livre
    private void verificarCarro(boolean precisaEstarLivre) throws CarroIndisponivelException {
        if (veiculo.getSituacao().equals("Manutenção")){
            throw new CarroIndisponivelException("Carro em manutenção.");
        }
        if (veiculo.precisaRevisao()){
            throw new CarroIndisponivelException("Carro precisa de revisão.");
        }
        if (precisaEstarLivre && !veiculo.getSituacao().equals("Livre")){
            throw new CarroIndisponivelException("Carro não está livre.");
        }
    }

    // ---------- mudanças de situação ----------

    public void retirar() throws LocadoraException {
        if (situacao != SituacaoLocacao.RESERVADA){
            throw new OperacaoInvalidaException("Só dá para retirar uma locação reservada!");
        }

        // confere de novo: o cliente pode ter sido bloqueado e o carro pode ter ido para a oficina
        verificarCliente();
        verificarCarro(true);

        kmRetirada = veiculo.getKm();
        veiculo.setSituacao("Alugado");
        situacao = SituacaoLocacao.ATIVA;
    }

    // se pagarExtrasAgora for false, os extras viram débito e o cliente fica bloqueado
    public Fechamento devolver(LocalDate dataReal, int kmFinal, double litrosFaltando, boolean pagarExtrasAgora)
            throws LocadoraException {
        if (situacao != SituacaoLocacao.ATIVA){
            throw new OperacaoInvalidaException("Só dá para devolver uma locação ativa!");
        }
        if (dataReal == null || dataReal.isBefore(dataRetirada)){
            throw new DatasInvalidasException("Data de devolução inválida!");
        }
        if (kmFinal < kmRetirada){
            throw new OperacaoInvalidaException("O km final é menor que o km da retirada!");
        }
        if (litrosFaltando < 0){
            throw new OperacaoInvalidaException("Litros faltando inválido!");
        }

        fechamento = new Fechamento(veiculo, orcamento.getDias(), dataPrevistaDevolucao, dataReal,
                kmFinal - kmRetirada, litrosFaltando, orcamento.getTotal(), pagarExtrasAgora);

        dataDevolucaoReal = dataReal;
        veiculo.setKm(kmFinal);
        veiculo.setSituacao("Livre");
        situacao = SituacaoLocacao.FINALIZADA;

        // fidelidade: pontos pelos dias que ficou com o carro
        int diasUsados = (int) ChronoUnit.DAYS.between(dataRetirada, dataReal);
        if (diasUsados < 1){
            diasUsados = 1;
        }
        cliente.registrarAluguel(diasUsados);

        if (fechamento.getTotalExtras() > 0 && !pagarExtrasAgora){
            cliente.registrarDebito(fechamento.getTotalExtras(), "multa da locação nº " + id);
        }

        return fechamento;
    }

    public void cancelar() throws LocadoraException {
        if (situacao != SituacaoLocacao.RESERVADA){
            throw new OperacaoInvalidaException("Só dá para cancelar uma locação reservada!");
        }
        situacao = SituacaoLocacao.CANCELADA;
    }

    // ---------- datas que se cruzam ----------

    // devolver num dia e outro cliente retirar nesse mesmo dia pode, então não usa <=
    public boolean cruzaCom(LocalDate novaRetirada, LocalDate novaDevolucao){
        return novaRetirada.isBefore(dataPrevistaDevolucao) && dataRetirada.isBefore(novaDevolucao);
    }

    // reservada ou ativa: ainda está segurando o carro
    public boolean isEmAndamento(){
        return situacao == SituacaoLocacao.RESERVADA || situacao == SituacaoLocacao.ATIVA;
    }

    public boolean isDoVeiculo(Veiculo outro){
        return veiculo.getPlaca().equals(outro.getPlaca());
    }

    public int getId(){
        return id;
    }

    public Cliente getCliente(){
        return cliente;
    }

    public Veiculo getVeiculo(){
        return veiculo;
    }

    public LocalDate getDataRetirada(){
        return dataRetirada;
    }

    public LocalDate getDataPrevistaDevolucao(){
        return dataPrevistaDevolucao;
    }

    public SituacaoLocacao getSituacao(){
        return situacao;
    }

    public Orcamento getOrcamento(){
        return orcamento;
    }

    public int getKmRetirada(){
        return kmRetirada;
    }

    public LocalDate getDataDevolucaoReal(){
        return dataDevolucaoReal;
    }

    public Fechamento getFechamento(){
        return fechamento;
    }

    @Override
    public String toString(){
        return "Locação nº " + id + " | " + cliente.getNome() + " | " + veiculo.getModelo() + " (" + veiculo.getPlaca() + ")"
                + " | " + data(dataRetirada) + " a " + data(dataPrevistaDevolucao) + " | " + situacao.getNome();
    }
}
