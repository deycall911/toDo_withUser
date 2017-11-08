package toDoWithFavorites.Enums;

public enum Role {
    ADMIN(0), SUPERVISOR(1), USER(2);
    private int priority;

    Role(int i) {
        this.priority = i;
    }

    public int getPriority() {
        return priority;
    }
}

