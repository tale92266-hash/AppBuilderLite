package com.nobi.appbuilder.assets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates asset files by magic bytes and size before they are
 * imported. A file that fails validation is reported, never
 * imported as something it is not.
 */
public final class AssetValidationManager {

    public static final long MAX_FILE_BYTES = 50L * 1024 * 1024;

    private AssetValidationManager() {
    }

    public static List<String> validateFile(File file, AssetType expected) {
        List<String> errors = new ArrayList<>();
        if (file == null || !file.exists()) {
            errors.add("File does not exist");
            return errors;
        }
        if (file.length() > MAX_FILE_BYTES) {
            errors.add("File is larger than 50 MB");
        }
        if (expected == AssetType.IMAGE) {
            if (!isImage(file)) {
                errors.add("Not a supported image (PNG, JPEG, WebP, GIF)");
            }
        } else if (expected == AssetType.FONT) {
            if (!isFont(file)) {
                errors.add("Not a supported font (TTF, OTF)");
            }
        }
        return errors;
    }

    public static boolean isImage(File file) {
        byte[] magic = readMagic(file, 12);
        if (magic == null) {
            return false;
        }
        if (magic.length >= 8 && magic[0] == (byte) 0x89 && magic[1] == 'P'
                && magic[2] == 'N' && magic[3] == 'G') {
            return true;
        }
        if (magic.length >= 3 && magic[0] == (byte) 0xFF
                && magic[1] == (byte) 0xD8 && magic[2] == (byte) 0xFF) {
            return true;
        }
        if (magic.length >= 12 && magic[0] == 'R' && magic[1] == 'I'
                && magic[2] == 'F' && magic[3] == 'F'
                && magic[8] == 'W' && magic[9] == 'E'
                && magic[10] == 'B' && magic[11] == 'P') {
            return true;
        }
        return magic.length >= 6 && magic[0] == 'G' && magic[1] == 'I'
                && magic[2] == 'F' && magic[3] == '8';
    }

    public static boolean isFont(File file) {
        byte[] magic = readMagic(file, 4);
        if (magic == null || magic.length < 4) {
            return false;
        }
        boolean ttf = magic[0] == 0x00 && magic[1] == 0x01
                && magic[2] == 0x00 && magic[3] == 0x00;
        boolean otf = magic[0] == 'O' && magic[1] == 'T'
                && magic[2] == 'T' && magic[3] == 'O';
        return ttf || otf;
    }

    /** Heuristic animated-GIF check: more than one image separator. */
    public static boolean isAnimatedGif(File file) {
        if (file == null || !file.exists() || file.length() > 10 * 1024 * 1024) {
            return false;
        }
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] head = new byte[6];
            if (in.read(head) < 6 || head[0] != 'G' || head[1] != 'I'
                    || head[2] != 'F') {
                return false;
            }
            int separators = 0;
            int b;
            while ((b = in.read()) != -1) {
                if (b == 0x2C) {
                    separators++;
                    if (separators > 1) {
                        return true;
                    }
                }
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    private static byte[] readMagic(File file, int count) {
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] magic = new byte[count];
            int read = in.read(magic);
            if (read < 0) {
                return null;
            }
            return magic;
        } catch (IOException e) {
            return null;
        }
    }
}
