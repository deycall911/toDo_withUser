package hello;

import hello.Entity.User;
import hello.Entity.UserToDoList;
import hello.Repository.Users;
import hello.Repository.UsersToDoList;
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
    public ToDo insert(@PathVariable String job) {
        MyUserPrincipal userDetails = (MyUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = users.findByUsername(userDetails.getUsername());

        ToDo toDo = getClient().path("insert").path(job).request()
                .header("xAuth", "teste")
                .post(Entity.json("")).readEntity(ToDo.class);

        UserToDoList newUserToDo = new UserToDoList();
        newUserToDo.setUserId(user.getId());
        newUserToDo.setToDoId(toDo.getId());

        usersToDoList.save(newUserToDo);

        return toDo;
    }

    @RequestMapping(method = POST, value = "/api/delete/{id}")
    public Boolean delete(@PathVariable int id) throws Exception {
        MyUserPrincipal userDetails = (MyUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        belongToUser(userDetails.getUserId(), id);

        return getClient().path("delete").path(String.valueOf(id))
                .request().header("xAuth", "teste")
                .post(Entity.json("")).readEntity(Boolean.class);
    }

    @RequestMapping("/api/data")
    public Iterable<ToDo> requestList(@RequestParam(required = false) Boolean done) {
        MyUserPrincipal userDetails = (MyUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return getUsersToDos(userDetails.getUserId());
    }

    @RequestMapping("/api/markDone/{id}/{done}")
    public ToDo markAsDone(@PathVariable int id, @PathVariable Boolean done) throws Exception {
        MyUserPrincipal userDetails = (MyUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        belongToUser(userDetails.getUserId(), id);

        return getClient().path("markDone").path(String.valueOf(id)).path(String.valueOf(done))
                .request().header("xAuth", "teste")
                .post(Entity.json("")).readEntity(ToDo.class);
    }

    private List<ToDo> getUsersToDos(int userId) {
        List<UserToDoList> usersToDos = usersToDoList.findByUserId(userId);
        List<Integer> requiredToDoIds = usersToDos.stream().map(UserToDoList::getToDoId).collect(Collectors.toList());
        return getClient().path("data")
                .request().header("xAuth", "teste")
                .post(Entity.json(requiredToDoIds)).readEntity(new GenericType<List<ToDo>>() {
                });

    }

    private void belongToUser(int userId, int toDoId) throws Exception {
        List<ToDo> toDos = getUsersToDos(userId);
        if (toDos == null || toDos.isEmpty() || toDos.stream().noneMatch(x -> x.getId() == toDoId)) {
            throw new Exception("This toDo doesn't exist or doesn't belong to you");
        }
    }


}
