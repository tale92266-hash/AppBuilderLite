package com.nobi.appbuilder.build.cloud;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Minimal GitHub REST client for cloud builds: user, repos, file
 * contents, workflow dispatch, runs, artifacts. Pure Java — no
 * Android imports — so it can be exercised on a JVM too. Every
 * call throws IOException with the HTTP status and the server's
 * message; a 404 is returned as null where the caller expects
 * "not found" instead of a failure.
 *
 * Note: Android's org.json.JSONException is a CHECKED exception
 * (unlike Maven's org.json, where it is unchecked), so every JSON
 * operation below is wrapped into IOException — the client's only
 * failure type.
 */
public class GitHubClient {

    private static final String API = "https://api.github.com";
    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 60000;

    private final String token;

    public GitHubClient(String token) {
        this.token = token;
    }

    /** The login of the token owner, or throws when invalid. */
    public String getUserLogin() throws IOException {
        try {
            JSONObject user = new JSONObject(get("/user"));
            return user.getString("login");
        } catch (JSONException e) {
            throw new IOException("Invalid GitHub response", e);
        }
    }

    /** Creates the private build repo (auto-initialised), or null
     * when it already exists (422 name already exists). */
    public JSONObject createRepo(String name) throws IOException {
        try {
            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("private", true);
            body.put("auto_init", true);
            body.put("description",
                    "AppBuilder Lite cloud builds — project ZIPs built by "
                            + "GitHub Actions. Created by the app.");
            try {
                return new JSONObject(post("/user/repos", body.toString()));
            } catch (HttpException e) {
                if (e.status == 422) {
                    return null;
                }
                throw e;
            }
        } catch (JSONException e) {
            throw new IOException("Invalid GitHub response", e);
        }
    }

    public JSONObject getRepo(String owner, String repo) throws IOException {
        try {
            String out = getOrNull("/repos/" + owner + "/" + repo);
            return out == null ? null : new JSONObject(out);
        } catch (JSONException e) {
            throw new IOException("Invalid GitHub response", e);
        }
    }

    /** Default branch of a repo ("main" when unknown). */
    public String defaultBranch(String owner, String repo)
            throws IOException {
        // optString never throws; getRepo only throws IOException.
        JSONObject found = getRepo(owner, repo);
        if (found == null) {
            return "main";
        }
        return found.optString("default_branch", "main");
    }

    /** SHA of a file in the repo, or null when it does not exist. */
    public String getFileSha(String owner, String repo, String path)
            throws IOException {
        try {
            String out = getOrNull("/repos/" + owner + "/" + repo
                    + "/contents/" + path);
            if (out == null) {
                return null;
            }
            return new JSONObject(out).optString("sha", null);
        } catch (JSONException e) {
            throw new IOException("Invalid GitHub response", e);
        }
    }

    /**
     * Creates or updates a file. Returns the commit SHA of the
     * change. Content is base64 (Contents API, 100 MB limit).
     */
    public String putFile(String owner, String repo, String path,
                          byte[] content, String message)
            throws IOException {
        try {
            String sha = getFileSha(owner, repo, path);
            JSONObject body = new JSONObject();
            body.put("message", message);
            body.put("content",
                    Base64.getEncoder().encodeToString(content));
            if (sha != null) {
                body.put("sha", sha);
            }
            JSONObject out = new JSONObject(put(
                    "/repos/" + owner + "/" + repo + "/contents/" + path,
                    body.toString()));
            return out.getJSONObject("commit").getString("sha");
        } catch (JSONException e) {
            throw new IOException("Invalid GitHub response", e);
        }
    }

    /** Triggers a workflow_dispatch run on the given branch. */
    public void dispatchWorkflow(String owner, String repo,
                                 String workflowFile, String ref)
            throws IOException {
        try {
            JSONObject body = new JSONObject();
            body.put("ref", ref);
            post("/repos/" + owner + "/" + repo + "/actions/workflows/"
                    + workflowFile + "/dispatches", body.toString());
        } catch (JSONException e) {
            throw new IOException("Invalid GitHub response", e);
        }
    }

    /** Latest run of a workflow with this head SHA, or null. */
    public JSONObject findRunByHeadSha(String owner, String repo,
                                       String workflowFile, String headSha)
            throws IOException {
        try {
            JSONObject runs = new JSONObject(get(
                    "/repos/" + owner + "/" + repo + "/actions/workflows/"
                            + workflowFile + "/runs?per_page=10"));
            JSONArray list = runs.optJSONArray("workflow_runs");
            if (list == null) {
                return null;
            }
            for (int i = 0; i < list.length(); i++) {
                JSONObject run = list.getJSONObject(i);
                if (headSha.equals(run.optString("head_sha"))) {
                    return run;
                }
            }
            return null;
        } catch (JSONException e) {
            throw new IOException("Invalid GitHub response", e);
        }
    }

    public JSONObject getRun(String owner, String repo, long runId)
            throws IOException {
        try {
            return new JSONObject(get(
                    "/repos/" + owner + "/" + repo + "/actions/runs/" + runId));
        } catch (JSONException e) {
            throw new IOException("Invalid GitHub response", e);
        }
    }

    public JSONArray listArtifacts(String owner, String repo, long runId)
            throws IOException {
        try {
            JSONObject out = new JSONObject(get(
                    "/repos/" + owner + "/" + repo + "/actions/runs/" + runId
                            + "/artifacts?per_page=10"));
            return out.optJSONArray("artifacts");
        } catch (JSONException e) {
            throw new IOException("Invalid GitHub response", e);
        }
    }

    /** Downloads a URL (follows the artifact redirect) to a file. */
    public long downloadTo(String url, File dest) throws IOException {
        HttpURLConnection conn = open(url, "GET");
        conn.setReadTimeout(120000);
        int status = conn.getResponseCode();
        if (status < 200 || status >= 300) {
            throw new HttpException(status, readBody(conn));
        }
        long total = 0;
        try (InputStream in = conn.getInputStream();
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                total += read;
            }
        } finally {
            conn.disconnect();
        }
        return total;
    }

    private String get(String path) throws IOException {
        HttpURLConnection conn = open(API + path, "GET");
        int status = conn.getResponseCode();
        String body = readBody(conn);
        conn.disconnect();
        if (status < 200 || status >= 300) {
            throw new HttpException(status, body);
        }
        return body;
    }

    /** GET that returns null on 404 instead of throwing. */
    private String getOrNull(String path) throws IOException {
        HttpURLConnection conn = open(API + path, "GET");
        int status = conn.getResponseCode();
        if (status == 404) {
            conn.disconnect();
            return null;
        }
        String body = readBody(conn);
        conn.disconnect();
        if (status < 200 || status >= 300) {
            throw new HttpException(status, body);
        }
        return body;
    }

    private String post(String path, String json) throws IOException {
        return write("POST", path, json);
    }

    private String put(String path, String json) throws IOException {
        return write("PUT", path, json);
    }

    private String write(String method, String path, String json)
            throws IOException {
        HttpURLConnection conn = open(API + path, method);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream out = conn.getOutputStream()) {
            out.write(bytes);
        }
        int status = conn.getResponseCode();
        String body = readBody(conn);
        conn.disconnect();
        if (status < 200 || status >= 300) {
            throw new HttpException(status, body);
        }
        return body;
    }

    private HttpURLConnection open(String url, String method)
            throws IOException {
        HttpURLConnection conn =
                (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
        conn.setRequestProperty("User-Agent", "AppBuilderLite-CloudBuild");
        return conn;
    }

    private String readBody(HttpURLConnection conn) throws IOException {
        InputStream in = null;
        try {
            int status = conn.getResponseCode();
            in = status >= 400 ? conn.getErrorStream()
                    : conn.getInputStream();
            if (in == null) {
                return "";
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            return "";
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /** HTTP failure with status, so callers can react to 422/404. */
    public static class HttpException extends IOException {
        public final int status;

        public HttpException(int status, String body) {
            super("GitHub API " + status + ": " + shortBody(body));
            this.status = status;
        }

        private static String shortBody(String body) {
            if (body == null) {
                return "";
            }
            try {
                JSONObject json = new JSONObject(body);
                if (json.has("message")) {
                    return json.getString("message");
                }
            } catch (Exception ignored) {
            }
            return body.length() > 200 ? body.substring(0, 200) : body;
        }
    }
}
