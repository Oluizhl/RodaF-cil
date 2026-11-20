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
    public abstract double calcularValor(int dias);

    // quanto o CLIENTE paga por uma avaria, dependendo do que o seguro cobre
    public abstract double calcularAvaria(Avaria avaria);

    public abstract String getCobertura();
}
