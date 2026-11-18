package Alugueis;

public enum SituacaoLocacao {
    RESERVADA("Reservada"),
    ATIVA("Ativa"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada");

    private final String nome;

    SituacaoLocacao(String nome){
        this.nome = nome;
    }

    public String getNome(){
        return nome;
    }
}
