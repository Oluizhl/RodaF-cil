package Persistencia;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converte o estado dos objetos do grupo para uma arvore JSON e vice-versa.
 * Mantem referencias compartilhadas: a locacao e a frota apontam para o MESMO carro.
 *
 * Como as classes das Partes 1 a 3 nao possuem construtores de restauracao, usa
 * reflexao para recuperar os campos sem disparar regras de cadastro ou de aluguel.
 * Nao modifica nenhuma classe dos colegas e nao depende de Gson/Jackson.
 */
final class ConversorObjetos {
    private final IdentityHashMap<Object, Integer> gravados = new IdentityHashMap<>();
    private final Map<Integer, Object> carregados = new HashMap<>();
    private int proximoId = 1;

    Object codificar(Object valor) throws IOException {
        if (valor == null || valor instanceof String || valor instanceof Number || valor instanceof Boolean)
            return valor;
        if (valor instanceof LocalDate || valor instanceof LocalDateTime) {
            Map<String,Object> data = novoMapa();
            data.put("$data", valor instanceof LocalDate ? "LocalDate" : "LocalDateTime");
            data.put("valor", valor.toString());
            return data;
        }
        if (valor instanceof Enum<?>) {
            Enum<?> enumerado = (Enum<?>) valor;
            validarClasse(enumerado.getDeclaringClass());
            Map<String,Object> mapa = novoMapa();
            mapa.put("$enum", enumerado.getDeclaringClass().getName());
            mapa.put("valor", enumerado.name());
            return mapa;
        }

        Integer existente = gravados.get(valor);
        if (existente != null) {
            Map<String,Object> referencia = novoMapa();
            referencia.put("$ref", existente);
            return referencia;
        }
        int id = proximoId++;
        gravados.put(valor, id);
        if (valor instanceof List<?>) {
            Map<String,Object> mapa = novoMapa();
            mapa.put("$id", id);
            List<Object> itens = new ArrayList<>();
            for (Object item : (List<?>) valor) itens.add(codificar(item));
            mapa.put("$lista", itens);
            return mapa;
        }

        Class<?> classe = valor.getClass();
        validarClasse(classe);
        Map<String,Object> mapa = novoMapa();
        mapa.put("$id", id);
        mapa.put("$classe", classe.getName());
        Map<String,Object> campos = novoMapa();
        for (Field campo : todosCampos(classe)) {
            try {
                campo.setAccessible(true);
                campos.put(campo.getDeclaringClass().getName() + "#" + campo.getName(), codificar(campo.get(valor)));
            } catch (ReflectiveOperationException | RuntimeException e) {
                throw new IOException("Nao foi possivel ler " + classe.getSimpleName() + "." + campo.getName(), e);
            }
        }
        mapa.put("campos", campos);
        return mapa;
    }

    Object decodificar(Object valor) throws IOException {
        if (valor == null || valor instanceof String || valor instanceof Number || valor instanceof Boolean) return valor;
        if (!(valor instanceof Map<?, ?>)) throw new IOException("Objeto JSON inesperado");
        Map<String,Object> mapa = mapa(valor);
        if (mapa.containsKey("$ref")) {
            int id = inteiro(mapa.get("$ref"));
            if (!carregados.containsKey(id)) throw new IOException("Referencia inexistente: " + id);
            return carregados.get(id);
        }
        if (mapa.containsKey("$data")) {
            try {
                String tipo = texto(mapa.get("$data"));
                String data = texto(mapa.get("valor"));
                if (tipo.equals("LocalDate")) return LocalDate.parse(data);
                if (tipo.equals("LocalDateTime")) return LocalDateTime.parse(data);
                throw new IOException("Tipo de data nao suportado: " + tipo);
            } catch (java.time.format.DateTimeParseException e) {
                throw new IOException("Data invalida no JSON", e);
            }
        }
        if (mapa.containsKey("$enum")) {
            try {
                Class<?> classe = Class.forName(texto(mapa.get("$enum")));
                validarClasse(classe);
                if (!classe.isEnum()) throw new IOException("Nao e enumeracao: " + classe.getName());
                @SuppressWarnings({"unchecked", "rawtypes"})
                Object constante = Enum.valueOf((Class<? extends Enum>) classe, texto(mapa.get("valor")));
                return constante;
            } catch (ClassNotFoundException | IllegalArgumentException e) {
                throw new IOException("Enumeracao invalida no JSON", e);
            }
        }
        if (!mapa.containsKey("$id")) throw new IOException("Faltando id do objeto JSON");
        int id = inteiro(mapa.get("$id"));
        if (id <= 0 || carregados.containsKey(id)) throw new IOException("Id duplicado ou invalido: " + id);
        if (mapa.containsKey("$lista")) {
            List<Object> lista = new ArrayList<>();
            carregados.put(id, lista); // antes dos filhos, para permitir referencias circulares
            for (Object item : lista(mapa.get("$lista"))) lista.add(decodificar(item));
            return lista;
        }
        try {
            Class<?> classe = Class.forName(texto(mapa.get("$classe")));
            validarClasse(classe);
            if (classe.isEnum() || classe.isInterface() || Modifier.isAbstract(classe.getModifiers()))
                throw new IOException("Tipo nao instanciavel: " + classe.getName());
            Object objeto = novoObjetoSemConstrutor(classe);
            carregados.put(id, objeto);
            Map<String,Object> campos = mapa(mapa.get("campos"));
            for (Field campo : todosCampos(classe)) {
                String chave = campo.getDeclaringClass().getName() + "#" + campo.getName();
                if (!campos.containsKey(chave)) throw new IOException("Campo ausente: " + chave);
                Object dado = decodificar(campos.get(chave));
                campo.setAccessible(true);
                campo.set(objeto, converterTipo(dado, campo.getType()));
            }
            return objeto;
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new IOException("Erro ao reconstruir objeto do JSON: " + mapa.get("$classe"), e);
        }
    }

