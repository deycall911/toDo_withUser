package toDoWithFavorites;

import toDoWithFavorites.Enums.ToDoStatus;

import java.util.Date;

public class ToDoFavorite extends ToDo {
    public ToDoFavorite() {
    }

    public ToDoFavorite(ToDo toDo) {
        this.status = ToDoStatus.TODO;
        this.favorite = false;
        this.content = toDo.content;
        this.done = toDo.done;
        this.id = toDo.id;
        this.created = new Date();
    }

    public ToDoFavorite(ToDo toDo, Boolean favorite, ToDoStatus status) {
        this.favorite = favorite;
        this.content = toDo.content;
        this.done = toDo.done;
        this.id = toDo.id;
        this.status = status;
    }

    public ToDoFavorite(ToDo toDo, Boolean favorite, Date created, ToDoStatus status) {
        this(toDo, favorite, status);
        this.created = created;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    private Date created;
    private Boolean favorite;
    private ToDoStatus status;

    public ToDoStatus getStatus() {
        return status;
    }

    public void setStatus(ToDoStatus status) {
        this.status = status;
    }

    public Boolean isFavorite() {
        return favorite;
    }


    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }
}
