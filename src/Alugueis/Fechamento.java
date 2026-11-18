package Alugueis;

import Carros.Veiculo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// o que é cobrado a mais na devolução: atraso, km a mais e combustível
public class Fechamento {
    public static final int KM_LIVRE_POR_DIA = 200;
    public static final double VALOR_KM_EXTRA = 0.80;
    public static final double VALOR_LITRO = 7.50;
    public static final double FATOR_MULTA_ATRASO = 1.5; // cada dia de atraso custa 1,5 diária

    private int diasAtraso;
    private double multaAtraso;
    private int kmRodado;
    private int kmPermitido;
    private int kmExtra;
    private double valorKmExtra;
    private double litrosFaltando;
    private double valorCombustivel;
    private double totalExtras;
    private double valorLocacao;
    private double totalGeral;
    private boolean extrasPagos;

    public Fechamento(Veiculo veiculo, int diasContratados, LocalDate dataPrevista, LocalDate dataReal,
                      int kmRodado, double litrosFaltando, double valorLocacao, boolean extrasPagos){
        this.kmRodado = kmRodado;
        this.litrosFaltando = litrosFaltando;
        this.valorLocacao = valorLocacao;
        this.extrasPagos = extrasPagos;

        if (dataReal.isAfter(dataPrevista)){
            diasAtraso = (int) ChronoUnit.DAYS.between(dataPrevista, dataReal);
        }
        multaAtraso = diasAtraso * veiculo.calcularDiaria() * FATOR_MULTA_ATRASO;

        kmPermitido = diasContratados * KM_LIVRE_POR_DIA;
        if (kmRodado > kmPermitido){
            kmExtra = kmRodado - kmPermitido;
        }
        valorKmExtra = kmExtra * VALOR_KM_EXTRA;

        valorCombustivel = litrosFaltando * VALOR_LITRO;

        totalExtras = multaAtraso + valorKmExtra + valorCombustivel;
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
        texto += "Combustível faltando (" + litrosFaltando + " L): " + Orcamento.dinheiro(valorCombustivel) + "\n";
        texto += "TOTAL GERAL: " + Orcamento.dinheiro(totalGeral);
        return texto;
    }
}
