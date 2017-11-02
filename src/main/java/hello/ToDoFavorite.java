package hello;

import java.util.Date;

public class ToDoFavorite extends ToDo {
    public ToDoFavorite() {
    }

    public ToDoFavorite(ToDo toDo) {
        this.favorite = false;
        this.content = toDo.content;
        this.done = toDo.done;
        this.id = toDo.id;
        this.created = new Date();
    }

    public ToDoFavorite(ToDo toDo, Boolean favorite) {
        this.favorite = favorite;
        this.content = toDo.content;
        this.done = toDo.done;
        this.id = toDo.id;
    }

    public ToDoFavorite(ToDo toDo, Boolean favorite, Date created) {
        this.favorite = favorite;
        this.content = toDo.content;
        this.done = toDo.done;
        this.id = toDo.id;
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

    public Boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }
}