    private static Object converterTipo(Object valor, Class<?> tipo) throws IOException {
        if (valor == null) {
            if (tipo.isPrimitive()) throw new IOException("Campo primitivo nulo");
            return null;
        }
        if (tipo == int.class || tipo == Integer.class) return numero(valor).intValue();
        if (tipo == long.class || tipo == Long.class) return numero(valor).longValue();
        if (tipo == double.class || tipo == Double.class) return numero(valor).doubleValue();
        if (tipo == float.class || tipo == Float.class) return numero(valor).floatValue();
        if (tipo == boolean.class || tipo == Boolean.class) {
            if (!(valor instanceof Boolean)) throw new IOException("Booleano invalido");
            return valor;
        }
        if (!tipo.isInstance(valor)) throw new IOException("Campo invalido: esperado " + tipo.getName());
        return valor;
    }

    private static Number numero(Object valor) throws IOException {
        if (!(valor instanceof Number)) throw new IOException("Numero esperado no JSON");
        return (Number) valor;
    }
    private static int inteiro(Object valor) throws IOException {
        Number num = numero(valor);
        if (num.doubleValue() != num.intValue()) throw new IOException("Inteiro invalido");
        return num.intValue();
    }
    private static String texto(Object valor) throws IOException {
        if (!(valor instanceof String)) throw new IOException("Texto esperado no JSON");
        return (String) valor;
    }
    @SuppressWarnings("unchecked")
    private static Map<String,Object> mapa(Object v) throws IOException {
        if (!(v instanceof Map<?,?>)) throw new IOException("Objeto esperado no JSON");
        return (Map<String,Object>) v;
    }
    @SuppressWarnings("unchecked")
    private static List<Object> lista(Object v) throws IOException {
        if (!(v instanceof List<?>)) throw new IOException("Lista esperada no JSON");
        return (List<Object>) v;
    }
    private static Map<String,Object> novoMapa() { return new LinkedHashMap<>(); }

    private static List<Field> todosCampos(Class<?> classe) {
        List<Field> campos = new ArrayList<>();
        for (Class<?> atual = classe; atual != null && atual != Object.class; atual = atual.getSuperclass()) {
            for (Field campo : atual.getDeclaredFields()) {
                if (!Modifier.isStatic(campo.getModifiers()) && !Modifier.isTransient(campo.getModifiers()) && !campo.isSynthetic())
                    campos.add(campo);
            }
        }
        campos.sort(Comparator.comparing(c -> c.getDeclaringClass().getName() + "#" + c.getName()));
        return campos;
    }

    private static void validarClasse(Class<?> classe) throws IOException {
        String pacote = classe.getPackageName();
        if (!pacote.equals("Carros") && !pacote.equals("Clientes") && !pacote.equals("Alugueis"))
            throw new IOException("Classe nao permitida no banco de dados: " + classe.getName());
        if (classe.getName().contains("$")) throw new IOException("Classe interna nao permitida");
    }

    /** Aloca classes do proprio projeto sem executar construtores de regras de negocio. */
    private static Object novoObjetoSemConstrutor(Class<?> classe) throws ReflectiveOperationException {
        Class<?> unsafe = Class.forName("sun.misc.Unsafe");
        Field singleton = unsafe.getDeclaredField("theUnsafe");
        singleton.setAccessible(true);
        Method alocar = unsafe.getMethod("allocateInstance", Class.class);
        try {
            return alocar.invoke(singleton.get(null), classe);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable causa = e.getCause();
            if (causa instanceof RuntimeException) throw (RuntimeException) causa;
            throw new ReflectiveOperationException("Nao foi possivel criar " + classe.getName(), causa);
        }
    }
}
