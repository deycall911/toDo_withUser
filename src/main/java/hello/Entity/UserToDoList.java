package hello.Entity;

import javax.persistence.*;

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
}
