package Alugueis;

public enum TipoAvaria {
    LATARIA_MECANICA("Lataria/mecânica"),
    VIDRO("Vidro"),
    PNEU("Pneu"),
    INTERIOR("Interior");

    private final String nome;

    TipoAvaria(String nome){
        this.nome = nome;
    }

    public String getNome(){
        return nome;
    }
}
