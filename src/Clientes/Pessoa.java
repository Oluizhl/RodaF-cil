package Clientes;

public class Pessoa {
    private String nome;
    private String cpf; // guardado só com números; só muda depois de validado (encapsulamento)
    private String telefone;
    private String email;

    public Pessoa(String nome, String cpf, String telefone, String email){
        setNome(nome);
        setCpf(cpf);
        setTelefone(telefone);
        setEmail(email);
    }

    public String getNome(){
        return nome;
    }

    public void setNome(String nome){
        if (nome != null && !nome.trim().isEmpty()){
            this.nome = nome.trim();
        } else {
            System.out.println("ERRO: O nome não pode ficar vazio!");
        }
    }

    public String getCpf(){
        return cpf;
    }

    public String getCpfFormatado(){
        if (cpf == null){
            return "";
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    public void setCpf(String cpf){
        if (cpfValido(cpf)){
            this.cpf = apenasDigitos(cpf);
        } else {
            System.out.println("ERRO: CPF inválido!");
        }
    }

    public String getTelefone(){
        return telefone;
    }

    public void setTelefone(String telefone){
        String digitos = apenasDigitos(telefone);
        if (digitos.length() == 10 || digitos.length() == 11){ // com DDD
            this.telefone = digitos;
        } else {
            System.out.println("ERRO: Telefone inválido! Use DDD + número.");
        }
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        if (email != null && email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")){
            this.email = email;
        } else {
            System.out.println("ERRO: E-mail inválido!");
        }
    }

    // ---------- métodos de apoio (static: não dependem de um objeto) ----------

    public static String apenasDigitos(String texto){
        if (texto == null){
            return "";
        }
        return texto.replaceAll("[^0-9]", "");
    }

    public static boolean cpfValido(String cpf){
        String d = apenasDigitos(cpf);

        if (d.length() != 11){
            return false;
        }

        // 111.111.111-11, 222.222.222-22... passam na conta, mas não são válidos
        if (d.matches("(\\d)\\1{10}")){
            return false;
        }

        return calcularDigito(d, 9) == (d.charAt(9) - '0')
                && calcularDigito(d, 10) == (d.charAt(10) - '0');
    }

    // calcula o dígito verificador usando os 'quantidade' primeiros números
    private static int calcularDigito(String d, int quantidade){
        int soma = 0;
        int peso = quantidade + 1;

        for (int i = 0; i < quantidade; i++){
            soma += (d.charAt(i) - '0') * peso;
            peso--;
        }

        int resto = (soma * 10) % 11;
        if (resto == 10){
            resto = 0;
        }
        return resto;
    }
}
