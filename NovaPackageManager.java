import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.net.URI;
import java.net.http.*;
import java.time.*;

/**
 * NovaPackageManager — manages Nova modules/packages.
 *
 * A "package" is a .nova file (or folder of .nova files) that can be
 * installed into the Nova packages directory and then loaded with:
 *
 *     use packageName
 *
 * Package registry:
 *   Local:  ~/.nova/packages/
 *   Remote: packages are fetched from a GitHub raw URL or a direct URL
 *           to a .nova file.
 *
 * Commands:
 *   nova install <url>          Install a package from a URL or GitHub path
 *   nova install <name> <path>  Install a local file as a named package
 *   nova remove  <name>         Remove an installed package
 *   nova list                   List all installed packages
 *   nova info    <name>         Show info about an installed package
 *   nova publish <file>         Print instructions for sharing a package
 */
public class NovaPackageManager {

    static final Path NOVA_HOME    = Path.of(System.getProperty("user.home"), ".nova");
    static final Path PACKAGES_DIR = NOVA_HOME.resolve("packages");
    static final Path REGISTRY     = NOVA_HOME.resolve("registry.txt");

    // ── Entry points ──────────────────────────────────────────────────────────

    public static void install(String[] args) {
        ensurePackagesDir();

        if (args.length == 1) {
            System.err.println("Usage: nova install <url>  OR  nova install <name> <local-path>");
            System.exit(1);
        }

        String target = args[1];

        if (args.length >= 3) {
            // nova install mylib ./mylib.nova  — install local file
            installLocal(target, Path.of(args[2]));
        } else if (target.startsWith("http://") || target.startsWith("https://")) {
            // nova install https://...  — install from URL
            installFromUrl(target);
        } else if (target.contains("/")) {
            // nova install user/repo/file.nova  — GitHub shorthand
            String url = "https://raw.githubusercontent.com/" + target;
            installFromUrl(url);
        } else {
            System.err.println("Cannot resolve '" + target + "' as a package.");
            System.err.println("Provide a URL, GitHub path (user/repo/file.nova), or local path.");
            System.exit(1);
        }
    }

    public static void remove(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: nova remove <package-name>");
            System.exit(1);
        }
        String name = args[1];
        Path pkg = PACKAGES_DIR.resolve(name + ".nova");
        if (!Files.exists(pkg)) {
            System.err.println("Package '" + name + "' is not installed.");
            System.exit(1);
        }
        try {
            Files.delete(pkg);
            removeFromRegistry(name);
            System.out.println("Removed: " + name);
        } catch (IOException e) {
            System.err.println("Error removing package: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void list(String[] args) {
        ensurePackagesDir();
        try (var stream = Files.list(PACKAGES_DIR)) {
            List<Path> packages = stream
                .filter(p -> p.toString().endsWith(".nova"))
                .sorted()
                .toList();

            if (packages.isEmpty()) {
                System.out.println("No packages installed.");
                System.out.println("Install one with:  nova install <url>");
                return;
            }

            System.out.println("Installed packages  (" + PACKAGES_DIR + "):");
            System.out.println();
            for (Path p : packages) {
                String name = p.getFileName().toString().replace(".nova", "");
                long   size = Files.size(p);
                String date = Files.getLastModifiedTime(p).toInstant()
                                   .atZone(ZoneId.systemDefault())
                                   .toLocalDate().toString();
                System.out.printf("  %-20s  %5d bytes  installed %s%n", name, size, date);
            }
            System.out.println();
            System.out.println("Use with:  use <package-name>");
        } catch (IOException e) {
            System.err.println("Error listing packages: " + e.getMessage());
        }
    }

    public static void info(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: nova info <package-name>");
            System.exit(1);
        }
        String name = args[1];
        Path pkg = PACKAGES_DIR.resolve(name + ".nova");
        if (!Files.exists(pkg)) {
            System.err.println("Package '" + name + "' is not installed.");
            System.exit(1);
        }
        try {
            long   size  = Files.size(pkg);
            String date  = Files.getLastModifiedTime(pkg).toInstant()
                                .atZone(ZoneId.systemDefault()).toString();
            String source = readRegistryEntry(name);
            String code  = Files.readString(pkg);
            long   lines = code.lines().count();
            long   fns   = code.lines().filter(l -> l.trim().startsWith("fn ")).count();

            System.out.println("Package:  " + name);
            System.out.println("Path:     " + pkg);
            System.out.println("Size:     " + size + " bytes");
            System.out.println("Lines:    " + lines);
            System.out.println("Functions:" + fns);
            System.out.println("Installed:" + date);
            if (source != null) System.out.println("Source:   " + source);
            System.out.println();
            System.out.println("Use with:  use " + name);
        } catch (IOException e) {
            System.err.println("Error reading package info: " + e.getMessage());
        }
    }

