package toDoWithFavorites.Repository;

import toDoWithFavorites.Entity.User;
import org.springframework.data.repository.CrudRepository;

public interface Users extends CrudRepository<User, Integer> {
    public User findByUsername(String username);
}
