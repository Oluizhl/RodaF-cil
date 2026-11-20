package Alugueis;

public class SemSeguro extends Seguro {
    public SemSeguro(){
        super("Sem seguro");
    }

    @Override
    public double calcularValor(double diaria, int dias){
        return 0;
    }

    @Override
    public String getCobertura(){
        return "Nenhuma, o cliente paga qualquer dano.";
    }
}
