package Telas;

public abstract class Tela {
    private String titulo;

    public Tela(String titulo){
        this.titulo = titulo;
    }

    public String getTitulo(){
        return titulo;
    }

    // opções numeradas a partir de 1; a opção 0 é sempre "Voltar" (ou "Sair" no menu principal)
    public abstract String[] getOpcoes();

    public abstract void executar(int opcao);

    public String getTextoSair(){
        return "Voltar";
    }

    public void abrir(){
        while (true){
            cabecalho(titulo);

            String[] opcoes = getOpcoes();
            for (int i = 0; i < opcoes.length; i++){
                System.out.println(" " + (i + 1) + " - " + opcoes[i]);
            }
            System.out.println(" 0 - " + getTextoSair());
            System.out.println("==========================================");

            int opcao = Entrada.lerInteiro("Escolha uma opção", 0, opcoes.length);
            if (opcao == 0){
                return;
            }

            System.out.println();
            executar(opcao);
            Entrada.pausar();
        }
    }

    public static void cabecalho(String texto){
        System.out.println();
        System.out.println("==========================================");
        System.out.println("  RODA FÁCIL - " + texto.toUpperCase());
        System.out.println("==========================================");
    }
}
