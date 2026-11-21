package Telas;

import Alugueis.Fechamento;
import Alugueis.Locacao;
import Alugueis.Orcamento;
import Alugueis.SituacaoLocacao;
import Carros.Veiculo;
import Clientes.Cliente;

import java.time.LocalDate;
import java.util.ArrayList;

// painel gerencial: faturamento do mês, carros mais alugados, melhores clientes e ocupação da frota
public class PainelGerencial {
    private static final int TOP = 5;

    private Sistema sistema;

    public PainelGerencial(Sistema sistema){
        this.sistema = sistema;
    }

    public void mostrar(){
        Tela.cabecalho("Painel gerencial");
        faturamentoDoMes();
        carrosMaisAlugados();
        melhoresClientes();
        ocupacaoDaFrota();
    }

    // soma o que foi pago nas locações devolvidas neste mês (locação + cobranças extras)
    private void faturamentoDoMes(){
        LocalDate hoje = LocalDate.now();
        int quantidade = 0;
        double locacoes = 0;
        double extras = 0;

        for (Locacao l : sistema.getLocacoes().buscarPorSituacao(SituacaoLocacao.FINALIZADA)){
            LocalDate devolucao = l.getDataDevolucaoReal().toLocalDate();
            if (devolucao.getMonth() == hoje.getMonth() && devolucao.getYear() == hoje.getYear()){
                Fechamento f = l.getFechamento();
                quantidade++;
                locacoes += f.getValorLocacao();
                extras += f.getTotalExtras();
            }
        }

        System.out.println("\n>> FATURAMENTO DE " + String.format("%02d/%d", hoje.getMonthValue(), hoje.getYear()));
        System.out.println("Locações finalizadas: " + quantidade);
        System.out.println("Valor das locações:   " + Orcamento.dinheiro(locacoes));
        System.out.println("Cobranças extras:     " + Orcamento.dinheiro(extras));
        System.out.println("TOTAL:                " + Orcamento.dinheiro(locacoes + extras));
    }

    private void carrosMaisAlugados(){
        ArrayList<Veiculo> frota = sistema.getLocadora().getFrota();
        // ordena do carro com mais aluguéis para o com menos
        frota.sort((a, b) -> contarAlugueis(b) - contarAlugueis(a));

        System.out.println("\n>> CARROS MAIS ALUGADOS");
        int posicao = 0;
        for (Veiculo v : frota){
            int vezes = contarAlugueis(v);
            if (vezes == 0 || posicao == TOP){
                break;
            }
            posicao++;
            System.out.println(posicao + "º " + v.getMarca() + " " + v.getModelo() + " (" + v.getPlaca() + ") - "
                    + vezes + " aluguel(éis)");
        }
        if (posicao == 0){
            System.out.println("Nenhum carro foi alugado ainda.");
        }
    }

    private void melhoresClientes(){
        ArrayList<Cliente> clientes = sistema.getClientes().getClientes();
        // ordena de quem gastou mais para quem gastou menos
        clientes.sort((a, b) -> Double.compare(totalGasto(b), totalGasto(a)));

        System.out.println("\n>> MELHORES CLIENTES");
        int posicao = 0;
        for (Cliente c : clientes){
            double total = totalGasto(c);
            if (total == 0 || posicao == TOP){
                break;
            }
            posicao++;
            System.out.println(posicao + "º " + c.getNome() + " - " + Orcamento.dinheiro(total)
                    + " | " + c.getNivel().getNome() + " (" + c.getPontos() + " pts)");
        }
        if (posicao == 0){
            System.out.println("Nenhum cliente finalizou um aluguel ainda.");
        }
    }

    private void ocupacaoDaFrota(){
        ArrayList<Veiculo> frota = sistema.getLocadora().getFrota();
        int alugados = 0;
        int manutencao = 0;

        for (Veiculo v : frota){
            if (v.getSituacao().equals("Alugado")){
                alugados++;
            } else if (v.getSituacao().equals("Manutenção")){
                manutencao++;
            }
        }
        int livres = frota.size() - alugados - manutencao;
        double porcentagem = frota.isEmpty() ? 0 : alugados * 100.0 / frota.size();

        System.out.println("\n>> OCUPAÇÃO DA FROTA");
        System.out.println("Total de carros: " + frota.size());
        System.out.println("Alugados: " + alugados + " | Livres: " + livres + " | Em manutenção: " + manutencao);
        System.out.println("Frota alugada: " + String.format("%.1f", porcentagem) + "% " + barra(porcentagem));
    }

    // ---------- métodos de apoio ----------

    // reservas canceladas não contam
    private int contarAlugueis(Veiculo veiculo){
        int vezes = 0;
        for (Locacao l : sistema.getLocacoes().getLocacoes()){
            if (l.isDoVeiculo(veiculo) && l.getSituacao() != SituacaoLocacao.CANCELADA){
                vezes++;
            }
        }
        return vezes;
    }

    // só conta o que já foi fechado (locações finalizadas)
    private double totalGasto(Cliente cliente){
        double total = 0;
        for (Locacao l : sistema.getLocacoes().buscarPorCliente(cliente)){
            if (l.getSituacao() == SituacaoLocacao.FINALIZADA){
                total += l.getFechamento().getTotalGeral();
            }
        }
        return total;
    }

    // [#####.....] desenha a porcentagem com 20 casas
    private String barra(double porcentagem){
        int cheias = (int) Math.round(porcentagem / 5);
        return "[" + "#".repeat(cheias) + ".".repeat(20 - cheias) + "]";
    }
}
