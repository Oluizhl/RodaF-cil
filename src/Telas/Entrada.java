package Telas;

import Clientes.Pessoa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;

// lê o que o usuário digita sem travar o programa:
// se ele digitar errado (letra no lugar de número, data inválida), mostra o erro e pede de novo
public class Entrada {
    // "uuuu" + STRICT faz o Java recusar datas que não existem, como 31/02/2026
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private static Scanner scanner = new Scanner(System.in);

    public static String lerTexto(String pergunta){
        while (true){
            System.out.print(pergunta + ": ");
            String texto = scanner.nextLine().trim();
            if (!texto.isEmpty()){
                return texto;
            }
            System.out.println("ERRO: Esse campo não pode ficar vazio!");
        }
    }

    public static int lerInteiro(String pergunta){
        while (true){
            String texto = lerTexto(pergunta);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e){
                System.out.println("ERRO: Digite um número inteiro (ex.: 3).");
            }
        }
    }

    // inteiro dentro de uma faixa (usado nos menus)
    public static int lerInteiro(String pergunta, int minimo, int maximo){
        while (true){
            int numero = lerInteiro(pergunta);
            if (numero >= minimo && numero <= maximo){
                return numero;
            }
            System.out.println("ERRO: Digite um número de " + minimo + " a " + maximo + ".");
        }
    }

    // aceita vírgula ou ponto (120,50 ou 120.50)
    public static double lerDecimal(String pergunta){
        while (true){
            String texto = lerTexto(pergunta).replace(",", ".");
            try {
                double numero = Double.parseDouble(texto);
                if (numero >= 0){
                    return numero;
                }
                System.out.println("ERRO: O valor não pode ser negativo!");
            } catch (NumberFormatException e){
                System.out.println("ERRO: Digite um número (ex.: 120,50).");
            }
        }
    }

    public static LocalDate lerData(String pergunta){
        while (true){
            String texto = lerTexto(pergunta + " (dd/mm/aaaa)");
            try {
                return LocalDate.parse(texto, FORMATO_DATA);
            } catch (DateTimeParseException e){
                System.out.println("ERRO: Data inválida! Use o formato dd/mm/aaaa (ex.: 25/12/2026).");
            }
        }
    }

    public static LocalDateTime lerDataHora(String pergunta){
        while (true){
            String texto = lerTexto(pergunta + " (dd/mm/aaaa hh:mm)");
            try {
                return LocalDateTime.parse(texto, FORMATO_DATA_HORA);
            } catch (DateTimeParseException e){
                System.out.println("ERRO: Data inválida! Use o formato dd/mm/aaaa hh:mm (ex.: 25/12/2026 09:30).");
            }
        }
    }

    public static boolean lerSimNao(String pergunta){
        while (true){
            String texto = lerTexto(pergunta + " (s/n)").toLowerCase();
            if (texto.equals("s") || texto.equals("sim")){
                return true;
            }
            if (texto.equals("n") || texto.equals("nao") || texto.equals("não")){
                return false;
            }
            System.out.println("ERRO: Responda com s ou n.");
        }
    }

    // ---------- dados de pessoas (usam as regras da Parte 2) ----------

    public static String lerCpf(String pergunta){
        while (true){
            String cpf = lerTexto(pergunta);
            if (Pessoa.cpfValido(cpf)){
                return Pessoa.apenasDigitos(cpf);
            }
            System.out.println("ERRO: CPF inválido! Confira os números.");
        }
    }

    public static String lerTelefone(String pergunta){
        while (true){
            String telefone = Pessoa.apenasDigitos(lerTexto(pergunta + " (DDD + número)"));
            if (telefone.length() == 10 || telefone.length() == 11){
                return telefone;
            }
            System.out.println("ERRO: Telefone inválido! Use DDD + número (ex.: 86999998888).");
        }
    }

    public static String lerEmail(String pergunta){
        while (true){
            String email = lerTexto(pergunta);
            if (email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")){
                return email;
            }
            System.out.println("ERRO: E-mail inválido! (ex.: nome@email.com)");
        }
    }

    public static void pausar(){
        System.out.print("\nPressione ENTER para continuar...");
        scanner.nextLine();
    }
}
