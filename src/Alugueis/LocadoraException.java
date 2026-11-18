package Alugueis;

// todos os erros da Parte 3 herdam desta classe
public class LocadoraException extends Exception {
    public LocadoraException(String mensagem){
        super(mensagem);
    }
}
