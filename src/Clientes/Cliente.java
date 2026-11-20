package Clientes;

import Carros.Eletrico;
import Carros.SUV;
import Carros.Veiculo;

import java.time.LocalDate;
import java.time.Period;

public class Cliente extends Pessoa {
    public static final double REAIS_POR_PONTO = 10.00; // 1 ponto a cada R$ 10,00 pagos no aluguel

    private LocalDate dataNascimento;
    private CNH cnh;
    private int pontos;
    private double debito; // multa ou avaria que o cliente ainda não pagou
    private String motivoDebito;

    public Cliente(String nome, String cpf, String telefone, String email, LocalDate dataNascimento, CNH cnh){
        super(nome, cpf, telefone, email);
        setDataNascimento(dataNascimento);
        this.cnh = cnh;
        this.pontos = 0;
        this.debito = 0;
        this.motivoDebito = "";
    }

    public LocalDate getDataNascimento(){
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento){
        if (dataNascimento == null || dataNascimento.isAfter(LocalDate.now())){
            System.out.println("ERRO: Data de nascimento inválida!");
        } else if (Period.between(dataNascimento, LocalDate.now()).getYears() < 18){
            System.out.println("ERRO: O cliente precisa ter 18 anos ou mais!");
        } else {
            this.dataNascimento = dataNascimento;
        }
    }

    public int getIdade(){
        if (dataNascimento == null){
            return 0;
        }
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    public CNH getCnh(){
        return cnh;
    }

    public void setCnh(CNH cnh){
        if (cnh == null){
            System.out.println("ERRO: CNH não informada!");
        } else if (cnh.isVencida()){
            System.out.println("ERRO: CNH vencida!");
        } else {
            this.cnh = cnh;
        }
    }

    // ---------- habilitação: o cliente pode dirigir esse carro? ----------

    // devolve o motivo de não poder dirigir, ou null se está tudo certo
    public String motivoNaoPodeDirigir(Veiculo veiculo){
        if (cnh == null){
            return "Cliente sem CNH.";
        }
        if (cnh.isVencida()){
            return "CNH vencida.";
        }
        if (!cnh.permiteCarro()){
            return "A categoria " + cnh.getCategoria() + " não permite dirigir carros.";
        }
        // SUV e elétrico: 21 anos ou mais e CNH há pelo menos 2 anos
        if (veiculo instanceof SUV || veiculo instanceof Eletrico){
            if (getIdade() < 21){
                return "SUV e elétrico só para quem tem 21 anos ou mais.";
            }
            if (cnh.anosDeHabilitacao() < 2){
                return "SUV e elétrico exigem CNH há pelo menos 2 anos.";
            }
        }
        return null;
    }

    public boolean podeDirigir(Veiculo veiculo){
        return motivoNaoPodeDirigir(veiculo) == null;
    }

    // ---------- fidelidade ----------

    public int getPontos(){
        return pontos;
    }

    public NivelFidelidade getNivel(){
        return NivelFidelidade.porPontos(pontos);
    }

    public double getDescontoFidelidade(){
        return getNivel().getDesconto();
    }

    public double aplicarDesconto(double valor){
        return valor * (1 - getDescontoFidelidade());
    }

    // chamar quando o aluguel for finalizado, com o valor pago no aluguel:
    // 1 ponto a cada R$ 10,00 (multas e avarias não contam; os pontos não expiram)
    public void registrarAluguel(double valorPago){
        if (valorPago <= 0){
            System.out.println("ERRO: O valor pago no aluguel deve ser maior que zero!");
            return;
        }

        NivelFidelidade antes = getNivel();
        pontos += (int) (valorPago / REAIS_POR_PONTO);
        NivelFidelidade depois = getNivel();

        if (depois != antes){
            System.out.println(getNome() + " subiu de nível: " + antes.getNome() + " -> " + depois.getNome() + "!");
        }
    }

    // ---------- bloqueio por multa ou avaria ----------

    public double getDebito(){
        return debito;
    }

    public String getMotivoDebito(){
        return motivoDebito;
    }

    public boolean isBloqueado(){
        return debito > 0;
    }

    public void registrarDebito(double valor, String motivo){
        if (valor <= 0){
            System.out.println("ERRO: O valor do débito deve ser maior que zero!");
            return;
        }
        debito += valor;
        motivoDebito = motivo;
        System.out.println(getNome() + " bloqueado: " + motivo + " (R$ " + String.format("%.2f", debito) + ")");
    }

    public void quitarDebito(double valor){
        if (valor <= 0){
            System.out.println("ERRO: O valor pago deve ser maior que zero!");
            return;
        }

        debito -= valor;

        if (debito <= 0){
            debito = 0;
            motivoDebito = "";
            System.out.println(getNome() + " desbloqueado!");
        }
    }

    // ---------- regra final para a Parte 3 usar ----------

    // devolve o motivo de não poder alugar, ou null se pode
    public String motivoNaoPodeAlugar(Veiculo veiculo){
        if (isBloqueado()){
            return "Cliente bloqueado: " + motivoDebito + ".";
        }
        return motivoNaoPodeDirigir(veiculo);
    }

    public boolean podeAlugar(Veiculo veiculo){
        return motivoNaoPodeAlugar(veiculo) == null;
    }

    @Override
    public String toString(){
        return getNome() + " | CPF " + getCpfFormatado() + " | " + getNivel().getNome()
                + " (" + pontos + " pts)" + (isBloqueado() ? " | BLOQUEADO" : "");
    }
}
