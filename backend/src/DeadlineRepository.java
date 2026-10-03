import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeadlineRepository {

    private Deadline map(ResultSet r) throws SQLException {
        return new Deadline(
            r.getInt("id"),
            r.getString("task_name"),
            r.getString("subject"),
            r.getDate("deadline_date"),
            r.getTime("deadline_time"),
            r.getString("priority"),
            r.getString("status"),
            r.getTimestamp("created_at")
        );
    }

    // View, search and filter deadlines
    public List<Deadline> findAll(
            String search,
            String status,
            String priority) throws SQLException {

        StringBuilder query =
            new StringBuilder("SELECT * FROM deadlines WHERE 1=1");

        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            query.append(
                " AND (task_name LIKE ? OR subject LIKE ?)"
            );
            parameters.add("%" + search + "%");
            parameters.add("%" + search + "%");
        }

        if (status != null &&
            !status.isBlank() &&
            !status.equals("ALL")) {

            query.append(" AND status=?");
            parameters.add(status);
        }

        if (priority != null &&
            !priority.isBlank() &&
            !priority.equals("ALL")) {

            query.append(" AND priority=?");
            parameters.add(priority);
        }

        query.append(" ORDER BY deadline_date, deadline_time");

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(query.toString())
        ) {

            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet result = statement.executeQuery()) {

                List<Deadline> deadlines = new ArrayList<>();

                while (result.next()) {
                    deadlines.add(map(result));
                }

                return deadlines;
            }
        }
    }

    // Find deadlines due within the next 2 days
    public List<Deadline> alerts() throws SQLException {

        String query =
            "SELECT * FROM deadlines " +
            "WHERE status='PENDING' " +
            "AND TIMESTAMP(deadline_date, deadline_time) " +
            "<= DATE_ADD(NOW(), INTERVAL 2 DAY) " +
            "ORDER BY deadline_date, deadline_time";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(query);
            ResultSet result = statement.executeQuery()
        ) {

            List<Deadline> deadlines = new ArrayList<>();

            while (result.next()) {
                deadlines.add(map(result));
            }

            return deadlines;
        }
    }

    // Add a new deadline
    public int create(
            String task,
            String subject,
            String date,
            String time,
            String priority) throws SQLException {

        String query =
            "INSERT INTO deadlines " +
            "(task_name, subject, deadline_date, deadline_time, priority) " +
            "VALUES (?, ?, ?, ?, ?)";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(
                    query,
                    Statement.RETURN_GENERATED_KEYS
                )
        ) {

            statement.setString(1, task);
            statement.setString(2, subject);
            statement.setDate(3, Date.valueOf(date));

            if (time.length() == 5) {
                time = time + ":00";
            }

            statement.setTime(4, Time.valueOf(time));
            statement.setString(5, priority);

            statement.executeUpdate();

            try (ResultSet result =
                     statement.getGeneratedKeys()) {

                if (result.next()) {
                    return result.getInt(1);
                }
            }
        }

        throw new SQLException("ID generation failed");
    }

    // Update deadline
    public boolean update(
            int id,
            String task,
            String subject,
            String date,
            String time,
            String priority,
            String status) throws SQLException {

        String query =
            "UPDATE deadlines SET " +
            "task_name=?, subject=?, deadline_date=?, " +
            "deadline_time=?, priority=?, status=? " +
            "WHERE id=?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(query)
        ) {

            statement.setString(1, task);
            statement.setString(2, subject);
            statement.setDate(3, Date.valueOf(date));

            if (time.length() == 5) {
                time = time + ":00";
            }

            statement.setTime(4, Time.valueOf(time));
            statement.setString(5, priority);
            statement.setString(6, status);
            statement.setInt(7, id);

            return statement.executeUpdate() > 0;
        }
    }

    // Mark deadline as completed
    public boolean complete(int id) throws SQLException {

        String query =
            "UPDATE deadlines SET status='COMPLETED' WHERE id=?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(query)
        ) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    // Delete deadline
    public boolean delete(int id) throws SQLException {

        String query =
            "DELETE FROM deadlines WHERE id=?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(query)
        ) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }
}
