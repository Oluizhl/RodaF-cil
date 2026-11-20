package Alugueis;

public enum Adicional {
    GPS("GPS", 15.00),
    CADEIRINHA("Cadeirinha infantil", 20.00), // até 2 por aluguel (cada uma custa o valor por dia)
    CONDUTOR_EXTRA("Condutor extra", 30.00);

    public static final int MAX_CADEIRINHAS = 2;

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
