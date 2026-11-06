package com.nobi.appbuilder.generator.resources;

import com.nobi.appbuilder.database.AssetRepository;
import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.xml.XMLIdResolver;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Copies project image assets into res/drawable (images) or
 * src/main/assets (everything else), preserving formats. A missing
 * or unreadable file is a warning, never a failed generation — the
 * layout stage already warned about the unresolved reference.
 */
public class AssetGenerator {

    private final GenerationContext context;

    /** One copied asset: generated path + raw bytes. */
    public static class GeneratedAsset {
        public final String path;
        public final byte[] bytes;

        public GeneratedAsset(String path, byte[] bytes) {
            this.path = path;
            this.bytes = bytes;
        }
    }

    public AssetGenerator(GenerationContext context) {
        this.context = context;
    }

    public List<GeneratedAsset> generate() {
        List<GeneratedAsset> out = new ArrayList<>();
        AssetRepository repository = context.getAssetRepository();
        if (repository == null) {
            return out;
        }
        List<AssetRepository.Asset> assets =
                repository.getByProject(context.getProject().getId());
        for (AssetRepository.Asset asset : assets) {
            if (asset.path == null || asset.path.isEmpty()) {
                continue;
            }
            File file = new File(asset.path);
            if (!file.exists() || !file.isFile()) {
                context.addWarning("resources", null, asset.id,
                        "asset file not found: " + asset.path);
                continue;
            }
            String ext = extensionOf(asset.name);
            String base = XMLIdResolver.sanitizeResourceName(
                    baseNameOf(asset.name));
            String path;
            if (isImageExt(ext)) {
                path = "app/src/main/res/drawable/" + base + "." + ext;
            } else {
                path = "app/src/main/assets/" + base
                        + (ext.isEmpty() ? "" : "." + ext);
            }
            try (FileInputStream in = new FileInputStream(file)) {
                byte[] bytes = new byte[(int) file.length()];
                int read = 0;
                while (read < bytes.length) {
                    int n = in.read(bytes, read, bytes.length - read);
                    if (n < 0) {
                        break;
                    }
                    read += n;
                }
                out.add(new GeneratedAsset(path, bytes));
            } catch (Exception e) {
                context.addWarning("resources", null, asset.id,
                        "could not read asset " + asset.name + ": "
                                + e.getMessage());
            }
        }
        return out;
    }

    private boolean isImageExt(String ext) {
        return "png".equals(ext) || "jpg".equals(ext) || "jpeg".equals(ext)
                || "webp".equals(ext) || "gif".equals(ext);
    }

    private String extensionOf(String name) {
        if (name == null) {
            return "";
        }
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase();
    }

    private String baseNameOf(String name) {
        if (name == null) {
            return "asset";
        }
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }
}
