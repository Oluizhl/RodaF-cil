package Alugueis;

public class SemSeguro extends Seguro {
    public SemSeguro(){
        super("Sem seguro");
    }

    @Override
    public double calcularValor(int dias){
        return 0;
    }

    // o cliente paga 100% de qualquer avaria
    @Override
    public double calcularAvaria(Avaria avaria){
        return avaria.getValorConserto();
    }

    @Override
    public String getCobertura(){
        return "Nenhuma, o cliente paga 100% de qualquer avaria.";
    }
}
