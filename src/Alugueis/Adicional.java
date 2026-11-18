package Alugueis;

public enum Adicional {
    GPS("GPS", 15.00),
    CADEIRINHA("Cadeirinha", 12.00),
    CONDUTOR_EXTRA("Condutor extra", 20.00);

    private final String nome;
    private final double valorDiario;

    Adicional(String nome, double valorDiario){
        this.nome = nome;
        this.valorDiario = valorDiario;
    }

    public String getNome(){
        return nome;
    }

    public double getValorDiario(){
        return valorDiario;
    }
}
