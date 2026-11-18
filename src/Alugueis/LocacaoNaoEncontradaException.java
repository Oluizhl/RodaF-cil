package Alugueis;

public class LocacaoNaoEncontradaException extends LocadoraException {
    public LocacaoNaoEncontradaException(int id){
        super("Locação não encontrada!");
    }
}
