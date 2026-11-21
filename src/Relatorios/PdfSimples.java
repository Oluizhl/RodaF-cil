package Relatorios;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class PdfSimples {
    private static final int LARGURA = 595;
    private static final int ALTURA = 842;
    private static final int MARGEM = 50;
    private static final int ENTRELINHA = 15;
    private static final int MAX_CARACTERES = 88;
    private static final Charset ACENTOS = Charset.forName("windows-1252");

    private ArrayList<String[]> linhas;

    public PdfSimples(){
        linhas = new ArrayList<String[]>();
    }

    public void titulo(String texto){
        linhas.add(new String[]{"F2", "16", texto});
        espaco();
    }

    public void secao(String texto){
        espaco();
        linhas.add(new String[]{"F2", "12", texto});
    }

    public void linha(String texto){
        while (texto.length() > MAX_CARACTERES){
            int corte = texto.lastIndexOf(' ', MAX_CARACTERES);
            if (corte <= 0){
                corte = MAX_CARACTERES;
            }
            linhas.add(new String[]{"F1", "11", texto.substring(0, corte)});
            texto = "   " + texto.substring(corte).trim();
        }
        linhas.add(new String[]{"F1", "11", texto});
    }

    public void negrito(String texto){
        linhas.add(new String[]{"F2", "11", texto});
    }

    public void espaco(){
        linhas.add(new String[]{"F1", "11", ""});
    }

    public void salvar(Path arquivo) throws IOException {
        int linhasPorPagina = (ALTURA - 2 * MARGEM) / ENTRELINHA;
        ArrayList<String> conteudos = new ArrayList<String>();
        StringBuilder pagina = null;
        int y = 0;

        for (int i = 0; i < linhas.size(); i++){
            if (i % linhasPorPagina == 0){
                if (pagina != null){
                    conteudos.add(pagina.toString());
                }
                pagina = new StringBuilder();
                y = ALTURA - MARGEM;
            }
            String[] l = linhas.get(i);
            pagina.append("BT /").append(l[0]).append(" ").append(l[1]).append(" Tf ")
                    .append(MARGEM).append(" ").append(y).append(" Td (").append(escapar(l[2])).append(") Tj ET\n");
            y -= ENTRELINHA;
        }
        if (pagina != null){
            conteudos.add(pagina.toString());
        }

        ArrayList<String> objetos = new ArrayList<String>();
        String filhos = "";
        for (int i = 0; i < conteudos.size(); i++){
            filhos += (5 + i * 2) + " 0 R ";
        }
        objetos.add("<< /Type /Catalog /Pages 2 0 R >>");
        objetos.add("<< /Type /Pages /Kids [" + filhos + "] /Count " + conteudos.size() + " >>");
        objetos.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
        objetos.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");
        for (int i = 0; i < conteudos.size(); i++){
            int numeroConteudo = 6 + i * 2;
            objetos.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + LARGURA + " " + ALTURA + "]"
                    + " /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + numeroConteudo + " 0 R >>");
            byte[] bytes = conteudos.get(i).getBytes(ACENTOS);
            objetos.add("<< /Length " + bytes.length + " >>\nstream\n" + conteudos.get(i) + "endstream");
        }

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        escrever(saida, "%PDF-1.4\n");
        ArrayList<Integer> posicoes = new ArrayList<Integer>();
        for (int i = 0; i < objetos.size(); i++){
            posicoes.add(saida.size());
            escrever(saida, (i + 1) + " 0 obj\n" + objetos.get(i) + "\nendobj\n");
        }

        int inicioXref = saida.size();
        escrever(saida, "xref\n0 " + (objetos.size() + 1) + "\n0000000000 65535 f \n");
        for (int posicao : posicoes){
            escrever(saida, String.format("%010d 00000 n \n", posicao));
        }
        escrever(saida, "trailer\n<< /Size " + (objetos.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + inicioXref + "\n%%EOF\n");

        if (arquivo.getParent() != null){
            Files.createDirectories(arquivo.getParent());
        }
        Files.write(arquivo, saida.toByteArray());
    }

    private void escrever(ByteArrayOutputStream saida, String texto){
        byte[] bytes = texto.getBytes(ACENTOS);
        saida.write(bytes, 0, bytes.length);
    }

    private String escapar(String texto){
        return texto.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
