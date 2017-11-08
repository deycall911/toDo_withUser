package toDoWithFavorites.Entity;

import toDoWithFavorites.Enums.ToDoStatus;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "users_todos")
public class UserToDoList {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getToDoId() {
        return toDoId;
    }

    public void setToDoId(Integer toDoId) {
        this.toDoId = toDoId;
    }

    private Integer userId;

    private Integer toDoId;

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    private Date created;

    public Boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }

    private Boolean favorite;

    public ToDoStatus getStatus() {
        return status;
    }

    public void setStatus(ToDoStatus status) {
        this.status = status;
    }

    @Enumerated(EnumType.STRING)
    private ToDoStatus status;
}
