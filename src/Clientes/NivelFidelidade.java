package Clientes;

public enum NivelFidelidade {
    BRONZE("Bronze", 0, 0.00),
    PRATA("Prata", 100, 0.05),
    OURO("Ouro", 300, 0.10),
    DIAMANTE("Diamante", 600, 0.15);

    private final String nome;
    private final int pontosMinimos;
    private final double desconto; // 0.05 = 5%

    NivelFidelidade(String nome, int pontosMinimos, double desconto){
        this.nome = nome;
        this.pontosMinimos = pontosMinimos;
        this.desconto = desconto;
    }

    public String getNome(){
        return nome;
    }

    public int getPontosMinimos(){
        return pontosMinimos;
    }

    public double getDesconto(){
        return desconto;
    }

    // descobre o nível de acordo com os pontos
    public static NivelFidelidade porPontos(int pontos){
        NivelFidelidade resultado = BRONZE;

        for (NivelFidelidade n : values()){
            if (pontos >= n.pontosMinimos){
                resultado = n;
            }
        }

        return resultado;
    }
}