    public static void publish(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: nova publish <file.nova>");
            System.exit(1);
        }
        String file = args[1];
        System.out.println("""
            To share your Nova package:

            Option 1 — GitHub (recommended):
              1. Create a public GitHub repository
              2. Upload your .nova file to it
              3. Others can install it with:
                 nova install <your-github-username>/<repo>/<file.nova>

            Option 2 — Direct URL:
              1. Host your .nova file anywhere publicly accessible
              2. Others can install it with:
                 nova install https://example.com/yourpackage.nova

            Option 3 — Local sharing:
              1. Share the .nova file directly
              2. Recipients install it with:
                 nova install <name> <path-to-file.nova>

            Your file: """ + file + """

            Make sure your package:
              - Has a clear name (the filename becomes the package name)
              - Uses 'fn' to define public functions
              - Has a comment at the top explaining what it does
              - Only uses standard Nova syntax and built-in modules
            """);
    }

    // ── Install helpers ───────────────────────────────────────────────────────

    private static void installLocal(String name, Path source) {
        if (!Files.exists(source)) {
            System.err.println("File not found: " + source);
            System.exit(1);
        }
        if (!name.endsWith(".nova")) name = name;
        Path dest = PACKAGES_DIR.resolve(name + ".nova");
        try {
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
            addToRegistry(name, "local:" + source.toAbsolutePath());
            System.out.println("Installed: " + name);
            System.out.println("Use with:  use " + name);
        } catch (IOException e) {
            System.err.println("Error installing package: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void installFromUrl(String url) {
        // Derive package name from URL
        String filename = url.substring(url.lastIndexOf('/') + 1);
        String name = filename.endsWith(".nova")
            ? filename.substring(0, filename.length() - 5)
            : filename;

        System.out.println("Fetching: " + url);

        try {
            HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Failed to fetch package (HTTP " + response.statusCode() + ")");
                System.exit(1);
            }

            String code = response.body();

            // Basic validation — must parse as Nova
            try {
                Lexer lexer = new Lexer(code);
                List<Token> tokens = lexer.tokenize();
                new Parser(tokens).parse();
            } catch (NovaException e) {
                System.err.println("Warning: package has syntax errors: " + e.getMessage());
                System.err.println("Installing anyway — it may work at runtime.");
            }

            Path dest = PACKAGES_DIR.resolve(name + ".nova");
            Files.writeString(dest, code);
            addToRegistry(name, url);

            System.out.println("Installed: " + name);
            System.out.println("Use with:  use " + name);

        } catch (IOException | InterruptedException e) {
            System.err.println("Network error: " + e.getMessage());
            System.exit(1);
        }
    }

    // ── Registry helpers ──────────────────────────────────────────────────────

    private static void ensurePackagesDir() {
        try {
            Files.createDirectories(PACKAGES_DIR);
        } catch (IOException e) {
            System.err.println("Cannot create packages directory: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void addToRegistry(String name, String source) throws IOException {
        // registry.txt: name=source, one per line
        Map<String, String> entries = loadRegistry();
        entries.put(name, source);
        saveRegistry(entries);
    }

    private static void removeFromRegistry(String name) throws IOException {
        Map<String, String> entries = loadRegistry();
        entries.remove(name);
        saveRegistry(entries);
    }

    private static String readRegistryEntry(String name) {
        try {
            return loadRegistry().get(name);
        } catch (IOException e) {
            return null;
        }
    }

    private static Map<String, String> loadRegistry() throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        if (!Files.exists(REGISTRY)) return map;
        for (String line : Files.readAllLines(REGISTRY)) {
            int eq = line.indexOf('=');
            if (eq > 0) map.put(line.substring(0, eq), line.substring(eq + 1));
        }
        return map;
    }

    private static void saveRegistry(Map<String, String> entries) throws IOException {
        StringBuilder sb = new StringBuilder();
        entries.forEach((k, v) -> sb.append(k).append('=').append(v).append('\n'));
        Files.writeString(REGISTRY, sb.toString());
    }
}
