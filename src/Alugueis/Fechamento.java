package Alugueis;

import Carros.Eletrico;
import Carros.Veiculo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

// o que é cobrado a mais na devolução: atraso, km a mais, combustível ou bateria e avarias
// essas cobranças NÃO recebem nenhum desconto e são pagas à vista (Pix, cartão à vista ou dinheiro)
public class Fechamento {
    public static final int TOLERANCIA_ATRASO_MINUTOS = 60;
    public static final double MULTA_ATRASO = 0.20;     // 1 diária + 20% por dia (ou fração) de atraso
    public static final double VALOR_KM_EXTRA = 1.00;
    public static final double VALOR_LITRO = 7.50;
    public static final double TAXA_COMBUSTIVEL = 30.00; // se faltar qualquer quantidade
    public static final double VALOR_BLOCO_BATERIA = 25.00; // a cada 10 pontos percentuais (ou fração)
    public static final int CARGA_MINIMA_SAIDA = 80;
    public static final int CARGA_BAIXA = 10;
    public static final double TAXA_RECARGA_URGENTE = 80.00;

    private int diasAtraso;
    private double multaAtraso;
    private int kmRodado;
    private int kmPermitido;
    private int kmExtra;
    private double valorKmExtra;
    private double litrosFaltando;
    private double valorCombustivel;
    private int cargaSaida;
    private int cargaDevolucao;
    private double valorBateria;
    private ArrayList<Avaria> avarias;
    private double valorAvarias;        // quanto o cliente paga, já considerando o seguro
    private double totalExtras;
    private double valorLocacao;
    private double totalGeral;
    private boolean extrasPagos;

    public Fechamento(Veiculo veiculo, Seguro seguro, int diasContratados, LocalDateTime dataPrevista, LocalDateTime dataReal,
                      int kmRodado, double litrosFaltando, int cargaSaida, int cargaDevolucao,
                      ArrayList<Avaria> avarias, double valorLocacao, boolean extrasPagos){
        this.kmRodado = kmRodado;
        this.litrosFaltando = litrosFaltando;
        this.cargaSaida = cargaSaida;
        this.cargaDevolucao = cargaDevolucao;
        this.avarias = (avarias != null) ? new ArrayList<Avaria>(avarias) : new ArrayList<Avaria>();
        this.valorLocacao = valorLocacao;
        this.extrasPagos = extrasPagos;

        // atraso: tolerância de 1 hora; depois disso, cada dia ou fração de atraso = 1 diária + 20%
        Duration atraso = Duration.between(dataPrevista, dataReal);
        if (atraso.toMinutes() > TOLERANCIA_ATRASO_MINUTOS){
            diasAtraso = (int) Math.ceil(atraso.toMinutes() / (24.0 * 60.0));
        }
        multaAtraso = diasAtraso * veiculo.getValorDiaria() * (1 + MULTA_ATRASO);

        // km: limite por dia de aluguel (100 km; Utilitário 150 km), R$ 1,00 por km acima
        kmPermitido = diasContratados * veiculo.getLimiteKmPorDia();
        if (kmRodado > kmPermitido){
            kmExtra = kmRodado - kmPermitido;
        }
        valorKmExtra = kmExtra * VALOR_KM_EXTRA;

        if (veiculo instanceof Eletrico){
            // bateria: cada 10 pontos percentuais (ou fração) abaixo da carga de saída
            int queda = cargaSaida - cargaDevolucao;
            if (queda > 0){
                valorBateria = Math.ceil(queda / 10.0) * VALOR_BLOCO_BATERIA;
            }
            if (cargaDevolucao < CARGA_BAIXA){
                valorBateria += TAXA_RECARGA_URGENTE;
            }
        } else {
            // combustível: R$ 7,50 por litro + R$ 30,00 de taxa se faltar qualquer quantidade
            if (litrosFaltando > 0){
                valorCombustivel = litrosFaltando * VALOR_LITRO + TAXA_COMBUSTIVEL;
            }
        }

        // avarias: o seguro decide quanto o cliente paga
        for (Avaria a : this.avarias){
            valorAvarias += seguro.calcularAvaria(a);
        }

        totalExtras = multaAtraso + valorKmExtra + valorCombustivel + valorBateria + valorAvarias;
        totalGeral = valorLocacao + totalExtras;
    }

    public int getDiasAtraso(){
        return diasAtraso;
    }

    public double getMultaAtraso(){
        return multaAtraso;
    }

    public int getKmRodado(){
        return kmRodado;
    }

    public int getKmPermitido(){
        return kmPermitido;
    }

    public int getKmExtra(){
        return kmExtra;
    }

    public double getValorKmExtra(){
        return valorKmExtra;
    }

    public double getLitrosFaltando(){
        return litrosFaltando;
    }

    public double getValorCombustivel(){
        return valorCombustivel;
    }

    public int getCargaSaida(){
        return cargaSaida;
    }

    public int getCargaDevolucao(){
        return cargaDevolucao;
    }

    public double getValorBateria(){
        return valorBateria;
    }

    public ArrayList<Avaria> getAvarias(){
        return new ArrayList<Avaria>(avarias);
    }

    public double getValorAvarias(){
        return valorAvarias;
    }

    public double getTotalExtras(){
        return totalExtras;
    }

    public double getValorLocacao(){
        return valorLocacao;
    }

    public double getTotalGeral(){
        return totalGeral;
    }

    public boolean isExtrasPagos(){
        return extrasPagos;
    }

    @Override
    public String toString(){
        String texto = "Valor da locação: " + Orcamento.dinheiro(valorLocacao) + "\n";
        texto += "Multa por atraso (" + diasAtraso + " dia(s)): " + Orcamento.dinheiro(multaAtraso) + "\n";
        texto += "Km a mais (" + kmExtra + " km): " + Orcamento.dinheiro(valorKmExtra) + "\n";
        if (cargaSaida >= 0){
            texto += "Bateria (saiu com " + cargaSaida + "%, voltou com " + cargaDevolucao + "%): " + Orcamento.dinheiro(valorBateria) + "\n";
        } else {
            texto += "Combustível faltando (" + litrosFaltando + " L): " + Orcamento.dinheiro(valorCombustivel) + "\n";
        }
        for (Avaria a : avarias){
            texto += "Avaria: " + a + "\n";
        }
        texto += "Avarias (parte do cliente): " + Orcamento.dinheiro(valorAvarias) + "\n";
        texto += "TOTAL GERAL: " + Orcamento.dinheiro(totalGeral);
        return texto;
    }
}
