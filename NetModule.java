import java.util.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.io.*;

public class NetModule {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    public static Map<String, BuiltinFunction> load() {
        Map<String, BuiltinFunction> funcs = new HashMap<>();

        // get(url)
        funcs.put("get", (args, line) -> {
            checkArgs("get", 1, args, line);
            return doRequest("GET", requireString("get", args.get(0), line), null, new MapValue(), line);
        });

        // getWithHeaders(url, headers)
        funcs.put("getWithHeaders", (args, line) -> {
            checkArgs("getWithHeaders", 2, args, line);
            return doRequest("GET",
                requireString("getWithHeaders", args.get(0), line),
                null,
                requireMap("getWithHeaders", args.get(1), line), line);
        });

        // post(url, body)
        funcs.put("post", (args, line) -> {
            checkArgs("post", 2, args, line);
            return doRequest("POST",
                requireString("post", args.get(0), line),
                args.get(1).toString(),
                new MapValue(), line);
        });

        // postWithHeaders(url, body, headers)
        funcs.put("postWithHeaders", (args, line) -> {
            checkArgs("postWithHeaders", 3, args, line);
            return doRequest("POST",
                requireString("postWithHeaders", args.get(0), line),
                args.get(1).toString(),
                requireMap("postWithHeaders", args.get(2), line), line);
        });

        // put(url, body)
        funcs.put("put", (args, line) -> {
            checkArgs("put", 2, args, line);
            return doRequest("PUT",
                requireString("put", args.get(0), line),
                args.get(1).toString(),
                new MapValue(), line);
        });

        // delete(url)
        funcs.put("delete", (args, line) -> {
            checkArgs("delete", 1, args, line);
            return doRequest("DELETE",
                requireString("delete", args.get(0), line),
                null,
                new MapValue(), line);
        });

        // patch(url, body)
        funcs.put("patch", (args, line) -> {
            checkArgs("patch", 2, args, line);
            return doRequest("PATCH",
                requireString("patch", args.get(0), line),
                args.get(1).toString(),
                new MapValue(), line);
        });

        // request(url, method, body, headers) — full control
        funcs.put("request", (args, line) -> {
            checkArgs("request", 4, args, line);
            String body = (args.get(2) instanceof NullValue) ? null : args.get(2).toString();
            return doRequest(
                requireString("request", args.get(1), line).toUpperCase(),
                requireString("request", args.get(0), line),
                body,
                requireMap("request", args.get(3), line), line);
        });

        // encode(s) — URL encode
        funcs.put("encode", (args, line) -> {
            checkArgs("encode", 1, args, line);
            try {
                return new StringValue(
                    URLEncoder.encode(requireString("encode", args.get(0), line), "UTF-8")
                        .replace("+", "%20"));
            } catch (UnsupportedEncodingException e) {
                throw new NovaRuntimeException("net.encode() failed", line);
            }
        });

        // decode(s) — URL decode
        funcs.put("decode", (args, line) -> {
            checkArgs("decode", 1, args, line);
            try {
                return new StringValue(
                    URLDecoder.decode(requireString("decode", args.get(0), line), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new NovaRuntimeException("net.decode() failed", line);
            }
        });

        // isOk(res) — true if status 200-299
        funcs.put("isOk", (args, line) -> {
            checkArgs("isOk", 1, args, line);
            MapValue res = requireMap("isOk", args.get(0), line);
            int status = res.get("status").asInt();
            return new BooleanValue(status >= 200 && status < 300);
        });

        return funcs;
    }

    private static MapValue doRequest(String method, String urlStr,
                                       String body, MapValue extraHeaders, int line) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(urlStr))
                .timeout(Duration.ofSeconds(15));

            // Apply custom headers
            for (Map.Entry<String, Value> entry : extraHeaders.getRaw().entrySet())
                builder.header(entry.getKey(), entry.getValue().toString());

            // Set method and body
            if (body != null) {
                builder.method(method, HttpRequest.BodyPublishers.ofString(body));
                if (!extraHeaders.has("Content-Type"))
                    builder.header("Content-Type", "application/json");
            } else {
                switch (method) {
                    case "GET":    builder.GET();    break;
                    case "DELETE": builder.DELETE(); break;
                    default: builder.method(method, HttpRequest.BodyPublishers.noBody()); break;
                }
            }

            HttpResponse<String> response = CLIENT.send(
                builder.build(), HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();

            // Response headers as MapValue
            MapValue headersMap = new MapValue();
            for (Map.Entry<String, List<String>> entry : response.headers().map().entrySet())
                if (entry.getKey() != null)
                    headersMap.set(entry.getKey(),
                        new StringValue(String.join(", ", entry.getValue())));

            MapValue result = new MapValue();
            result.set("status",  new IntValue(status));
            result.set("body",    new StringValue(response.body()));
            result.set("ok",      new BooleanValue(status >= 200 && status < 300));
            result.set("headers", headersMap);
            return result;

        } catch (IllegalArgumentException e) {
            throw new NovaRuntimeException(
                "net." + method.toLowerCase() + "() invalid URL: '" + urlStr + "'", line);
        } catch (HttpTimeoutException e) {
            throw new NovaRuntimeException(
                "net." + method.toLowerCase() + "() request timed out", line);
        } catch (ConnectException e) {
            throw new NovaRuntimeException(
                "net." + method.toLowerCase() + "() connection refused: " + urlStr, line);
        } catch (UnknownHostException e) {
            throw new NovaRuntimeException(
                "net." + method.toLowerCase() + "() unknown host: " + urlStr, line);
        } catch (IOException e) {
            throw new NovaRuntimeException(
                "net." + method.toLowerCase() + "() IO error: " + e.getMessage(), line);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NovaRuntimeException(
                "net." + method.toLowerCase() + "() interrupted", line);
        }
    }

    private static void checkArgs(String name, int expected, List<Value> args, int line) {
        if (args.size() != expected)
            throw new ArgumentException(name, expected, args.size(), line);
    }

    private static String requireString(String name, Value v, int line) {
        if (!(v instanceof StringValue))
            throw new TypeError(name + "() expects a string but got " + v.getTypeName(), line);
        return ((StringValue) v).getRaw();
    }

    private static MapValue requireMap(String name, Value v, int line) {
        if (!(v instanceof MapValue))
            throw new TypeError(name + "() expects a map but got " + v.getTypeName(), line);
        return (MapValue) v;
    }
}