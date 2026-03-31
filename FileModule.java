import java.util.*;
import java.io.*;
import java.nio.file.*;

public class FileModule {

    public static Map<String, BuiltinFunction> load() {
        Map<String, BuiltinFunction> funcs = new HashMap<>();

        // ============================================================
        // READING
        // ============================================================

        // read(path) — entire file as a single string
        funcs.put("read", (args, line) -> {
            checkArgs("read", 1, args, line);
            String path = requireString("read", args.get(0), line);
            try {
                return new StringValue(Files.readString(Path.of(path)));
            } catch (NoSuchFileException e) {
                throw new NovaRuntimeException("file.read() file not found: '" + path + "'", line);
            } catch (IOException e) {
                throw new NovaRuntimeException("file.read() error: " + e.getMessage(), line);
            }
        });

        // lines(path) — file as array of strings, one per line
        funcs.put("lines", (args, line) -> {
            checkArgs("lines", 1, args, line);
            String path = requireString("lines", args.get(0), line);
            try {
                List<String> fileLines = Files.readAllLines(Path.of(path));
                List<Value> elements = new ArrayList<>();
                for (String l : fileLines)
                    elements.add(new StringValue(l));
                return new ArrayValue(elements);
            } catch (NoSuchFileException e) {
                throw new NovaRuntimeException("file.lines() file not found: '" + path + "'", line);
            } catch (IOException e) {
                throw new NovaRuntimeException("file.lines() error: " + e.getMessage(), line);
            }
        });

        // exists(path) — boolean
        funcs.put("exists", (args, line) -> {
            checkArgs("exists", 1, args, line);
            String path = requireString("exists", args.get(0), line);
            return new BooleanValue(Files.exists(Path.of(path)));
        });

        // ============================================================
        // WRITING
        // ============================================================

        // write(path, content) — write string, overwrites if exists
        funcs.put("write", (args, line) -> {
            checkArgs("write", 2, args, line);
            String path    = requireString("write", args.get(0), line);
            String content = args.get(1).toString();
            try {
                Files.writeString(Path.of(path), content);
                return new BooleanValue(true);
            } catch (IOException e) {
                throw new NovaRuntimeException("file.write() error: " + e.getMessage(), line);
            }
        });

        // append(path, content) — append to end of file
        funcs.put("append", (args, line) -> {
            checkArgs("append", 2, args, line);
            String path    = requireString("append", args.get(0), line);
            String content = args.get(1).toString();
            try {
                Files.writeString(Path.of(path), content,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                return new BooleanValue(true);
            } catch (IOException e) {
                throw new NovaRuntimeException("file.append() error: " + e.getMessage(), line);
            }
        });

        // writeLines(path, arr) — write array of strings one per line
        funcs.put("writeLines", (args, line) -> {
            checkArgs("writeLines", 2, args, line);
            String path = requireString("writeLines", args.get(0), line);
            if (!(args.get(1) instanceof ArrayValue arr))
                throw new TypeError("writeLines() expects an array as second argument", line);
            try {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < arr.length(); i++) {
                    sb.append(arr.get(i, line).toString());
                    if (i < arr.length() - 1) sb.append("\n");
                }
                Files.writeString(Path.of(path), sb.toString());
                return new BooleanValue(true);
            } catch (IOException e) {
                throw new NovaRuntimeException("file.writeLines() error: " + e.getMessage(), line);
            }
        });

        // ============================================================
        // MANAGEMENT
        // ============================================================

        // delete(path) — delete file, returns boolean success
        funcs.put("delete", (args, line) -> {
            checkArgs("delete", 1, args, line);
            String path = requireString("delete", args.get(0), line);
            try {
                return new BooleanValue(Files.deleteIfExists(Path.of(path)));
            } catch (IOException e) {
                throw new NovaRuntimeException("file.delete() error: " + e.getMessage(), line);
            }
        });

        // copy(src, dest) — copy file
        funcs.put("copy", (args, line) -> {
            checkArgs("copy", 2, args, line);
            String src  = requireString("copy", args.get(0), line);
            String dest = requireString("copy", args.get(1), line);
            try {
                Files.copy(Path.of(src), Path.of(dest),
                    StandardCopyOption.REPLACE_EXISTING);
                return new BooleanValue(true);
            } catch (NoSuchFileException e) {
                throw new NovaRuntimeException("file.copy() source not found: '" + src + "'", line);
            } catch (IOException e) {
                throw new NovaRuntimeException("file.copy() error: " + e.getMessage(), line);
            }
        });

        // move(src, dest) — move/rename file
        funcs.put("move", (args, line) -> {
            checkArgs("move", 2, args, line);
            String src  = requireString("move", args.get(0), line);
            String dest = requireString("move", args.get(1), line);
            try {
                Files.move(Path.of(src), Path.of(dest),
                    StandardCopyOption.REPLACE_EXISTING);
                return new BooleanValue(true);
            } catch (NoSuchFileException e) {
                throw new NovaRuntimeException("file.move() source not found: '" + src + "'", line);
            } catch (IOException e) {
                throw new NovaRuntimeException("file.move() error: " + e.getMessage(), line);
            }
        });

        // size(path) — file size in bytes as LongValue
        funcs.put("size", (args, line) -> {
            checkArgs("size", 1, args, line);
            String path = requireString("size", args.get(0), line);
            try {
                return new LongValue(Files.size(Path.of(path)));
            } catch (NoSuchFileException e) {
                throw new NovaRuntimeException("file.size() file not found: '" + path + "'", line);
            } catch (IOException e) {
                throw new NovaRuntimeException("file.size() error: " + e.getMessage(), line);
            }
        });

        // name(path) — filename from path e.g. "code.txt"
        funcs.put("name", (args, line) -> {
            checkArgs("name", 1, args, line);
            String path = requireString("name", args.get(0), line);
            Path p = Path.of(path).getFileName();
            return p != null ? new StringValue(p.toString()) : new StringValue("");
        });

        // extension(path) — file extension e.g. "txt"
        funcs.put("extension", (args, line) -> {
            checkArgs("extension", 1, args, line);
            String path = requireString("extension", args.get(0), line);
            String fname = Path.of(path).getFileName().toString();
            int dot = fname.lastIndexOf('.');
            return dot >= 0 ? new StringValue(fname.substring(dot + 1)) : new StringValue("");
        });

        // parent(path) — parent directory path
        funcs.put("parent", (args, line) -> {
            checkArgs("parent", 1, args, line);
            String path = requireString("parent", args.get(0), line);
            Path p = Path.of(path).getParent();
            return p != null ? new StringValue(p.toString()) : new StringValue("");
        });

        // ============================================================
        // DIRECTORY
        // ============================================================

        // mkdir(path) — create directory
        funcs.put("mkdir", (args, line) -> {
            checkArgs("mkdir", 1, args, line);
            String path = requireString("mkdir", args.get(0), line);
            try {
                Files.createDirectories(Path.of(path));
                return new BooleanValue(true);
            } catch (IOException e) {
                throw new NovaRuntimeException("file.mkdir() error: " + e.getMessage(), line);
            }
        });

        // listFiles(path) — array of filenames in directory
        funcs.put("listFiles", (args, line) -> {
            checkArgs("listFiles", 1, args, line);
            String path = requireString("listFiles", args.get(0), line);
            try {
                List<Value> names = new ArrayList<>();
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(path))) {
                    for (Path entry : stream)
                        names.add(new StringValue(entry.getFileName().toString()));
                }
                return new ArrayValue(names);
            } catch (NotDirectoryException e) {
                throw new NovaRuntimeException("file.listFiles() path is not a directory: '" + path + "'", line);
            } catch (IOException e) {
                throw new NovaRuntimeException("file.listFiles() error: " + e.getMessage(), line);
            }
        });

        // isDir(path) — boolean
        funcs.put("isDir", (args, line) -> {
            checkArgs("isDir", 1, args, line);
            String path = requireString("isDir", args.get(0), line);
            return new BooleanValue(Files.isDirectory(Path.of(path)));
        });

        return funcs;
    }

    private static void checkArgs(String name, int expected, List<Value> args, int line) {
        if (args.size() != expected)
            throw new ArgumentException(name, expected, args.size(), line);
    }

    private static String requireString(String name, Value v, int line) {
        if (!(v instanceof StringValue))
            throw new TypeError(name + "() expects a string path but got " + v.getTypeName(), line);
        return ((StringValue) v).getRaw();
    }
}