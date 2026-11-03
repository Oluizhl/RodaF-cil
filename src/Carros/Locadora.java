package Carros;

import java.util.ArrayList;

public class Locadora {
    private ArrayList<Veiculo> frota;

    public Locadora(){
        frota = new ArrayList<Veiculo>();
    }

    public void cadastrar(Veiculo veiculo){
        frota.add(veiculo);
    }

    public void listar(){
        for (Veiculo v : frota){
            System.out.println("Placa: " + v.getPlaca());
            System.out.println("Marca: " + v.getMarca());
            System.out.println("Modelo: " + v.getModelo());
            System.out.println("Ano: " + v.getAno());
            System.out.println("Km: " + v.getKm());
            System.out.println("Situação: " + v.getSituacao());
            System.out.println("Diária: " + v.calcularDiaria());
            System.out.println("-----------------------------");
        }
    }

    public Veiculo buscarPorPlaca(String placa){
        for (Veiculo v : frota){
            if (v.getPlaca().equals(placa)){
                return v;
            }
        }
        return null;
    }

    public void mostrarHistorico(String placa){
        Veiculo veiculo = buscarPorPlaca(placa);

        if (veiculo == null){
            System.out.println("ERRO: Veículo com essa placa não encontrado!");
        } else {
            veiculo.mostrarHistorico();
        }
    }

    public void remover(String placa){
        Veiculo veiculo = buscarPorPlaca(placa); // reutilizar o método buscarPorPlaca
        if (veiculo != null){
            frota.remove(veiculo); // função remove (própria do ArrayList<>
            System.out.println("Veículo removido com sucesso!");
        } else {
            System.out.println("ERRO: Veículo com essa placa não encontrado!");
        }
    }

    public void editar(String placa, int novoKm, String novaSituacao, double novoValorDiaria){
        Veiculo veiculo = buscarPorPlaca(placa);

        if (veiculo != null){
            veiculo.setKm(novoKm);
            veiculo.setSituacao(novaSituacao);
            veiculo.setValorDiaria(novoValorDiaria);
            System.out.println("Dados alterados com sucesso!");
        } else {
            System.out.println("ERRO: Veículo com essa placa nao encontrado!");
        }
    }

    public void alugarCarro(String placa){
        Veiculo veiculo = buscarPorPlaca(placa);

        if (veiculo == null){
            System.out.println("Não há nenhum veículo com a placa inserida!");
        } else if (!veiculo.getSituacao().equals("Livre")){
            System.out.println("O veículo com a placa inserida não está disponível para aluguel no momento!");
        } else if (veiculo.precisaRevisao()){
            System.out.println("Veículo bloqueado: rodou 10.000 km desde a última revisão.");
        } else {
            veiculo.setSituacao("Alugado");
            System.out.println("Carro alugado com sucesso!");
        }
    }

    public ArrayList<Veiculo> buscarPorTipo(String tipo){
        ArrayList<Veiculo> resultado = new ArrayList<Veiculo>();

        for (Veiculo v : frota){
            if (tipo.equals("Carros.Passeio") && v instanceof Passeio){
                resultado.add(v);
            } else if (tipo.equals("Carros.SUV") && v instanceof SUV){
                resultado.add(v);
            } else if (tipo.equals("Carros.Utilitario") && v instanceof Utilitario){
                resultado.add(v);
            } else if (tipo.equals("Carros.Eletrico") && v instanceof Eletrico){
                resultado.add(v);
            }
        }

        return resultado;
    }

    public ArrayList<Veiculo> buscarPorPreco(double precoMaximo){
        ArrayList<Veiculo> resultado = new ArrayList<Veiculo>();

        for (Veiculo v : frota){
            if (v.calcularDiaria() <= precoMaximo){
                resultado.add(v);
            }
        }

        return resultado;
    }

    public ArrayList<Veiculo> buscarLivres(){
        ArrayList<Veiculo> resultado = new ArrayList<Veiculo>();

        for (Veiculo v : frota){
            if (v.getSituacao().equals("Livre") && !v.precisaRevisao()){
                resultado.add(v);
            }
        }

        return resultado;
    }
}
