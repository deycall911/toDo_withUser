package toDoWithFavorites.Repository;

import org.springframework.data.jpa.repository.Modifying;
import toDoWithFavorites.Entity.UserToDoList;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface UsersToDoList extends CrudRepository<UserToDoList, Integer> {
    public List<UserToDoList> findByUserId(Integer userId);
    public UserToDoList findByToDoId(Integer toDoId);
    @Modifying
    @Transactional
    public void deleteByToDoId(Integer toDoId);
}
