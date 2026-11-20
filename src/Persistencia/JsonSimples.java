package Persistencia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Leitor e escritor JSON independente de bibliotecas externas. */
final class JsonSimples {
    private JsonSimples() { }

    static String escrever(Object valor) throws IOException {
        StringBuilder sb = new StringBuilder();
        escreverValor(valor, sb, 0);
        return sb.toString() + "\n";
    }

    private static void escreverValor(Object valor, StringBuilder sb, int nivel) throws IOException {
        if (nivel > 100) throw new IOException("JSON muito profundo");
        if (valor == null) { sb.append("null"); return; }
        if (valor instanceof String) { escreverTexto((String) valor, sb); return; }
        if (valor instanceof Boolean) { sb.append(valor); return; }
        if (valor instanceof Number) {
            double n = ((Number) valor).doubleValue();
            if (!Double.isFinite(n)) throw new IOException("Numero invalido no JSON");
            sb.append(valor); return;
        }
        if (valor instanceof Map<?, ?>) {
            sb.append('{'); boolean primeiro = true;
            for (Map.Entry<?, ?> entrada : ((Map<?, ?>) valor).entrySet()) {
                if (!(entrada.getKey() instanceof String)) throw new IOException("Chave JSON nao e texto");
                if (!primeiro) sb.append(',');
                primeiro = false;
                sb.append('\n'); recuar(sb, nivel + 1);
                escreverTexto((String) entrada.getKey(), sb);
                sb.append(": "); escreverValor(entrada.getValue(), sb, nivel + 1);
            }
            if (!primeiro) { sb.append('\n'); recuar(sb, nivel); }
            sb.append('}'); return;
        }
        if (valor instanceof List<?>) {
            sb.append('['); boolean primeiro = true;
            for (Object item : (List<?>) valor) {
                if (!primeiro) sb.append(',');
                primeiro = false;
                sb.append('\n'); recuar(sb, nivel + 1);
                escreverValor(item, sb, nivel + 1);
            }
            if (!primeiro) { sb.append('\n'); recuar(sb, nivel); }
            sb.append(']'); return;
        }
        throw new IOException("Tipo JSON nao aceito: " + valor.getClass().getName());
    }

    private static void recuar(StringBuilder sb, int nivel) {
        for (int i = 0; i < nivel; i++) sb.append("  ");
    }

    private static void escreverTexto(String texto, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    if (c < 32) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
    }

    static Object ler(String texto) throws IOException {
        Leitor leitor = new Leitor(texto);
        Object valor = leitor.valor(0);
        leitor.espacos();
        if (leitor.i != texto.length()) throw leitor.erro("Texto extra depois do JSON");
        return valor;
    }

    private static final class Leitor {
        private final String s;
        private int i;
        Leitor(String s) { this.s = s; }
        IOException erro(String msg) { return new IOException(msg + " (posicao " + i + ")"); }
        void espacos() {
            while (i < s.length() && (s.charAt(i) == ' ' || s.charAt(i) == '\n'
                    || s.charAt(i) == '\r' || s.charAt(i) == '\t')) i++;
        }
        char atual() throws IOException {
            if (i >= s.length()) throw erro("JSON incompleto");
            return s.charAt(i);
        }
        void esperar(char c) throws IOException {
            espacos(); if (atual() != c) throw erro("Era esperado '" + c + "'"); i++;
        }
        Object valor(int profundidade) throws IOException {
            if (profundidade > 100) throw erro("JSON profundo demais");
            espacos(); char c = atual();
            if (c == '"') return texto();
            if (c == '{') {
                i++; Map<String, Object> obj = new LinkedHashMap<>();
                espacos(); if (atual() == '}') { i++; return obj; }
                while (true) {
                    espacos(); if (atual() != '"') throw erro("Chave nao e texto");
                    String chave = texto(); esperar(':');
                    if (obj.containsKey(chave)) throw erro("Chave duplicada: " + chave);
                    obj.put(chave, valor(profundidade + 1));
                    espacos(); c = atual(); i++;
                    if (c == '}') return obj;
                    if (c != ',') throw erro("Esperada virgula ou fim do objeto");
                }
            }
            if (c == '[') {
                i++; List<Object> lista = new ArrayList<>();
                espacos(); if (atual() == ']') { i++; return lista; }
                while (true) {
                    lista.add(valor(profundidade + 1));
                    espacos(); c = atual(); i++;
                    if (c == ']') return lista;
                    if (c != ',') throw erro("Esperada virgula ou fim da lista");
                }
            }
            if (s.startsWith("true", i)) { i += 4; return true; }
            if (s.startsWith("false", i)) { i += 5; return false; }
            if (s.startsWith("null", i)) { i += 4; return null; }
            if (c == '-' || Character.isDigit(c)) return numero();
            throw erro("Valor JSON desconhecido");
        }
        String texto() throws IOException {
            esperar('"'); StringBuilder sb = new StringBuilder();
            while (true) {
                char c = atual(); i++;
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    char esc = atual(); i++;
                    switch (esc) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            if (i + 4 > s.length()) throw erro("Unicode incompleto");
                            try { sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); }
                            catch (NumberFormatException e) { throw erro("Unicode invalido"); }
                            i += 4; break;
                        default: throw erro("Escape invalido");
                    }
                } else {
                    if (c < 32) throw erro("Controle invalido em texto JSON");
                    sb.append(c);
                }
            }
        }
        Number numero() throws IOException {
            int inicio = i;
            if (s.charAt(i) == '-') i++;
            if (i >= s.length()) throw erro("Numero incompleto");
            if (s.charAt(i) == '0') i++;
            else {
                if (s.charAt(i) < '1' || s.charAt(i) > '9') throw erro("Numero invalido");
                while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
            }
            boolean decimal = false;
            if (i < s.length() && s.charAt(i) == '.') {
                decimal = true; i++;
                if (i >= s.length() || !Character.isDigit(s.charAt(i))) throw erro("Decimal invalido");
                while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
            }
            if (i < s.length() && (s.charAt(i) == 'e' || s.charAt(i) == 'E')) {
                decimal = true; i++;
                if (i < s.length() && (s.charAt(i) == '+' || s.charAt(i) == '-')) i++;
                if (i >= s.length() || !Character.isDigit(s.charAt(i))) throw erro("Expoente invalido");
                while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
            }
            try {
                if (!decimal) return Long.parseLong(s.substring(inicio, i));
                double v = Double.parseDouble(s.substring(inicio, i));
                if (!Double.isFinite(v)) throw erro("Numero fora do limite");
                return v;
            } catch (NumberFormatException e) { throw erro("Numero fora do limite"); }
        }
    }
}
