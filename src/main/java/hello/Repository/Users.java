package hello.Repository;

import hello.Entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface Users extends CrudRepository<User, Integer> {
    public User findByUsername(String username);
}
