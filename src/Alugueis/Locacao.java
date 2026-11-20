package Alugueis;

import Carros.Eletrico;
import Carros.Veiculo;
import Clientes.Cliente;
import Clientes.Funcionario;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Locacao {
    private int id;
    private Cliente cliente;
    private Cliente condutorExtra; // null se não tem condutor extra
    private Veiculo veiculo;
    private LocalDateTime dataRetirada;
    private LocalDateTime dataPrevistaDevolucao;
    private SituacaoLocacao situacao;
    private Orcamento orcamento;

    // preenchidos na retirada e na devolução (cada vistoria registra o funcionário)
    private int kmRetirada;
    private Vistoria vistoriaRetirada;
    private Vistoria vistoriaDevolucao;
    private LocalDateTime dataDevolucaoReal;
    private Fechamento fechamento;

    // cria uma reserva; a situação só muda por retirar(), devolver() e cancelar() (encapsulamento)
    public Locacao(int id, Cliente cliente, Veiculo veiculo, LocalDateTime dataRetirada, LocalDateTime dataPrevistaDevolucao,
                   Seguro seguro, FormaPagamento pagamento, ArrayList<Adicional> adicionais, Cliente condutorExtra)
            throws LocadoraException {
        if (cliente == null || veiculo == null){
            throw new OperacaoInvalidaException("A locação precisa de um cliente e de um carro!");
        }

        int dias = contarDias(dataRetirada, dataPrevistaDevolucao);

        // condutor extra: o adicional e o condutor andam juntos
        ArrayList<Adicional> lista = (adicionais != null) ? new ArrayList<Adicional>(adicionais) : new ArrayList<Adicional>();
        if (condutorExtra != null && !lista.contains(Adicional.CONDUTOR_EXTRA)){
            lista.add(Adicional.CONDUTOR_EXTRA);
        }
        if (condutorExtra == null && lista.contains(Adicional.CONDUTOR_EXTRA)){
            throw new OperacaoInvalidaException("Informe os dados do condutor extra!");
        }

        this.id = id;
        this.cliente = cliente;
        this.condutorExtra = condutorExtra;
        this.veiculo = veiculo;
        this.dataRetirada = dataRetirada;
        this.dataPrevistaDevolucao = dataPrevistaDevolucao;

        verificarCliente();
        verificarCarro(false);

        this.orcamento = new Orcamento(cliente, veiculo, dataRetirada, dias, lista, seguro, pagamento);
        this.situacao = SituacaoLocacao.RESERVADA;
    }

    // cada dia conta 24 horas a partir do horário da retirada (fração de dia conta como um dia)
    public static int contarDias(LocalDateTime retirada, LocalDateTime devolucao) throws DatasInvalidasException {
        if (retirada == null || devolucao == null){
            throw new DatasInvalidasException("Informe as duas datas!");
        }
        if (!devolucao.isAfter(retirada)){
            throw new DatasInvalidasException("A devolução precisa ser depois da retirada!");
        }
        long minutos = Duration.between(retirada, devolucao).toMinutes();
        return (int) Math.ceil(minutos / (24.0 * 60.0));
    }

    public static String data(LocalDateTime data){
        if (data == null){
            return "-";
        }
        return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
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
        if (cliente.getCnh().getValidade().isBefore(dataPrevistaDevolucao.toLocalDate())){
            throw new ClienteNaoHabilitadoException("A CNH vence antes da devolução.");
        }

        // o condutor extra passa pelas mesmas regras de idade e CNH do cliente
        if (condutorExtra != null){
            String motivoCondutor = condutorExtra.motivoNaoPodeDirigir(veiculo);
            if (motivoCondutor != null){
                throw new ClienteNaoHabilitadoException("Condutor extra: " + motivoCondutor);
            }
            if (condutorExtra.getCnh().getValidade().isBefore(dataPrevistaDevolucao.toLocalDate())){
                throw new ClienteNaoHabilitadoException("Condutor extra: a CNH vence antes da devolução.");
            }
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

    // retirada de carro a combustão (sai com o tanque cheio)
    public void retirar(Funcionario funcionario) throws LocadoraException {
        retirar(funcionario, -1);
    }

    // o elétrico precisa sair com pelo menos 80% de carga, registrada na retirada
    public void retirar(Funcionario funcionario, int cargaBateria) throws LocadoraException {
        if (situacao != SituacaoLocacao.RESERVADA){
            throw new OperacaoInvalidaException("Só dá para retirar uma locação reservada!");
        }
        if (funcionario == null){
            throw new OperacaoInvalidaException("Informe o funcionário que fez a vistoria da retirada!");
        }
        if (veiculo instanceof Eletrico){
            if (cargaBateria < 0 || cargaBateria > 100){
                throw new OperacaoInvalidaException("Informe a carga da bateria (0 a 100%) do carro elétrico!");
            }
            if (cargaBateria < Fechamento.CARGA_MINIMA_SAIDA){
                throw new OperacaoInvalidaException("O elétrico precisa sair com pelo menos "
                        + Fechamento.CARGA_MINIMA_SAIDA + "% de carga!");
            }
        } else {
            cargaBateria = -1;
        }

        // confere de novo: o cliente pode ter sido bloqueado e o carro pode ter ido para a oficina
        verificarCliente();
        verificarCarro(true);

        kmRetirada = veiculo.getKm();
        vistoriaRetirada = new Vistoria(funcionario, LocalDateTime.now(), kmRetirada, 0, cargaBateria, null);
        veiculo.setSituacao("Alugado");
        situacao = SituacaoLocacao.ATIVA;
    }

    // se pagarExtrasAgora for false, as cobranças viram débito e o cliente fica bloqueado
    public Fechamento devolver(Funcionario funcionario, LocalDateTime dataReal, int kmFinal, double litrosFaltando,
                               int cargaBateria, ArrayList<Avaria> avarias, boolean pagarExtrasAgora)
            throws LocadoraException {
        if (situacao != SituacaoLocacao.ATIVA){
            throw new OperacaoInvalidaException("Só dá para devolver uma locação ativa!");
        }
        if (funcionario == null){
            throw new OperacaoInvalidaException("Informe o funcionário que fez a vistoria da devolução!");
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
        if (veiculo instanceof Eletrico){
            if (cargaBateria < 0 || cargaBateria > 100){
                throw new OperacaoInvalidaException("Informe a carga da bateria (0 a 100%) na devolução!");
            }
        } else {
            cargaBateria = -1;
        }

        vistoriaDevolucao = new Vistoria(funcionario, dataReal, kmFinal, litrosFaltando, cargaBateria, avarias);

        fechamento = new Fechamento(veiculo, orcamento.getSeguro(), orcamento.getDias(), dataPrevistaDevolucao, dataReal,
                kmFinal - kmRetirada, litrosFaltando, vistoriaRetirada.getCargaBateria(), cargaBateria,
                avarias, orcamento.getTotal(), pagarExtrasAgora);

        dataDevolucaoReal = dataReal;
        veiculo.setKm(kmFinal);
        veiculo.setSituacao("Livre");
        situacao = SituacaoLocacao.FINALIZADA;

        // fidelidade: 1 ponto a cada R$ 10,00 pagos no aluguel (multas e avarias não contam)
        cliente.registrarAluguel(orcamento.getTotal());

        if (fechamento.getTotalExtras() > 0 && !pagarExtrasAgora){
            cliente.registrarDebito(fechamento.getTotalExtras(), "cobranças da locação nº " + id);
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

    // devolver às 10h e outro cliente retirar às 10h pode, então não usa <=
    public boolean cruzaCom(LocalDateTime novaRetirada, LocalDateTime novaDevolucao){
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

    public Cliente getCondutorExtra(){
        return condutorExtra;
    }

    public Veiculo getVeiculo(){
        return veiculo;
    }

    public LocalDateTime getDataRetirada(){
        return dataRetirada;
    }

    public LocalDateTime getDataPrevistaDevolucao(){
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

    // para o contrato, o recibo e o histórico de ações (Partes 4 e 5)
    public Vistoria getVistoriaRetirada(){
        return vistoriaRetirada;
    }

    public Vistoria getVistoriaDevolucao(){
        return vistoriaDevolucao;
    }

    public LocalDateTime getDataDevolucaoReal(){
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
