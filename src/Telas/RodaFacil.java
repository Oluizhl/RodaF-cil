package Telas;

import java.nio.file.Path;

public class RodaFacil {
    public static void main(String[] args){
        Sistema sistema = new Sistema(Path.of("dados"));
        sistema.carregar();

        new MenuPrincipal(sistema).abrir();

        sistema.salvar();
        System.out.println("Dados salvos. Até logo!");
    }
}
