package Carros;

import java.util.ArrayList;

public abstract class Veiculo {
    private String placa;
    private String marca;
    private String modelo;
    private int ano;
    private int km;
    private String situacao; // livre, alugado ou em manutenção
    private double valorDiaria;
    private int kmUltimaRevisao;
    private ArrayList<Manutencao> historico;

    public Veiculo(String placa, String marca, String modelo, int ano, int km, double valorDiaria){
        setPlaca(placa);
        setMarca(marca);
        setModelo(modelo);
        setAno(ano);
        setKm(km);
        this.situacao = "Livre";
        setValorDiaria(valorDiaria);
        this.kmUltimaRevisao = this.km;
        historico = new ArrayList<Manutencao>();
    }

    public String getPlaca(){
        return placa;
    }

    public void setPlaca(String placa){
        if (placa.matches("^[A-Z]{3}[0-9][A-Z][0-9]{2}$")){
            this.placa = placa;
        } else {
            System.out.println("ERRO: Placa fora do padrão Mercosul!");
        }
    }

    public String getMarca(){
        return marca;
    }

    public void setMarca(String marca){
        this.marca = marca;
    }

    public String getModelo(){
        return modelo;
    }

    public void setModelo(String modelo){
        this.modelo = modelo;
    }

    public int getAno(){
        return ano;
    }

    public void setAno(int ano){
        if (1920 < ano && ano < 2027){
            this.ano = ano;
        } else {
            System.out.println("ERRO: O ano digitado é inválido!");
        }
    }

    public int getKm(){
        return km;
    }

    public void setKm(int km){
        if (km > 0) {
            this.km = km;
        } else {
            System.out.println("ERRO: A quilometragem do carro deve ser um valor positivo.");
        }
    }

    public String getSituacao(){
        return situacao;
    }

    public void setSituacao(String situacao){
        if (situacao.equals("Livre") || situacao.equals("Alugado") || situacao.equals("Manutenção")){
            this.situacao = situacao;
        } else{
            System.out.println("ERRO: Situação inválida!");
        }
    }

    public double getValorDiaria() {
        return valorDiaria;
    }

    public void setValorDiaria(double valorDiaria){
        this.valorDiaria = valorDiaria;
    }

    public int getKmUltimaRevisao(){
        return kmUltimaRevisao;
    }

    public boolean precisaRevisao(){
        if (km - kmUltimaRevisao >= 10000){
            return true;
        } else {
            return false;
        }
    }

    public void registrarManutencao(Manutencao m){
        historico.add(m);
    }
    
    public void mostrarHistorico(){  // mostrarHistorico() e mostrarHistorico(String placa) têm o mesmo nome,
        if (historico.isEmpty()){    // porém, possuem parâmetros diferentes, configurando uma SOBRECARGA!
            System.out.println("Nenhuma manutenção registrada para esse veículo.");
        } else {
            for (Manutencao m : historico){
                System.out.println(m.getData() + " | " + m.getServico() + " | " + m.getCusto());
            }
        }
    }

    public void realizarRevisao(String data, double custo){
        registrarManutencao(new Manutencao(data, "Revisão", custo));

        this.kmUltimaRevisao = this.km;

        if (situacao.equals("Manutenção")){
            setSituacao("Livre");
        }
    }

    public abstract double calcularDiaria();
}