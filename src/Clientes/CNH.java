package Clientes;

import java.time.LocalDate;
import java.time.Period;

public class CNH {
    private String numero;
    private String categoria; // A, B, C, D, E, AB, AC, AD ou AE
    private LocalDate primeiraHabilitacao; // usada na regra "CNH há 2 anos"
    private LocalDate validade;

    public CNH(String numero, String categoria, LocalDate primeiraHabilitacao, LocalDate validade){
        setNumero(numero);
        setCategoria(categoria);
        setPrimeiraHabilitacao(primeiraHabilitacao);
        setValidade(validade);
    }

    public String getNumero(){
        return numero;
    }

    public void setNumero(String numero){
        String digitos = Pessoa.apenasDigitos(numero);
        if (digitos.length() == 11){
            this.numero = digitos;
        } else {
            System.out.println("ERRO: O número da CNH deve ter 11 dígitos!");
        }
    }

    public String getCategoria(){
        return categoria;
    }

    public void setCategoria(String categoria){
        if (categoria != null && categoria.toUpperCase().matches("^(A|B|C|D|E|AB|AC|AD|AE)$")){
            this.categoria = categoria.toUpperCase();
        } else {
            System.out.println("ERRO: Categoria da CNH inválida!");
        }
    }

    public LocalDate getPrimeiraHabilitacao(){
        return primeiraHabilitacao;
    }

    public void setPrimeiraHabilitacao(LocalDate primeiraHabilitacao){
        if (primeiraHabilitacao != null && !primeiraHabilitacao.isAfter(LocalDate.now())){
            this.primeiraHabilitacao = primeiraHabilitacao;
        } else {
            System.out.println("ERRO: Data da primeira habilitação inválida!");
        }
    }

    public LocalDate getValidade(){
        return validade;
    }

    public void setValidade(LocalDate validade){
        if (validade != null){
            this.validade = validade;
        } else {
            System.out.println("ERRO: A validade da CNH não pode ficar vazia!");
        }
    }

    public boolean isVencida(){
        return validade == null || validade.isBefore(LocalDate.now());
    }

    // anos completos desde a primeira habilitação
    public int anosDeHabilitacao(){
        if (primeiraHabilitacao == null){
            return 0;
        }
        return Period.between(primeiraHabilitacao, LocalDate.now()).getYears();
    }

    // categoria só A (moto) não dirige carro
    public boolean permiteCarro(){
        return categoria != null && !categoria.equals("A");
    }
}
