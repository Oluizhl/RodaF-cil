package Telas;

import Alugueis.Adicional;
import Alugueis.Avaria;
import Alugueis.Fechamento;
import Alugueis.FormaPagamento;
import Alugueis.Locacao;
import Alugueis.LocadoraException;
import Alugueis.Orcamento;
import Alugueis.PagamentoCartao;
import Alugueis.PagamentoDinheiro;
import Alugueis.PagamentoPix;
import Alugueis.Seguro;
import Alugueis.SeguroBasico;
import Alugueis.SeguroCompleto;
import Alugueis.SemSeguro;
import Alugueis.SimuladorOrcamento;
import Alugueis.SituacaoLocacao;
import Alugueis.TipoAvaria;
import Carros.Eletrico;
import Carros.Veiculo;
import Clientes.Cliente;
import Clientes.Funcionario;
import Relatorios.DocumentosLocacao;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class TelaAlugueis extends Tela {
    private Sistema sistema;
    private TelaCarros telaCarros;
    private TelaClientes telaClientes;

    public TelaAlugueis(Sistema sistema){
        super("Aluguéis");
        this.sistema = sistema;
        this.telaCarros = new TelaCarros(sistema);
        this.telaClientes = new TelaClientes(sistema);
    }

    @Override
    public String[] getOpcoes(){
        return new String[]{
                "Ver carros disponíveis num período",
                "Simular orçamento",
                "Reservar carro",
                "Retirar carro (gera o contrato em PDF)",
                "Devolver carro (gera o recibo em PDF)",
                "Cancelar reserva",
                "Listar aluguéis",
                "Ver detalhes de um aluguel"
        };
    }

    @Override
    public void executar(int opcao){
        try {
            switch (opcao){
                case 1: verDisponiveis(); break;
                case 2: simular(); break;
                case 3: reservar(); break;
                case 4: retirar(); break;
                case 5: devolver(); break;
                case 6: cancelar(); break;
                case 7: listar(); break;
                case 8: detalhes(); break;
            }
        } catch (LocadoraException e){
            System.out.println("ERRO: " + e.getMessage());
        }
    }

    private void verDisponiveis(){
        LocalDateTime retirada = Entrada.lerDataHora("Retirada");
        LocalDateTime devolucao = Entrada.lerDataHora("Devolução");

        ArrayList<Veiculo> disponiveis = new ArrayList<Veiculo>();
        for (Veiculo v : sistema.getLocadora().getFrota()){
            if (sistema.getLocacoes().estaDisponivel(v, retirada, devolucao)){
                disponiveis.add(v);
            }
        }
        TelaCarros.listar(disponiveis);
    }

    private void simular() throws LocadoraException {
        Veiculo veiculo = telaCarros.escolherCarro();
        if (veiculo == null){
            return;
        }
        Cliente cliente = null;
        if (Entrada.lerSimNao("O cliente já é cadastrado? (para o desconto de fidelidade)")){
            cliente = telaClientes.escolherCliente("CPF do cliente");
        }
        LocalDateTime retirada = Entrada.lerDataHora("Retirada");
        LocalDateTime devolucao = Entrada.lerDataHora("Devolução");
        ArrayList<Adicional> adicionais = escolherAdicionais(false);
        Seguro seguro = escolherSeguro();

        System.out.println();
        SimuladorOrcamento.compararPagamentos(cliente, veiculo, retirada, devolucao, seguro, adicionais);
    }

    private void reservar() throws LocadoraException {
        Cliente cliente = telaClientes.escolherCliente("CPF do cliente");
        if (cliente == null){
            return;
        }
        Veiculo veiculo = telaCarros.escolherCarro();
        if (veiculo == null){
            return;
        }
        LocalDateTime retirada = Entrada.lerDataHora("Retirada");
        LocalDateTime devolucao = Entrada.lerDataHora("Devolução");

        ArrayList<Adicional> adicionais = escolherAdicionais(true);
        Cliente condutorExtra = null;
        if (adicionais.contains(Adicional.CONDUTOR_EXTRA)){
            System.out.println("O condutor extra precisa estar cadastrado como cliente.");
            condutorExtra = telaClientes.escolherCliente("CPF do condutor extra");
            if (condutorExtra == null){
                return;
            }
        }
        Seguro seguro = escolherSeguro();
        FormaPagamento pagamento = escolherPagamento();

        // mostra o orçamento antes de confirmar
        Orcamento orcamento = SimuladorOrcamento.simular(cliente, veiculo, retirada, devolucao, seguro, pagamento, adicionais);
        System.out.println("\n--- Orçamento ---");
        System.out.println(orcamento);

        if (!Entrada.lerSimNao("Confirmar a reserva?")){
            System.out.println("Reserva não realizada.");
            return;
        }

        Locacao locacao = sistema.getLocacoes().reservar(cliente, veiculo, retirada, devolucao, seguro, pagamento,
                adicionais, condutorExtra);
        System.out.println("Reserva feita! Número da locação: " + locacao.getId());
        sistema.registrarAcao("Sistema", "RESERVA", locacao.toString());
    }

    private void retirar() throws LocadoraException {
        int id = Entrada.lerInteiro("Número da locação");
        Locacao locacao = sistema.getLocacoes().buscarPorId(id);
        System.out.println(locacao);
        if (locacao.getSituacao() != SituacaoLocacao.RESERVADA){
            System.out.println("ERRO: Só dá para retirar uma locação reservada!");
            return;
        }

        Funcionario funcionario = escolherFuncionario();
        if (funcionario == null){
            return;
        }

        if (locacao.getVeiculo() instanceof Eletrico){
            int carga = Entrada.lerInteiro("Carga da bateria na saída (%)", 0, 100);
            sistema.getLocacoes().retirar(id, funcionario, carga);
        } else {
            sistema.getLocacoes().retirar(id, funcionario);
        }

        System.out.println("Carro retirado! Boa viagem.");
        sistema.registrarAcao(funcionario.getNome(), "RETIRADA", locacao.toString());
        gerarContrato(locacao);
    }

    private void devolver() throws LocadoraException {
        int id = Entrada.lerInteiro("Número da locação");
        Locacao locacao = sistema.getLocacoes().buscarPorId(id);
        System.out.println(locacao);
        if (locacao.getSituacao() != SituacaoLocacao.ATIVA){
            System.out.println("ERRO: Só dá para devolver uma locação ativa (carro já retirado)!");
            return;
        }

        Funcionario funcionario = escolherFuncionario();
        if (funcionario == null){
            return;
        }

        LocalDateTime dataReal = LocalDateTime.now();
        if (!Entrada.lerSimNao("A devolução é agora?")){
            dataReal = Entrada.lerDataHora("Data da devolução");
        }

        System.out.println("Km na retirada: " + locacao.getKmRetirada());
        int kmFinal = Entrada.lerInteiro("Km atual do carro");

        double litros = 0;
        int carga = -1;
        if (locacao.getVeiculo() instanceof Eletrico){
            carga = Entrada.lerInteiro("Carga da bateria na devolução (%)", 0, 100);
        } else {
            litros = Entrada.lerDecimal("Litros faltando para encher o tanque (0 se cheio)");
        }

        ArrayList<Avaria> avarias = lerAvarias();
        boolean pagarAgora = Entrada.lerSimNao("O cliente vai pagar as cobranças extras agora?");

        Fechamento fechamento = sistema.getLocacoes().devolver(id, funcionario, dataReal, kmFinal, litros, carga,
                avarias, pagarAgora);

        System.out.println("\n--- Fechamento ---");
        System.out.println(fechamento);
        sistema.registrarAcao(funcionario.getNome(), "DEVOLUÇÃO", locacao + " | total " + Orcamento.dinheiro(fechamento.getTotalGeral()));
        gerarRecibo(locacao);
    }

    private void cancelar() throws LocadoraException {
        int id = Entrada.lerInteiro("Número da locação");
        Locacao locacao = sistema.getLocacoes().buscarPorId(id);
        System.out.println(locacao);

        if (Entrada.lerSimNao("Cancelar essa reserva?")){
            sistema.getLocacoes().cancelar(id);
            System.out.println("Reserva cancelada.");
            sistema.registrarAcao("Sistema", "CANCELAMENTO", locacao.toString());
        }
    }

    private void listar(){
        System.out.println("Mostrar: 1 - Todos   2 - Reservados   3 - Ativos   4 - Finalizados   5 - Cancelados");
        int filtro = Entrada.lerInteiro("Opção", 1, 5);

        if (filtro == 1){
            sistema.getLocacoes().listar();
            return;
        }

        SituacaoLocacao[] situacoes = {SituacaoLocacao.RESERVADA, SituacaoLocacao.ATIVA,
                SituacaoLocacao.FINALIZADA, SituacaoLocacao.CANCELADA};
        ArrayList<Locacao> lista = sistema.getLocacoes().buscarPorSituacao(situacoes[filtro - 2]);
        if (lista.isEmpty()){
            System.out.println("Nenhuma locação encontrada.");
        }
        for (Locacao l : lista){
            System.out.println(l);
        }
    }

    private void detalhes() throws LocadoraException {
        int id = Entrada.lerInteiro("Número da locação");
        Locacao locacao = sistema.getLocacoes().buscarPorId(id);

        System.out.println(locacao);
        if (locacao.getCondutorExtra() != null){
            System.out.println("Condutor extra: " + locacao.getCondutorExtra().getNome());
        }
        System.out.println("\n--- Orçamento ---");
        System.out.println(locacao.getOrcamento());

        if (locacao.getFechamento() != null){
            System.out.println("\n--- Fechamento ---");
            System.out.println(locacao.getFechamento());
        }

        // permite gerar o PDF de novo (ex.: o cliente perdeu o contrato)
        if (locacao.getVistoriaRetirada() != null && Entrada.lerSimNao("Gerar o contrato em PDF de novo?")){
            gerarContrato(locacao);
        }
        if (locacao.getFechamento() != null && Entrada.lerSimNao("Gerar o recibo em PDF de novo?")){
            gerarRecibo(locacao);
        }
    }

    private ArrayList<Adicional> escolherAdicionais(boolean perguntarCondutor){
        ArrayList<Adicional> adicionais = new ArrayList<Adicional>();

        if (Entrada.lerSimNao("Adicionar GPS (" + Orcamento.dinheiro(Adicional.GPS.getValorDiario()) + "/dia)?")){
            adicionais.add(Adicional.GPS);
        }
        int cadeirinhas = Entrada.lerInteiro("Quantas cadeirinhas infantis ("
                + Orcamento.dinheiro(Adicional.CADEIRINHA.getValorDiario()) + "/dia cada)?", 0, Adicional.MAX_CADEIRINHAS);
        for (int i = 0; i < cadeirinhas; i++){
            adicionais.add(Adicional.CADEIRINHA);
        }
        if (perguntarCondutor && Entrada.lerSimNao("Adicionar condutor extra ("
                + Orcamento.dinheiro(Adicional.CONDUTOR_EXTRA.getValorDiario()) + "/dia)?")){
            adicionais.add(Adicional.CONDUTOR_EXTRA);
        }
        return adicionais;
    }

    private Seguro escolherSeguro(){
        System.out.println("Seguro:");
        System.out.println(" 1 - Sem seguro");
        System.out.println(" 2 - Básico (" + Orcamento.dinheiro(SeguroBasico.VALOR_POR_DIA) + "/dia, franquia "
                + Orcamento.dinheiro(SeguroBasico.FRANQUIA) + ")");
        System.out.println(" 3 - Completo (" + Orcamento.dinheiro(SeguroCompleto.VALOR_POR_DIA) + "/dia, sem franquia)");
        int opcao = Entrada.lerInteiro("Opção", 1, 3);

        if (opcao == 2){
            return new SeguroBasico();
        } else if (opcao == 3){
            return new SeguroCompleto();
        }
        return new SemSeguro();
    }

    private FormaPagamento escolherPagamento() throws LocadoraException {
        System.out.println("Forma de pagamento: 1 - Pix (5% de desconto)   2 - Dinheiro   3 - Cartão");
        int opcao = Entrada.lerInteiro("Opção", 1, 3);

        if (opcao == 1){
            return new PagamentoPix();
        } else if (opcao == 2){
            return new PagamentoDinheiro();
        }
        System.out.println("Até " + PagamentoCartao.MAX_PARCELAS_SEM_JUROS + "x sem juros; de 4x a "
                + PagamentoCartao.MAX_PARCELAS + "x com acréscimo de 5%. Parcela mínima de R$ 100,00.");
        int parcelas = Entrada.lerInteiro("Parcelas", 1, PagamentoCartao.MAX_PARCELAS);
        return new PagamentoCartao(parcelas);
    }

    private ArrayList<Avaria> lerAvarias() throws LocadoraException {
        ArrayList<Avaria> avarias = new ArrayList<Avaria>();
        TipoAvaria[] tipos = TipoAvaria.values();

        while (Entrada.lerSimNao(avarias.isEmpty() ? "O carro voltou com alguma avaria?" : "Tem outra avaria?")){
            for (int i = 0; i < tipos.length; i++){
                System.out.println(" " + (i + 1) + " - " + tipos[i].getNome());
            }
            TipoAvaria tipo = tipos[Entrada.lerInteiro("Tipo da avaria", 1, tipos.length) - 1];
            String descricao = Entrada.lerTexto("Descrição");
            double valor = Entrada.lerDecimal("Valor do conserto (R$)");
            if (valor <= 0){
                System.out.println("ERRO: O valor do conserto deve ser maior que zero! Avaria não registrada.");
                continue;
            }
            avarias.add(new Avaria(tipo, descricao, valor));
        }
        return avarias;
    }

    private Funcionario escolherFuncionario(){
        String matricula = Entrada.lerTexto("Matrícula do funcionário que fez a vistoria");
        Funcionario funcionario = sistema.getFuncionarios().buscarPorMatricula(matricula);
        if (funcionario == null){
            System.out.println("ERRO: Funcionário com essa matrícula não encontrado!");
        }
        return funcionario;
    }


    private void gerarContrato(Locacao locacao){
        try {
            Path arquivo = DocumentosLocacao.gerarContrato(locacao, pastaPdf());
            System.out.println("Contrato gerado: " + arquivo.toAbsolutePath());
        } catch (IOException e){
            System.out.println("ERRO: Não foi possível gerar o contrato: " + e.getMessage());
        }
    }

    private void gerarRecibo(Locacao locacao){
        try {
            Path arquivo = DocumentosLocacao.gerarRecibo(locacao, pastaPdf());
            System.out.println("Recibo gerado: " + arquivo.toAbsolutePath());
        } catch (IOException e){
            System.out.println("ERRO: Não foi possível gerar o recibo: " + e.getMessage());
        }
    }

    private Path pastaPdf(){
        return sistema.getPastaDados().resolve("pdf");
    }
}
