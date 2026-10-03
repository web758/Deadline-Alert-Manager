import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeadlineHandler implements HttpHandler {

    private final DeadlineRepository repository =
        new DeadlineRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        addCorsHeaders(exchange);

        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            // Handle browser preflight request
            if (method.equals("OPTIONS")) {
                sendResponse(exchange, 204, "");
                return;
            }

            // GET /api/deadlines/alerts
            if (path.equals("/api/deadlines/alerts")
                    && method.equals("GET")) {

                sendResponse(
                    exchange,
                    200,
                    toJson(repository.alerts())
                );
                return;
            }

            // GET /api/deadlines
            if (path.equals("/api/deadlines")
                    && method.equals("GET")) {

                String search =
                    getQuery(exchange, "search");

                String status =
                    getQuery(exchange, "status");

                String priority =
                    getQuery(exchange, "priority");

                sendResponse(
                    exchange,
                    200,
                    toJson(
                        repository.findAll(
                            search,
                            status,
                            priority
                        )
                    )
                );
                return;
            }

            // POST /api/deadlines
            if (path.equals("/api/deadlines")
                    && method.equals("POST")) {

                String body = readBody(exchange);

                String taskName =
                    getValue(body, "taskName");

                String subject =
                    getValue(body, "subject");

                String date =
                    getValue(body, "deadlineDate");

                String time =
                    getValue(body, "deadlineTime");

                String priority =
                    getValue(body, "priority");

                if (isBlank(taskName)
                        || isBlank(subject)
                        || isBlank(date)
                        || isBlank(time)) {

                    sendResponse(
                        exchange,
                        400,
                        "{\"error\":\"Required fields are missing\"}"
                    );
                    return;
                }

                if (isBlank(priority)) {
                    priority = "MEDIUM";
                }

                int id = repository.create(
                    taskName,
                    subject,
                    date,
                    time,
                    priority
                );

                sendResponse(
                    exchange,
                    201,
                    "{\"message\":\"Deadline created\",\"id\":"
                        + id + "}"
                );
                return;
            }

            // PUT /api/deadlines/{id}
            // PATCH /api/deadlines/{id}/complete
            // DELETE /api/deadlines/{id}

            Matcher matcher =
                Pattern.compile(
                    "/api/deadlines/(\\d+)(?:/(complete))?"
                ).matcher(path);

            if (matcher.matches()) {

                int id =
                    Integer.parseInt(matcher.group(1));

                // Complete deadline
                if (matcher.group(2) != null
                        && method.equals("PATCH")) {

                    boolean success =
                        repository.complete(id);

                    if (success) {
                        sendResponse(
                            exchange,
                            200,
                            "{\"message\":\"Deadline completed\"}"
                        );
                    } else {
                        sendResponse(
                            exchange,
                            404,
                            "{\"error\":\"Deadline not found\"}"
                        );
                    }

                    return;
                }

                // Update deadline
                if (method.equals("PUT")) {

                    String body =
                        readBody(exchange);

                    boolean success =
                        repository.update(
                            id,
                            getValue(body, "taskName"),
                            getValue(body, "subject"),
                            getValue(body, "deadlineDate"),
                            getValue(body, "deadlineTime"),
                            getValue(body, "priority"),
                            getValue(body, "status")
                        );

                    if (success) {
                        sendResponse(
                            exchange,
                            200,
                            "{\"message\":\"Deadline updated\"}"
                        );
                    } else {
                        sendResponse(
                            exchange,
                            404,
                            "{\"error\":\"Deadline not found\"}"
                        );
                    }

                    return;
                }

                // Delete deadline
                if (method.equals("DELETE")) {

                    boolean success =
                        repository.delete(id);

                    if (success) {
                        sendResponse(
                            exchange,
                            200,
                            "{\"message\":\"Deadline deleted\"}"
                        );
                    } else {
                        sendResponse(
                            exchange,
                            404,
                            "{\"error\":\"Deadline not found\"}"
                        );
                    }

                    return;
                }
            }

            sendResponse(
                exchange,
                404,
                "{\"error\":\"Endpoint not found\"}"
            );

        } catch (IllegalArgumentException e) {

            sendResponse(
                exchange,
                400,
                "{\"error\":\"Invalid date or time\"}"
            );

        } catch (SQLException e) {

            sendResponse(
                exchange,
                500,
                "{\"error\":\"Database error: "
                    + escape(e.getMessage()) + "\"}"
            );

        } catch (Exception e) {

            sendResponse(
                exchange,
                500,
                "{\"error\":\"Server error: "
                    + escape(e.getMessage()) + "\"}"
            );
        }
    }

    private static String readBody(
            HttpExchange exchange) throws IOException {

        try (InputStream input =
                 exchange.getRequestBody()) {

            return new String(
                input.readAllBytes(),
                StandardCharsets.UTF_8
            );
        }
    }

    private static String getValue(
            String json,
            String key) {

        String pattern =
            "\"" + Pattern.quote(key)
            + "\"\\s*:\\s*\"([^\"]*)\"";

        Matcher matcher =
            Pattern.compile(pattern).matcher(json);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    private static String getQuery(
            HttpExchange exchange,
            String name) {

        String query =
            exchange.getRequestURI().getRawQuery();

        if (query == null) {
            return "";
        }

        for (String item : query.split("&")) {

            String[] parts =
                item.split("=", 2);

            if (parts.length == 2
                    && parts[0].equals(name)) {

                return java.net.URLDecoder.decode(
                    parts[1],
                    StandardCharsets.UTF_8
                );
            }
        }

        return "";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String escape(String value) {

        if (value == null) {
            return "";
        }

        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");
    }

    private static String toJson(
            List<Deadline> deadlines) {

        StringBuilder json =
            new StringBuilder("[");

        for (int i = 0;
             i < deadlines.size();
             i++) {

            Deadline d = deadlines.get(i);

            if (i > 0) {
                json.append(",");
            }

            json.append("{")
                .append("\"id\":")
                .append(d.id)

                .append(",\"taskName\":\"")
                .append(escape(d.taskName))

                .append("\",\"subject\":\"")
                .append(escape(d.subject))

                .append("\",\"deadlineDate\":\"")
                .append(d.deadlineDate)

                .append("\",\"deadlineTime\":\"")
                .append(d.deadlineTime)

                .append("\",\"priority\":\"")
                .append(escape(d.priority))

                .append("\",\"status\":\"")
                .append(escape(d.status))

                .append("\"}");
        }

        json.append("]");

        return json.toString();
    }

    private static void addCorsHeaders(
            HttpExchange exchange) {

        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Origin",
            "*"
        );

        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Methods",
            "GET,POST,PUT,PATCH,DELETE,OPTIONS"
        );

        exchange.getResponseHeaders().set(
            "Access-Control-Allow-Headers",
            "Content-Type"
        );
    }

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response) throws IOException {

        byte[] data =
            response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
            "Content-Type",
            "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
            statusCode,
            data.length
        );

        try (OutputStream output =
                 exchange.getResponseBody()) {

            output.write(data);
        }
    }
}
