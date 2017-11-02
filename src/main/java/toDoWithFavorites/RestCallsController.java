package toDoWithFavorites;

import toDoWithFavorites.Entity.User;
import toDoWithFavorites.Entity.UserToDoList;
import toDoWithFavorites.Repository.Users;
import toDoWithFavorites.Repository.UsersToDoList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class RestCallsController {
    private WebTarget getClient() {
        Client client = ClientBuilder.newClient();
        client.register(LoggerFilter.class);
        return client.target("https://serene-ravine-85231.herokuapp.com").path("api");
    }

    @Autowired
    Users users;

    @Autowired
    UsersToDoList usersToDoList;

    @RequestMapping(method = POST, value = "/api/insert/{job}")
    public ToDoFavorite insert(@PathVariable String job) {
        MyUserPrincipal userDetails = (MyUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = users.findByUsername(userDetails.getUsername());

        ToDo toDo = getClient().path("insert").path(job).request()
                .header("xAuth", "teste")
                .post(Entity.json("")).readEntity(ToDo.class);

        UserToDoList newUserToDo = new UserToDoList();
        newUserToDo.setUserId(user.getId());
        newUserToDo.setToDoId(toDo.getId());
        newUserToDo.setCreated(new Date());
        newUserToDo.setFavorite(false);

        usersToDoList.save(newUserToDo);

        return new ToDoFavorite(toDo);
    }

    @RequestMapping(method = POST, value = "/api/delete/{id}")
    public Boolean delete(@PathVariable int id) throws Exception {
        MyUserPrincipal userDetails = (MyUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        belongToUser(userDetails.getUserId(), id);

        Boolean deleteSuccessful = getClient().path("delete").path(String.valueOf(id))
                .request().header("xAuth", "teste")
                .post(Entity.json("")).readEntity(Boolean.class);

        if (deleteSuccessful) {
            usersToDoList.delete(id);
            return true;
        } else {
            return false;
        }
    }

    @RequestMapping("/api/data")
    public Iterable<ToDoFavorite> requestList(@RequestParam(required = false) Boolean done) {
        MyUserPrincipal userDetails = (MyUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return getUsersToDos(userDetails.getUserId());
    }

    @RequestMapping("/api/markDone/{id}/{done}")
    public ToDoFavorite markAsDone(@PathVariable int id, @PathVariable Boolean done) throws Exception {
        MyUserPrincipal userDetails = (MyUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        belongToUser(userDetails.getUserId(), id);

        ToDo oldToDo = getClient().path("markDone").path(String.valueOf(id)).path(String.valueOf(done))
                .request().header("xAuth", "teste")
                .post(Entity.json("")).readEntity(ToDo.class);

        return new ToDoFavorite(oldToDo, usersToDoList.findByToDoId(id).isFavorite());
    }

    @RequestMapping("/api/markFavorite/{id}/{favorite}")
    public ToDoFavorite markAsFavorite(@PathVariable int id, @PathVariable Boolean favorite) throws Exception {
        MyUserPrincipal userDetails = (MyUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        belongToUser(userDetails.getUserId(), id);

        UserToDoList userToDoList = usersToDoList.findByToDoId(id);
        userToDoList.setFavorite(favorite);
        usersToDoList.save(userToDoList);

        List<ToDo> oldToDoList = getClient().path("data")
                .request().header("xAuth", "teste")
                .post(Entity.json(Collections.singletonList(id))).readEntity(new GenericType<List<ToDo>>() {});
        return new ToDoFavorite(oldToDoList.get(0), favorite);
    }

    private List<ToDoFavorite> getUsersToDos(int userId) {
        List<UserToDoList> usersToDos = usersToDoList.findByUserId(userId);
        List<Integer> requiredToDoIds = usersToDos.stream().map(UserToDoList::getToDoId).collect(Collectors.toList());
        List<ToDo> oldToDoList = getClient().path("data")
                .request().header("xAuth", "teste")
                .post(Entity.json(requiredToDoIds)).readEntity(new GenericType<List<ToDo>>() {
                });
        return oldToDoList.stream().map(toDo -> {
            UserToDoList currentUserToDoList = usersToDos.stream().filter(userToDoList -> userToDoList.getToDoId().equals(toDo.getId())).findFirst().get();
            return new ToDoFavorite(toDo, currentUserToDoList.isFavorite(), currentUserToDoList.getCreated());
        }).sorted((toDoFavorite1, toDoFavorite2) -> {
            if (toDoFavorite1.isDone().equals(toDoFavorite2.isDone()) && toDoFavorite1.isFavorite().equals(toDoFavorite2.isFavorite())) return 0;
            if (toDoFavorite1.isDone().equals(toDoFavorite2.isDone())) {
                if (toDoFavorite1.isFavorite()) return -1;
                if (toDoFavorite2.isFavorite()) return 1;
            }
            if (toDoFavorite1.isFavorite().equals(toDoFavorite2.isFavorite())) {
                if (toDoFavorite1.isDone()) return 1;
                if (toDoFavorite2.isDone()) return -1;
            }
            if (toDoFavorite1.isFavorite() || toDoFavorite2.isDone()) return -1;
            if (toDoFavorite2.isFavorite() || toDoFavorite1.isDone()) return 1;

            return toDoFavorite1.getCreated().compareTo(toDoFavorite2.getCreated());
        }).collect(Collectors.toList());
    }

    private void belongToUser(int userId, int toDoId) throws Exception {
        List<ToDoFavorite> toDos = getUsersToDos(userId);
        if (toDos == null || toDos.isEmpty() || toDos.stream().noneMatch(x -> x.getId() == toDoId)) {
            throw new Exception("This toDo doesn't exist or doesn't belong to you");
        }
    }




}
