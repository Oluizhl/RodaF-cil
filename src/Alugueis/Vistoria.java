package Alugueis;

import Clientes.Funcionario;

import java.time.LocalDateTime;
import java.util.ArrayList;

// vistoria feita por um funcionário na retirada ou na devolução do carro
public class Vistoria {
    private Funcionario funcionario;
    private LocalDateTime data;
    private int km;
    private double litrosFaltando;      // carros a combustão (0 na retirada: tanque cheio)
    private int cargaBateria;           // carros elétricos, em % (-1 se não for elétrico)
    private ArrayList<Avaria> avarias;

    public Vistoria(Funcionario funcionario, LocalDateTime data, int km, double litrosFaltando,
                    int cargaBateria, ArrayList<Avaria> avarias) throws LocadoraException {
        if (funcionario == null){
            throw new OperacaoInvalidaException("Informe o funcionário que fez a vistoria!");
        }
        this.funcionario = funcionario;
        this.data = data;
        this.km = km;
        this.litrosFaltando = litrosFaltando;
        this.cargaBateria = cargaBateria;
        this.avarias = (avarias != null) ? new ArrayList<Avaria>(avarias) : new ArrayList<Avaria>();
    }

    public Funcionario getFuncionario(){
        return funcionario;
    }

    public LocalDateTime getData(){
        return data;
    }

    public int getKm(){
        return km;
    }

    public double getLitrosFaltando(){
        return litrosFaltando;
    }

    public int getCargaBateria(){
        return cargaBateria;
    }

    public ArrayList<Avaria> getAvarias(){
        return new ArrayList<Avaria>(avarias);
    }
}
