package Persistencia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

/**
 * Backup OPCIONAL em uma pasta sincronizada pelo Google Drive para computador.
 * Nao usa a API do Google. Sem a pasta configurada, o sistema funciona normalmente.
 */
public class BackupDrive {
    private final Path pastaSincronizada;

    public BackupDrive(Path pastaSincronizada) { this.pastaSincronizada = pastaSincronizada; }

    public static BackupDrive daVariavelDeAmbiente() {
        String configuracao = System.getenv("RODAFACIL_DRIVE_DIR");
        return new BackupDrive(configuracao == null || configuracao.isBlank() ? null : Path.of(configuracao));
    }

    /** @return true se realizou a copia, false se nao esta configurado. */
    public boolean fazerBackup(Path pastaDados) throws IOException {
        if (pastaSincronizada == null) return false;
        Path origem = pastaDados.toAbsolutePath().normalize();
        Path destino = pastaSincronizada.toAbsolutePath().normalize();
        if (destino.startsWith(origem)) throw new IOException("Pasta de backup nao pode ficar dentro de dados");
        if (!Files.isDirectory(origem)) throw new IOException("Pasta de dados nao encontrada: " + origem);
        Files.createDirectories(destino);
        try (Stream<Path> arquivos = Files.list(origem)) {
            for (Path arquivo : (Iterable<Path>) arquivos.filter(Files::isRegularFile)::iterator) {
                Files.copy(arquivo, destino.resolve(arquivo.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return true;
    }
}
