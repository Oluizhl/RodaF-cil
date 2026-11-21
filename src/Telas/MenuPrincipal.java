package Telas;

// menu principal: abre as outras telas, todas com o mesmo padrão
public class MenuPrincipal extends Tela {
    private Sistema sistema;

    public MenuPrincipal(Sistema sistema){
        super("Menu principal");
        this.sistema = sistema;
    }

    @Override
    public String[] getOpcoes(){
        return new String[]{
                "Carros",
                "Clientes",
                "Funcionários",
                "Aluguéis",
                "Painel gerencial",
                "Carregar dados de exemplo",
                "Fazer backup no Google Drive",
                "Exportar dados para CSV"
        };
    }

    @Override
    public String getTextoSair(){
        return "Sair";
    }

    @Override
    public void executar(int opcao){
        switch (opcao){
            case 1: new TelaCarros(sistema).abrir(); break;
            case 2: new TelaClientes(sistema).abrir(); break;
            case 3: new TelaFuncionarios(sistema).abrir(); break;
            case 4: new TelaAlugueis(sistema).abrir(); break;
            case 5: new PainelGerencial(sistema).mostrar(); break;
            case 6: carregarExemplo(); break;
            case 7: sistema.fazerBackupAgora(); break;
            case 8: sistema.exportarCsv(); break;
        }
        // salva tudo em dados/locadora.json depois de cada tela (Parte 4)
        sistema.salvar();
    }

    private void carregarExemplo(){
        System.out.println("ATENÇÃO: isso apaga os dados atuais e coloca dados de demonstração.");
        if (Entrada.lerSimNao("Continuar?")){
            DadosExemplo.carregar(sistema);
        }
    }
}
