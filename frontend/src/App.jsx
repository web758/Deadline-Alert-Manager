import { useEffect, useState } from "react";

const API = "http://localhost:8080/api/deadlines";

const emptyForm = {
  taskName: "",
  subject: "",
  deadlineDate: "",
  deadlineTime: "10:00",
  priority: "MEDIUM",
  status: "PENDING",
};

function App() {
  const [deadlines, setDeadlines] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [form, setForm] = useState(emptyForm);

  const [editId, setEditId] = useState(null);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("ALL");
  const [priority, setPriority] = useState("ALL");
  const [message, setMessage] = useState("");

  async function loadData() {
    try {
      const params = new URLSearchParams({
        search,
        status,
        priority,
      });

      const response = await fetch(`${API}?${params}`);
      const data = await response.json();

      if (response.ok) {
        setDeadlines(data);
      } else {
        setMessage(data.error);
      }

      const alertResponse = await fetch(`${API}/alerts`);

      if (alertResponse.ok) {
        setAlerts(await alertResponse.json());
      }
    } catch (error) {
      setMessage("Cannot connect to Java backend.");
    }
  }

  useEffect(() => {
    loadData();
  }, [search, status, priority]);

  function handleChange(event) {
    setForm({
      ...form,
      [event.target.name]: event.target.value,
    });
  }

  async function saveDeadline(event) {
    event.preventDefault();

    try {
      const response = await fetch(
        editId ? `${API}/${editId}` : API,
        {
          method: editId ? "PUT" : "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(form),
        }
      );

      const data = await response.json();

      setMessage(data.message || data.error);

      if (response.ok) {
        setForm(emptyForm);
        setEditId(null);
        loadData();
      }
    } catch (error) {
      setMessage("Cannot connect to Java backend.");
    }
  }

  function editDeadline(item) {
    setEditId(item.id);

    setForm({
      taskName: item.taskName,
      subject: item.subject,
      deadlineDate: item.deadlineDate,
      deadlineTime: item.deadlineTime.slice(0, 5),
      priority: item.priority,
      status: item.status,
    });

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  }

  async function completeDeadline(id) {
    try {
      const response = await fetch(
        `${API}/${id}/complete`,
        {
          method: "PATCH",
        }
      );

      const data = await response.json();

      setMessage(data.message || data.error);
      loadData();
    } catch (error) {
      setMessage("Cannot connect to Java backend.");
    }
  }

  async function deleteDeadline(id) {
    const confirmed = window.confirm(
      "Delete this deadline?"
    );

    if (!confirmed) {
      return;
    }

    try {
      const response = await fetch(
        `${API}/${id}`,
        {
          method: "DELETE",
        }
      );

      const data = await response.json();

      setMessage(data.message || data.error);
      loadData();
    } catch (error) {
      setMessage("Cannot connect to Java backend.");
    }
  }

  function cancelEdit() {
    setEditId(null);
    setForm(emptyForm);
  }

  const total = deadlines.length;

  const pending = deadlines.filter(
    (item) => item.status === "PENDING"
  ).length;

  const completed = deadlines.filter(
    (item) => item.status === "COMPLETED"
  ).length;

  return (
    <div className="page">

      <header className="header">
        <div>
          <p className="small-title">
            JAVA MINI PROJECT
          </p>

          <h1>Deadline Alert Manager</h1>

          <p className="subtitle">
            Manage assignments, deadlines and alerts easily
          </p>
        </div>

        <div className="technology">
          JAVA • JDBC • MYSQL
        </div>
      </header>

      <main>

        {message && (
          <div className="message">
            {message}
          </div>
        )}

        {/* Statistics */}

        <div className="stats">

          <div className="stat-card">
            <span>Total Deadlines</span>
            <strong>{total}</strong>
          </div>

          <div className="stat-card">
            <span>Pending</span>
            <strong>{pending}</strong>
          </div>

          <div className="stat-card">
            <span>Completed</span>
            <strong>{completed}</strong>
          </div>

          <div className="stat-card">
            <span>Alerts</span>
            <strong>{alerts.length}</strong>
          </div>

        </div>

        {/* Add / Update */}

        <section className="card">

          <div className="section-header">

            <h2>
              {editId
                ? "Update Deadline"
                : "Add New Deadline"}
            </h2>

            {editId && (
              <button
                className="secondary-button"
                onClick={cancelEdit}
              >
                Cancel
              </button>
            )}

          </div>

          <form onSubmit={saveDeadline}>

            <div className="form-group">
              <label>Task Name</label>

              <input
                name="taskName"
                value={form.taskName}
                onChange={handleChange}
                placeholder="Example: Java Assignment"
                required
              />
            </div>

            <div className="form-group">
              <label>Subject</label>

              <input
                name="subject"
                value={form.subject}
                onChange={handleChange}
                placeholder="Example: Java"
                required
              />
            </div>

            <div className="form-group">
              <label>Deadline Date</label>

              <input
                type="date"
                name="deadlineDate"
                value={form.deadlineDate}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label>Deadline Time</label>

              <input
                type="time"
                name="deadlineTime"
                value={form.deadlineTime}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label>Priority</label>

              <select
                name="priority"
                value={form.priority}
                onChange={handleChange}
              >
                <option value="HIGH">HIGH</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="LOW">LOW</option>
              </select>
            </div>

            {editId && (
              <div className="form-group">
                <label>Status</label>

                <select
                  name="status"
                  value={form.status}
                  onChange={handleChange}
                >
                  <option value="PENDING">
                    PENDING
                  </option>

                  <option value="COMPLETED">
                    COMPLETED
                  </option>
                </select>
              </div>
            )}

            <button
              className="primary-button"
              type="submit"
            >
              {editId
                ? "Update Deadline"
                : "Add Deadline"}
            </button>

          </form>

        </section>

        {/* Alerts */}

        <section className="card">

          <div className="section-header">

            <h2>Upcoming Alerts</h2>

            <button
              className="secondary-button"
              onClick={loadData}
            >
              Refresh
            </button>

          </div>

          {alerts.length === 0 ? (

            <p className="empty">
              No upcoming alerts within 2 days.
            </p>

          ) : (

            <div>

              {alerts.map((item) => (

                <div
                  className="alert"
                  key={item.id}
                >

                  <div>
                    <strong>
                      {item.taskName}
                    </strong>

                    <p>
                      {item.subject}
                    </p>
                  </div>

                  <div className="alert-date">
                    Due: {item.deadlineDate}
                    <br />
                    {item.deadlineTime.slice(0, 5)}
                  </div>

                  <span
                    className={`priority ${item.priority.toLowerCase()}`}
                  >
                    {item.priority}
                  </span>

                </div>

              ))}

            </div>

          )}

        </section>

        {/* All Deadlines */}

        <section className="card">

          <div className="section-header">

            <h2>All Deadlines</h2>

          </div>

          <div className="filters">

            <input
              placeholder="Search task or subject"
              value={search}
              onChange={(event) =>
                setSearch(event.target.value)
              }
            />

            <select
              value={status}
              onChange={(event) =>
                setStatus(event.target.value)
              }
            >
              <option value="ALL">All Status</option>
              <option value="PENDING">Pending</option>
              <option value="COMPLETED">
                Completed
              </option>
            </select>

            <select
              value={priority}
              onChange={(event) =>
                setPriority(event.target.value)
              }
            >
              <option value="ALL">
                All Priority
              </option>
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </select>

          </div>

          <div className="table-container">

            <table>

              <thead>

                <tr>
                  <th>ID</th>
                  <th>Task</th>
                  <th>Subject</th>
                  <th>Due Date</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>

              </thead>

              <tbody>

                {deadlines.length === 0 ? (

                  <tr>
                    <td
                      colSpan="7"
                      className="empty"
                    >
                      No deadlines found.
                    </td>
                  </tr>

                ) : (

                  deadlines.map((item) => (

                    <tr key={item.id}>

                      <td>
                        #{item.id}
                      </td>

                      <td>
                        <strong>
                          {item.taskName}
                        </strong>
                      </td>

                      <td>
                        {item.subject}
                      </td>

                      <td>
                        {item.deadlineDate}
                        <br />
                        {item.deadlineTime.slice(0, 5)}
                      </td>

                      <td>

                        <span
                          className={`priority ${item.priority.toLowerCase()}`}
                        >
                          {item.priority}
                        </span>

                      </td>

                      <td>

                        <span
                          className={
                            item.status === "COMPLETED"
                              ? "status completed"
                              : "status pending"
                          }
                        >
                          {item.status}
                        </span>

                      </td>

                      <td className="actions">

                        <button
                          onClick={() =>
                            editDeadline(item)
                          }
                        >
                          Edit
                        </button>

                        {item.status !==
                          "COMPLETED" && (

                          <button
                            onClick={() =>
                              completeDeadline(item.id)
                            }
                          >
                            Complete
                          </button>

                        )}

                        <button
                          onClick={() =>
                            deleteDeadline(item.id)
                          }
                        >
                          Delete
                        </button>

                      </td>

                    </tr>

                  ))

                )}

              </tbody>

            </table>

          </div>

        </section>

      </main>

      <footer>
        Deadline Alert Manager •  Java Application
      </footer>

    </div>
  );
}

export default App;