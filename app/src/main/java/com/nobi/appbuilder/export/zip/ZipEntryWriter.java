package com.nobi.appbuilder.export.zip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Writes ZIP entries with a deterministic naming strategy: forward
 * slashes, no leading slash, no "..", no duplicates (a duplicate is
 * an error, never silently overwritten). Directories get explicit
 * entries so empty folders survive extraction.
 */
public class ZipEntryWriter {

    private final ZipOutputStream out;
    private final Set<String> written = new HashSet<>();
    private int entryCount;

    public ZipEntryWriter(ZipOutputStream out) {
        this.out = out;
    }

    /** Normalizes an entry name; null when unsafe. */
    public static String safeEntryName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        String normalized = name.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.contains("..") || normalized.contains("\0")) {
            return null;
        }
        while (normalized.contains("//")) {
            normalized = normalized.replace("//", "/");
        }
        return normalized;
    }

    public void addDirectory(String name) throws IOException {
        String safe = safeEntryName(name);
        if (safe == null) {
            throw new IOException("Unsafe ZIP entry name: " + name);
        }
        if (!safe.endsWith("/")) {
            safe = safe + "/";
        }
        if (!written.add(safe)) {
            throw new IOException("Duplicate ZIP entry: " + safe);
        }
        out.putNextEntry(new ZipEntry(safe));
        out.closeEntry();
        entryCount++;
    }

    public void addText(String name, String content) throws IOException {
        byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        addBytes(name, bytes);
    }

    public void addBytes(String name, byte[] content) throws IOException {
        addStream(name, new ByteArrayInputStream(content), content.length);
    }

    public void addStream(String name, InputStream in, long size)
            throws IOException {
        String safe = safeEntryName(name);
        if (safe == null) {
            throw new IOException("Unsafe ZIP entry name: " + name);
        }
        if (!written.add(safe)) {
            throw new IOException("Duplicate ZIP entry: " + safe);
        }
        out.putNextEntry(new ZipEntry(safe));
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.closeEntry();
        entryCount++;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public boolean hasWritten(String name) {
        return written.contains(safeEntryName(name));
    }
}
