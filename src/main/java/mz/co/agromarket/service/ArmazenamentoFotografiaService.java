package mz.co.agromarket.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ArmazenamentoFotografiaService {

    private static final long TAMANHO_MAXIMO = 5L * 1024 * 1024;
    private final Path diretorioRaiz;

    public ArmazenamentoFotografiaService(@Value("${app.upload-dir:uploads}") String diretorio) {
        this.diretorioRaiz = Path.of(diretorio).toAbsolutePath().normalize();
    }

    public String guardar(MultipartFile ficheiro) {
        if (ficheiro == null || ficheiro.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ficheiro está vazio.");
        }
        if (ficheiro.getSize() > TAMANHO_MAXIMO) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Cada fotografia pode ter no máximo 5 MB.");
        }

        String extensao = validarTipoEObterExtensao(ficheiro);
        String nome = UUID.randomUUID() + extensao;
        Path destino = diretorioRaiz.resolve("produtos").resolve(nome).normalize();
        if (!destino.startsWith(diretorioRaiz)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Caminho de ficheiro inválido.");
        }

        try {
            Files.createDirectories(destino.getParent());
            try (InputStream conteudo = ficheiro.getInputStream()) {
                Files.copy(conteudo, destino);
            }
            return "produtos/" + nome;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível guardar a fotografia.", exception);
        }
    }

    public Resource carregar(String caminhoGuardado) {
        Path ficheiro = resolverCaminho(caminhoGuardado);
        try {
            Resource resource = new UrlResource(ficheiro.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fotografia não encontrada.");
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fotografia não encontrada.");
    }

    public void eliminar(String caminhoGuardado) {
        try {
            Files.deleteIfExists(resolverCaminho(caminhoGuardado));
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível eliminar a fotografia.", exception);
        }
    }

    public String obterTipoConteudo(String caminhoGuardado) {
        String caminho = caminhoGuardado.toLowerCase(Locale.ROOT);
        if (caminho.endsWith(".jpg")) return "image/jpeg";
        if (caminho.endsWith(".png")) return "image/png";
        if (caminho.endsWith(".webp")) return "image/webp";
        throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de fotografia não suportado.");
    }

    private String validarTipoEObterExtensao(MultipartFile ficheiro) {
        String mimeType = ficheiro.getContentType();
        try (InputStream input = ficheiro.getInputStream()) {
            byte[] assinatura = input.readNBytes(12);
            if ("image/jpeg".equalsIgnoreCase(mimeType)
                    && assinatura.length >= 3
                    && (assinatura[0] & 0xff) == 0xff
                    && (assinatura[1] & 0xff) == 0xd8
                    && (assinatura[2] & 0xff) == 0xff) {
                return ".jpg";
            }
            if ("image/png".equalsIgnoreCase(mimeType)
                    && assinatura.length >= 8
                    && (assinatura[0] & 0xff) == 0x89
                    && assinatura[1] == 'P' && assinatura[2] == 'N' && assinatura[3] == 'G'
                    && assinatura[4] == 0x0d && assinatura[5] == 0x0a
                    && assinatura[6] == 0x1a && assinatura[7] == 0x0a) {
                return ".png";
            }
            if ("image/webp".equalsIgnoreCase(mimeType)
                    && assinatura.length >= 12
                    && new String(assinatura, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF")
                    && new String(assinatura, 8, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP")) {
                return ".webp";
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não foi possível ler a fotografia.", exception);
        }
        throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Formato não suportado. Envie uma fotografia JPEG, PNG ou WebP válida.");
    }

    private Path resolverCaminho(String caminhoGuardado) {
        Path caminho = diretorioRaiz.resolve(caminhoGuardado).normalize();
        if (!caminho.startsWith(diretorioRaiz)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Caminho de ficheiro inválido.");
        }
        return caminho;
    }
}
