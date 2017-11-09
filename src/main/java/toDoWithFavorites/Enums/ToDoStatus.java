package toDoWithFavorites.Enums;

public enum ToDoStatus {
    TODO("TODO", 0), IN_PROGRESS("IN_PROGRESS", 1), DONE("DONE", 2), BLOCKED("BLOCKED", 3);

    String status;
    int id;
    ToDoStatus(String status, int id) {
        this.status = status;
        this.id = id;
    }
    @Override
    public String toString() {
        return status;
    }

    public int getId() {
        return id;
    }
}
