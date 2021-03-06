'use strict';
import {observable} from "../../../../node_modules/mobx/lib/mobx";

// tag::vars[]
const React = require('react');
const ReactDOM = require('react-dom');
const client = require('./client');

var Client = require('node-rest-client').Client;

var client2 = new Client();
// end::vars[]

// tag::app[]
class App extends React.Component {

    @observable toDoList = [];

    constructor(props) {
        super(props);
        this.state = {toDoList: this.toDoList};
        this.deleteJob = this.deleteJob.bind(this);
        this.deleteJobFromArray = this.deleteJobFromArray.bind(this);
        this.addJob = this.addJob.bind(this);
        this.handleChangeOnDone = this.handleChangeOnDone.bind(this);
        this.handleChangeOnFavorite = this.handleChangeOnFavorite.bind(this);
    }

    componentDidMount() {
        client({method: 'GET', path: '/api/data', headers: {'xAuth': 'teste'}}).then(response => {
            this.setState({toDoList: response.entity});
        });
    }

    deleteJob(job) {
        console.log(job);
        client({method: 'POST', path: '/api/delete/' + job.id, headers: {'xAuth': 'teste'}}).then({});
        this.deleteJobFromArray(job);
    }

    addJob() {
        var thisApp = this;
        client({
            method: 'POST',
            path: '/api/insert/' + $("#newJob").val(),
            headers: {'xAuth': 'teste'}
        }).then(response => {
            console.log(response.entity);
            thisApp.state.toDoList.push(response.entity);
            thisApp.setState({toDoList: thisApp.state.toDoList});
        });
    }

    deleteJobFromArray(job) {
        var index = this.state.toDoList.indexOf(job);
        if (index > -1) {
            var array = this.state.toDoList.splice(index, 1);
        }
        this.setState({toDoList: this.state.toDoList});
    }

    handleChangeOnDone(status, id) {
        var thisApp = this;
        client({
            method: 'POST', path: '/api/markDone/' + id + '/' + status, headers: {'xAuth': 'teste'}
        }).then(response => {

        });
    }

    handleChangeOnFavorite(status, id) {
        var thisApp = this;
        client({
            method: 'POST', path: '/api/markFavorite/' + id + '/' + status, headers: {'xAuth': 'teste'}
        }).then(response => {

        });
    }


    render() {
        return (
            <div>
                <ToDoList toDoList={this.state.toDoList} deleteJob={this.deleteJob} addJob={this.addJob}
                          handleChangeOnDone={this.handleChangeOnDone}
                          handleChangeOnFavorite={this.handleChangeOnFavorite}/>
                <input id="newJob" className="form-control"/>
                <button className="btn btn-primary col-sm-1" onClick={this.addJob}>Add</button>

            </div>
        )
    }
}

// end::app[]

const divStyle = {
    color: 'blue',
};

// tag::employee-list[]
class ToDoList extends React.Component {
    constructor(props) {
        super(props);
        this.deleteJob = this.deleteJob.bind(this);
        this.addJob = this.addJob.bind(this);
        this.handleChangeOnDone = this.handleChangeOnDone.bind(this);
        this.handleChangeOnFavorite = this.handleChangeOnFavorite.bind(this);
    }

    handleChangeOnDone(status, id) {
        this.props.handleChangeOnDone(status, id);
    }

    handleChangeOnFavorite(status, id) {
        this.props.handleChangeOnFavorite(status, id);
    }

    deleteJob(job) {
        this.props.deleteJob(job);
    }

    addJob() {
        this.props.addJob();
    }


    render() {
        var toDoList = this.props.toDoList.map(job =>
            <ToDo key={job.id} job={job} onDelete={this.deleteJob} handleChangeOnDone={this.handleChangeOnDone}
                  handleChangeOnFavorite={this.handleChangeOnFavorite}/>
        );
        var header = <Header/>;
        return (
            <div>
                {header}
                <table className="table table-bordered">
                    <tbody>
                    <tr>
                        <th className="col-sm-9">To Do</th>
                        <th className="col-sm-1">Favorite</th>
                        <th className="col-sm-1">Done</th>
                        <th className="col-sm-1"></th>
                    </tr>
                    {toDoList}
                    </tbody>
                </table>
            </div>
        )
    }
}

// end::employee-list[]


class Header extends React.Component {
    render() {
        return (
            <div>
                <div className="user_block">
                    <p>{this.state === null ? "" : this.state.username}</p>
                    <button onClick={this.logOut}>Logout</button>
                </div>
                <div className="add_user">
                    <input className="userinput" onChange={this.fillUsername}/>
                    <input className="userinput" onChange={this.fillPassword}/>
                    <button onClick={this.createUser}>Add User</button>
                </div>
                <h1>ToDo List</h1>
            </div>
        )
    }

    fillUsername(e) {
        this.state.newUsername = e.target.value;
    }

    fillPassword(e) {
        this.state.newPassword = e.target.value;
    }


    componentDidMount() {
        var thisApp = this;
        client({
            method: 'GET',
            path: '/me',
            headers: {'xAuth': 'teste'}
        }).then(response => {
            thisApp.setState({username: response.entity.username})
        });
    }

    constructor(props) {
        super(props);
        this.fillUsername = this.fillUsername.bind(this);
        this.fillPassword = this.fillPassword.bind(this);
        this.createUser = this.createUser.bind(this);
    }

    logOut() {
        window.location.href = '/logout';
    }

    createUser() {
        console.log(this.state.newUsername)
        var args = {
            data: {username: this.state.newUsername, password: this.state.newPassword},
            headers: {"Content-Type": "application/json"}
        };
        client2.post("http://localhost:8090/api/create/user", args, function (data, response) {
            // parsed response body as js object
            console.log(data);
            // raw response
            console.log(response);
        });
    }
}

// tag::employee[]
class ToDo extends React.Component {
    handleDelete(job) {
        this.props.onDelete(this.props.job);
    }

    constructor(props) {
        super(props);
        this.state = {checkedDone: this.props.job.done, checkedFavorite: this.props.job.favorite};
        this.handleDelete = this.handleDelete.bind(this);
        this.handleChangeOnDone = this.handleChangeOnDone.bind(this);
        this.handleChangeOnFavorite = this.handleChangeOnFavorite.bind(this);

    }

    handleChangeOnDone(event) {
        this.setState({checkedDone: event.target.checked});
        this.props.handleChangeOnDone(event.target.checked, this.props.job.id);
    }

    handleChangeOnFavorite(event) {
        this.setState({checkedFavorite: event.target.checked});
        this.props.handleChangeOnFavorite(event.target.checked, this.props.job.id);
    }

    render() {
        return (
            <tr>
                <td>{this.props.job.content}</td>
                <td><input type="checkbox" checked={this.state.checkedFavorite} onChange={this.handleChangeOnFavorite}/>
                </td>
                <td><input type="checkbox" checked={this.state.checkedDone} onChange={this.handleChangeOnDone}/></td>
                <td>
                    <button className="btn btn-danger" id={this.props.job.id} onClick={this.handleDelete}>Delete
                    </button>
                </td>
            </tr>
        )
    }
}

// end::employee[]

// tag::render[]
ReactDOM.render(
    <App/>,
    document.getElementById('root')
)


// end::render[]