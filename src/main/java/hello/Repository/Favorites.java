package hello.Repository;

import hello.Entity.Favorite;
import hello.Entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface Favorites extends CrudRepository<Favorite, Integer> {
    public List<Favorite> findByUserId(Integer userId);
}
