package Persistencia;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** Conjunto de dados da locadora: uma unica fotografia consistente do sistema. */
public class EstadoSistema {
    private final List<Object> carros = new ArrayList<>();
    private final List<Object> clientes = new ArrayList<>();
    private final List<Object> funcionarios = new ArrayList<>();
    private final List<Object> alugueis = new ArrayList<>();
    private int proximoIdLocacao = 1;

    public EstadoSistema() { }

    public EstadoSistema(List<?> carros, List<?> clientes, List<?> funcionarios, List<?> alugueis) {
        setCarros(carros);
        setClientes(clientes);
        setFuncionarios(funcionarios);
        setAlugueis(alugueis);
    }

    public void setCarros(List<?> lista) { substituir(carros, lista); }
    public void setClientes(List<?> lista) { substituir(clientes, lista); }
    public void setFuncionarios(List<?> lista) { substituir(funcionarios, lista); }
    public void setAlugueis(List<?> lista) { substituir(alugueis, lista); }

    public List<Object> getCarros() { return new ArrayList<>(carros); }
    public List<Object> getClientes() { return new ArrayList<>(clientes); }
    public List<Object> getFuncionarios() { return new ArrayList<>(funcionarios); }
    public List<Object> getAlugueis() { return new ArrayList<>(alugueis); }

    public <T> List<T> carros(Class<T> tipo) { return tipar(carros, tipo); }
    public <T> List<T> clientes(Class<T> tipo) { return tipar(clientes, tipo); }
    public <T> List<T> funcionarios(Class<T> tipo) { return tipar(funcionarios, tipo); }
    public <T> List<T> alugueis(Class<T> tipo) { return tipar(alugueis, tipo); }

    public int getProximoIdLocacao() { return proximoIdLocacao; }
    public void setProximoIdLocacao(int id) {
        if (id < 1) throw new IllegalArgumentException("Proximo id de locacao deve ser positivo");
        proximoIdLocacao = id;
    }

    private static void substituir(List<Object> destino, List<?> origem) {
        destino.clear();
        if (origem != null) destino.addAll(origem);
    }

    private static <T> List<T> tipar(List<Object> objetos, Class<T> tipo) {
        List<T> saida = new ArrayList<>();
        for (Object objeto : objetos) saida.add(tipo.cast(objeto));
        return saida;
    }

    /**
     * Le as listas dos gerenciadores existentes sem alterar os codigos dos outros colegas.
     * Ordem: Carros.Locadora, Clientes.CadastroClientes,
     * Clientes.CadastroFuncionarios, Alugueis.GerenciadorLocacoes.
     */
    public static EstadoSistema capturar(Object locadora, Object cadastroClientes,
                                         Object cadastroFuncionarios, Object gerenciadorLocacoes) throws IOException {
        EstadoSistema estado = new EstadoSistema(
                lerLista(locadora, "Carros.Locadora", "frota"),
                lerLista(cadastroClientes, "Clientes.CadastroClientes", "clientes"),
                lerLista(cadastroFuncionarios, "Clientes.CadastroFuncionarios", "funcionarios"),
                lerLista(gerenciadorLocacoes, "Alugueis.GerenciadorLocacoes", "locacoes")
        );
        try {
            Object id = campo(gerenciadorLocacoes, "Alugueis.GerenciadorLocacoes", "proximoId").get(gerenciadorLocacoes);
            estado.setProximoIdLocacao((Integer) id);
        } catch (IllegalAccessException e) {
            throw new IOException("Nao foi possivel ler o proximo id de locacao", e);
        }
        return estado;
    }

    /**
     * Coloca listas carregadas nos mesmos gerenciadores da Parte 5.
     * As referencias entre aluguel, carro, cliente e funcionario sao preservadas.
     * Chamar ao iniciar o programa, ANTES de iniciar novos cadastros.
     */
    public void aplicarEm(Object locadora, Object cadastroClientes,
                         Object cadastroFuncionarios, Object gerenciadorLocacoes) throws IOException {
        // Validar todos os destinos antes de modificar qualquer um.
        Field a = campo(locadora, "Carros.Locadora", "frota");
        Field b = campo(cadastroClientes, "Clientes.CadastroClientes", "clientes");
        Field c = campo(cadastroFuncionarios, "Clientes.CadastroFuncionarios", "funcionarios");
        Field d = campo(gerenciadorLocacoes, "Alugueis.GerenciadorLocacoes", "locacoes");
        Field id = campo(gerenciadorLocacoes, "Alugueis.GerenciadorLocacoes", "proximoId");
        try {
            a.set(locadora, new ArrayList<>(carros));
            b.set(cadastroClientes, new ArrayList<>(clientes));
            c.set(cadastroFuncionarios, new ArrayList<>(funcionarios));
            d.set(gerenciadorLocacoes, new ArrayList<>(alugueis));
            id.setInt(gerenciadorLocacoes, Math.max(proximoIdLocacao, maiorIdAluguel() + 1));
        } catch (IllegalAccessException e) {
            throw new IOException("Nao foi possivel aplicar os dados nos gerenciadores", e);
        }
    }

    private int maiorIdAluguel() throws IOException {
        int maior = 0;
        for (Object aluguel : alugueis) {
            try {
                Object valor = aluguel.getClass().getMethod("getId").invoke(aluguel);
                maior = Math.max(maior, ((Number) valor).intValue());
            } catch (ReflectiveOperationException e) {
                throw new IOException("Aluguel sem metodo getId", e);
            }
        }
        return maior;
    }

    private static List<?> lerLista(Object obj, String nomeClasse, String nomeCampo) throws IOException {
        try {
            Object valor = campo(obj, nomeClasse, nomeCampo).get(obj);
            if (!(valor instanceof List<?>)) throw new IOException(nomeCampo + " nao contem lista");
            return new ArrayList<>((List<?>) valor);
        } catch (IllegalAccessException e) {
            throw new IOException("Nao foi possivel ler a lista " + nomeCampo, e);
        }
    }

    private static Field campo(Object obj, String nomeClasse, String nomeCampo) throws IOException {
        if (obj == null || !obj.getClass().getName().equals(nomeClasse))
            throw new IOException("Esperado " + nomeClasse + " na integracao da Parte 4");
        try {
            Field campo = obj.getClass().getDeclaredField(nomeCampo);
            campo.setAccessible(true);
            return campo;
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new IOException("Campo nao encontrado: " + nomeClasse + "." + nomeCampo, e);
        }
    }
}
