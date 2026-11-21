package Relatorios;

import Alugueis.Avaria;
import Alugueis.Fechamento;
import Alugueis.Locacao;
import Alugueis.Orcamento;
import Carros.Eletrico;
import Carros.Veiculo;
import Clientes.Cliente;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DocumentosLocacao {

    public static Path gerarContrato(Locacao locacao, Path pasta) throws IOException {
        Orcamento o = locacao.getOrcamento();
        Veiculo v = locacao.getVeiculo();
        PdfSimples pdf = new PdfSimples();

        pdf.titulo("RODA FÁCIL - CONTRATO DE LOCAÇÃO Nº " + locacao.getId());
        pdf.linha("Emitido em: " + Locacao.data(LocalDateTime.now()));

        pdf.secao("LOCATÁRIO");
        dadosCliente(pdf, locacao.getCliente());
        if (locacao.getCondutorExtra() != null){
            pdf.secao("CONDUTOR EXTRA");
            dadosCliente(pdf, locacao.getCondutorExtra());
        }

        pdf.secao("VEÍCULO");
        dadosVeiculo(pdf, v);
        pdf.linha("Km na retirada: " + locacao.getKmRetirada());
        if (v instanceof Eletrico){
            pdf.linha("Carga da bateria na retirada: " + locacao.getVistoriaRetirada().getCargaBateria() + "%");
        } else {
            pdf.linha("Combustível na retirada: tanque cheio");
        }

        pdf.secao("PERÍODO");
        pdf.linha("Retirada: " + Locacao.data(locacao.getDataRetirada()));
        pdf.linha("Devolução prevista: " + Locacao.data(locacao.getDataPrevistaDevolucao()));
        pdf.linha("Dias contratados: " + o.getDias() + " (cada dia conta 24 horas a partir da retirada)");
        pdf.linha("Km permitido: " + (o.getDias() * v.getLimiteKmPorDia()) + " km (" + v.getLimiteKmPorDia() + " km por dia)");

        pdf.secao("VALORES");
        valoresOrcamento(pdf, o);
        pdf.linha("Cobertura do seguro: " + o.getSeguro().getCobertura());

        pdf.secao("COBRANÇAS NA DEVOLUÇÃO (se houver)");
        pdf.linha("- Atraso: tolerância de " + Fechamento.TOLERANCIA_ATRASO_MINUTOS + " minutos; depois, cada dia ou fração custa 1 diária + "
                + (int) (Fechamento.MULTA_ATRASO * 100) + "%.");
        pdf.linha("- Km a mais: " + Orcamento.dinheiro(Fechamento.VALOR_KM_EXTRA) + " por km acima do permitido.");
        if (v instanceof Eletrico){
            pdf.linha("- Bateria: " + Orcamento.dinheiro(Fechamento.VALOR_BLOCO_BATERIA) + " a cada 10% abaixo da carga de saída; abaixo de "
                    + Fechamento.CARGA_BAIXA + "% soma " + Orcamento.dinheiro(Fechamento.TAXA_RECARGA_URGENTE) + " de recarga urgente.");
        } else {
            pdf.linha("- Combustível: " + Orcamento.dinheiro(Fechamento.VALOR_LITRO) + " por litro faltando + taxa de "
                    + Orcamento.dinheiro(Fechamento.TAXA_COMBUSTIVEL) + ".");
        }
        pdf.linha("- Avarias: cobradas conforme o seguro contratado. As cobranças extras não recebem desconto.");

        pdf.secao("VISTORIA DA RETIRADA");
        pdf.linha("Funcionário: " + locacao.getVistoriaRetirada().getFuncionario().getNome()
                + " (matrícula " + locacao.getVistoriaRetirada().getFuncionario().getMatricula() + ")");
        pdf.linha("Data: " + Locacao.data(locacao.getVistoriaRetirada().getData()));

        assinaturas(pdf, locacao.getCliente().getNome(), locacao.getVistoriaRetirada().getFuncionario().getNome());

        Path arquivo = pasta.resolve("contrato-locacao-" + locacao.getId() + ".pdf");
        pdf.salvar(arquivo);
        return arquivo;
    }

    public static Path gerarRecibo(Locacao locacao, Path pasta) throws IOException {
        Orcamento o = locacao.getOrcamento();
        Fechamento f = locacao.getFechamento();
        Veiculo v = locacao.getVeiculo();
        PdfSimples pdf = new PdfSimples();

        pdf.titulo("RODA FÁCIL - RECIBO DE DEVOLUÇÃO Nº " + locacao.getId());
        pdf.linha("Emitido em: " + Locacao.data(LocalDateTime.now()));

        pdf.secao("CLIENTE");
        dadosCliente(pdf, locacao.getCliente());

        pdf.secao("VEÍCULO");
        dadosVeiculo(pdf, v);

        pdf.secao("PERÍODO");
        pdf.linha("Retirada: " + Locacao.data(locacao.getDataRetirada()));
        pdf.linha("Devolução prevista: " + Locacao.data(locacao.getDataPrevistaDevolucao()));
        pdf.linha("Devolução real: " + Locacao.data(locacao.getDataDevolucaoReal()));
        pdf.linha("Vistoria da devolução: " + locacao.getVistoriaDevolucao().getFuncionario().getNome()
                + " (matrícula " + locacao.getVistoriaDevolucao().getFuncionario().getMatricula() + ")");

        pdf.secao("VALOR DA LOCAÇÃO");
        valoresOrcamento(pdf, o);

        pdf.secao("COBRANÇAS DA DEVOLUÇÃO");
        pdf.linha("Atraso: " + f.getDiasAtraso() + " dia(s) = " + Orcamento.dinheiro(f.getMultaAtraso()));
        pdf.linha("Km rodado: " + f.getKmRodado() + " km | permitido: " + f.getKmPermitido() + " km | a mais: "
                + f.getKmExtra() + " km = " + Orcamento.dinheiro(f.getValorKmExtra()));
        if (v instanceof Eletrico){
            pdf.linha("Bateria: saiu com " + f.getCargaSaida() + "%, voltou com " + f.getCargaDevolucao() + "% = "
                    + Orcamento.dinheiro(f.getValorBateria()));
        } else {
            pdf.linha("Combustível faltando: " + f.getLitrosFaltando() + " L = " + Orcamento.dinheiro(f.getValorCombustivel()));
        }
        if (f.getAvarias().isEmpty()){
            pdf.linha("Avarias: nenhuma");
        }
        for (Avaria a : f.getAvarias()){
            pdf.linha("Avaria: " + a.getTipo().getNome() + " - " + a.getDescricao() + " | conserto "
                    + Orcamento.dinheiro(a.getValorConserto()) + " | cliente paga "
                    + Orcamento.dinheiro(o.getSeguro().calcularAvaria(a)));
        }
        pdf.linha("Total das avarias (parte do cliente, " + o.getSeguro().getNome() + "): " + Orcamento.dinheiro(f.getValorAvarias()));
        pdf.negrito("Total das cobranças extras: " + Orcamento.dinheiro(f.getTotalExtras()));
        if (f.getTotalExtras() > 0){
            pdf.linha(f.isExtrasPagos() ? "Situação: extras pagos na devolução."
                    : "Situação: extras NÃO pagos - o cliente fica bloqueado até quitar o débito.");
        }

        pdf.secao("RESUMO");
        pdf.linha("Locação: " + Orcamento.dinheiro(f.getValorLocacao()));
        pdf.linha("Extras: " + Orcamento.dinheiro(f.getTotalExtras()));
        pdf.negrito("TOTAL GERAL: " + Orcamento.dinheiro(f.getTotalGeral()));

        pdf.secao("FIDELIDADE");
        Cliente c = locacao.getCliente();
        pdf.linha("Pontos acumulados: " + c.getPontos() + " | Nível: " + c.getNivel().getNome());

        assinaturas(pdf, c.getNome(), locacao.getVistoriaDevolucao().getFuncionario().getNome());

        Path arquivo = pasta.resolve("recibo-locacao-" + locacao.getId() + ".pdf");
        pdf.salvar(arquivo);
        return arquivo;
    }

    // ---------- partes repetidas nos dois documentos ----------

    private static void dadosCliente(PdfSimples pdf, Cliente c){
        pdf.linha("Nome: " + c.getNome() + " | CPF: " + c.getCpfFormatado());
        pdf.linha("Telefone: " + c.getTelefone() + " | E-mail: " + c.getEmail());
        pdf.linha("CNH: " + c.getCnh().getNumero() + " | Categoria " + c.getCnh().getCategoria()
                + " | Validade: " + c.getCnh().getValidade().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private static void dadosVeiculo(PdfSimples pdf, Veiculo v){
        pdf.linha(v.getMarca() + " " + v.getModelo() + " (" + v.getAno() + ") | Placa: " + v.getPlaca()
                + " | Tipo: " + v.getClass().getSimpleName());
    }

    // usa o toString() do Orcamento da Parte 3, que já mostra cada valor numa linha
    private static void valoresOrcamento(PdfSimples pdf, Orcamento o){
        for (String linha : o.toString().split("\n")){
            if (linha.startsWith("TOTAL")){
                pdf.negrito(linha);
            } else {
                pdf.linha(linha);
            }
        }
    }

    private static void assinaturas(PdfSimples pdf, String cliente, String funcionario){
        pdf.espaco();
        pdf.espaco();
        pdf.linha("________________________________________");
        pdf.linha("Cliente: " + cliente);
        pdf.espaco();
        pdf.espaco();
        pdf.linha("________________________________________");
        pdf.linha("Roda Fácil: " + funcionario);
    }
}
