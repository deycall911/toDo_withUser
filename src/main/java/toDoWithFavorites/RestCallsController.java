package toDoWithFavorites;

import com.deliveredtechnologies.rulebook.FactMap;
import com.deliveredtechnologies.rulebook.NameValueReferableMap;
import com.deliveredtechnologies.rulebook.lang.RuleBookBuilder;
import com.deliveredtechnologies.rulebook.model.RuleBook;
import org.springframework.web.bind.annotation.RequestBody;
import toDoWithFavorites.Entity.User;
import toDoWithFavorites.Entity.UserToDoList;
import toDoWithFavorites.Enums.Role;
import toDoWithFavorites.Enums.ToDoStatus;
import toDoWithFavorites.Exceptions.CustomException;
import toDoWithFavorites.Exceptions.NotEnoughPrivilegesException;
import toDoWithFavorites.Exceptions.UserAlreadyExistException;
import toDoWithFavorites.Exceptions.WrongStatusException;
import toDoWithFavorites.Exceptions.WrongUserException;
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
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static toDoWithFavorites.Enums.ToDoStatus.BLOCKED;
import static toDoWithFavorites.Enums.ToDoStatus.DONE;
import static toDoWithFavorites.Enums.ToDoStatus.IN_PROGRESS;
import static toDoWithFavorites.Enums.ToDoStatus.TODO;

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

    @RequestMapping("/me")
    public User getCurrentUserData() {
        MyUserPrincipal currentUserDetails = (MyUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return users.findByUsername(currentUserDetails.getUsername());
    }

    @RequestMapping("/user/{username}")
    public User getUserData(@PathVariable String username) throws CustomException {
        MyUserPrincipal currentUserDetails = (MyUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User requestedUser = users.findByUsername(username);
        User currentUser = users.findByUsername(currentUserDetails.getUsername());
        if (requestedUser != null && currentUser.getId().equals(requestedUser.getId())) return requestedUser;
        if (requestedUser == null || (currentUser.getRole().getPriority() >= requestedUser.getRole().getPriority())) {
            throw new WrongUserException("User with such username doesn't exist under your group");
        }
        Integer userOwner = requestedUser.getOwnerId();
        while (userOwner != null && !userOwner.equals(currentUser.getId())) {
            userOwner = users.findOne(userOwner).getOwnerId();
        }

        if (userOwner == null) {
            throw new WrongUserException("User with such username doesn't exist under your group");
        } else {
            return requestedUser;
        }
    }

    @RequestMapping(method = POST, value = "/api/create/user")
    public User createUser(@RequestBody Map<String, String> body) throws Exception {
        User newUser = new User();
        newUser.setUsername(body.get("username"));
        newUser.setPassword(body.get("password"));

        MyUserPrincipal userDetails = (MyUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = users.findByUsername(userDetails.getUsername());

        if (users.findByUsername(newUser.getUsername()) != null) {
            throw new UserAlreadyExistException();
        }
        if (currentUser.getRole() == Role.ADMIN) {
            newUser.setRole(Role.SUPERVISOR);
        } else if (currentUser.getRole() == Role.SUPERVISOR) {
            newUser.setRole(Role.USER);
        } else {
            throw new NotEnoughPrivilegesException();
        }
        newUser.setOwnerId(currentUser.getId());

        return users.save(newUser);
    }

    @RequestMapping(method = POST, value = "/api/status/{id}/{newStatus}")
    public ToDoStatus changeStatus(@PathVariable Integer id, @PathVariable ToDoStatus newStatus) throws Exception {
        MyUserPrincipal userDetails = (MyUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        belongToUser(userDetails.getUserId(), id);
        UserToDoList currentToDoUserRelation = usersToDoList.findByToDoId(id);

        RuleBook ruleBook = RuleBookBuilder.create()
                .addRule(rule -> rule.withFactType(ToDoStatus.class)
                        .when(f -> currentToDoUserRelation.getStatus().getId() + 1 == f.getOne().getId())
                        .then(f -> saveStatus(currentToDoUserRelation, f.getOne())))
                .addRule(rule -> rule.withFactType(ToDoStatus.class)
                        .when(f -> currentToDoUserRelation.getStatus().getId() + 1 != f.getOne().getId())
                        .then(f -> saveStatus(currentToDoUserRelation, BLOCKED))
                ).build();

        NameValueReferableMap factMap = new FactMap();
        factMap.setValue(currentToDoUserRelation.getStatus().toString(), newStatus);
        ruleBook.run(factMap);

        return usersToDoList.findByToDoId(id).getStatus();
    }

    private void saveStatus(UserToDoList currentToDoUserRelation, ToDoStatus status) {
        try {
            currentToDoUserRelation.setStatus(status);
            usersToDoList.save(currentToDoUserRelation);
        } catch (Exception e) {
            currentToDoUserRelation.setStatus(BLOCKED);
            usersToDoList.save(currentToDoUserRelation);
        }
    }

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
        newUserToDo.setStatus(TODO);

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
            usersToDoList.deleteByToDoId(id);
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

        ToDoFavorite currentToDoFavorite = belongToUser(userDetails.getUserId(), id);
        ToDo oldToDo = getClient().path("markDone").path(String.valueOf(id)).path(String.valueOf(done))
                .request().header("xAuth", "teste")
                .post(Entity.json("")).readEntity(ToDo.class);

        return new ToDoFavorite(oldToDo, usersToDoList.findByToDoId(id).isFavorite(), currentToDoFavorite.getStatus());
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
                .post(Entity.json(Collections.singletonList(id))).readEntity(new GenericType<List<ToDo>>() {
                });
        return new ToDoFavorite(oldToDoList.get(0), favorite, userToDoList.getStatus());
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
            return new ToDoFavorite(toDo, currentUserToDoList.isFavorite(), currentUserToDoList.getCreated(), currentUserToDoList.getStatus());
        }).sorted((toDoFavorite1, toDoFavorite2) -> {
            if (toDoFavorite1.isDone().equals(toDoFavorite2.isDone()) && toDoFavorite1.isFavorite().equals(toDoFavorite2.isFavorite()))
                return 0;
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

    //Return current toDoFavorite
    private ToDoFavorite belongToUser(int userId, int toDoId) throws Exception {
        List<ToDoFavorite> toDos = getUsersToDos(userId);
        if (toDos == null || toDos.isEmpty() || toDos.stream().noneMatch(x -> x.getId() == toDoId)) {
            throw new WrongUserException("This toDo doesn't exist or doesn't belong to you");
        }
        return toDos.stream().filter(toDoFavorite -> toDoFavorite.getId() == toDoId).findFirst().get();
    }


}
