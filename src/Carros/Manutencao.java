package Carros;

public class Manutencao {
    private String data;
    private String servico;
    private double custo;

    public Manutencao (String data, String servico, double custo){
        this.data = data;
        this.servico = servico;
        setCusto(custo);
    }

    public String getData(){
        return data;
    }

    public String getServico() {
        return servico;
    }

    public double getCusto(){
        return custo;
    }

    public void setCusto(double custo){
        if (custo >= 0){
            this.custo = custo;
        } else {
            System.out.println("ERRO: O custo não pode ser menor do que zero!");
        }
    }
}
