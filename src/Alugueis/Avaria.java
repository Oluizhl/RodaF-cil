package Alugueis;

// um dano registrado pelo funcionário na vistoria da devolução
public class Avaria {
    private TipoAvaria tipo;
    private String descricao;
    private double valorConserto;

    public Avaria(TipoAvaria tipo, String descricao, double valorConserto) throws LocadoraException {
        if (tipo == null){
            throw new OperacaoInvalidaException("Informe o tipo da avaria!");
        }
        if (valorConserto <= 0){
            throw new OperacaoInvalidaException("O valor do conserto deve ser maior que zero!");
        }
        this.tipo = tipo;
        this.descricao = (descricao != null) ? descricao : "";
        this.valorConserto = valorConserto;
    }

    public TipoAvaria getTipo(){
        return tipo;
    }

    public String getDescricao(){
        return descricao;
    }

    public double getValorConserto(){
        return valorConserto;
    }

    @Override
    public String toString(){
        return tipo.getNome() + " - " + descricao + " (conserto " + Orcamento.dinheiro(valorConserto) + ")";
    }
}
