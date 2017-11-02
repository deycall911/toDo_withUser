package hello.Repository;

import hello.Entity.UserToDoList;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UsersToDoList extends CrudRepository<UserToDoList, Integer> {
    public List<UserToDoList> findByUserId(Integer userId);
    public UserToDoList findByToDoId(Integer toDoId);
}
