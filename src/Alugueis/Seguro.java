package Alugueis;

public abstract class Seguro {
    private String nome;

    public Seguro(String nome){
        this.nome = nome;
    }

    public String getNome(){
        return nome;
    }

    // cada tipo de seguro calcula o valor do seu jeito (polimorfismo)
    public abstract double calcularValor(double diaria, int dias);

    public abstract String getCobertura();
}
