package Persistencia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Interface de CRUD comum a carros, clientes, funcionarios e alugueis. */
public class Repositorio<T> {
    private final Map<String,T> itens = new LinkedHashMap<>();
    private final Function<T,String> obterChave;

    public Repositorio(Function<T,String> obterChave) {
        if (obterChave == null) throw new IllegalArgumentException("Funcao de chave obrigatoria");
        this.obterChave = obterChave;
    }

    public void salvar(T item) {
        if (item == null) throw new IllegalArgumentException("Item nao pode ser nulo");
        String chave = obterChave.apply(item);
        if (chave == null || chave.isBlank()) throw new IllegalArgumentException("Chave nao pode ser vazia");
        itens.put(chave, item); // cadastra ou atualiza
    }

    public T buscar(String chave) { return itens.get(chave); }
    public List<T> listar() { return new ArrayList<>(itens.values()); }
    public boolean remover(String chave) { return itens.remove(chave) != null; }
    public int quantidade() { return itens.size(); }
}
