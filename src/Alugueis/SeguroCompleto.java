package Alugueis;

public class SeguroCompleto extends Seguro {
    public static final double VALOR_POR_DIA = 70.00;

    public SeguroCompleto(){
        super("Seguro completo");
    }

    @Override
    public double calcularValor(int dias){
        return VALOR_POR_DIA * dias;
    }

    // cobre tudo, sem franquia
    @Override
    public double calcularAvaria(Avaria avaria){
        return 0;
    }

    @Override
    public String getCobertura(){
        return "Lataria, mecânica, vidros, pneus e interior, sem franquia.";
    }
}
