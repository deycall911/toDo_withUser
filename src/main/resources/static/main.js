'use strict';

// tag::vars[]
const React = require('react');
const ReactDOM = require('react-dom');
const client = require('./client');
// end::vars[]

// tag::app[]
class App extends React.Component {

	constructor(props) {
		super(props);
		this.state = {toDoList: []};
		this.deleteJob = this.deleteJob.bind(this);
        this.deleteJobFromArray = this.deleteJobFromArray.bind(this);
        this.addJob = this.addJob.bind(this);
		this.handleChange = this.handleChange.bind(this);


	}

	componentDidMount() {
		client({method: 'GET', path: '/api/data', headers: {'xAuth': 'teste'}}).then(response => {
			this.setState({toDoList: response.entity});
		});
	}
    deleteJob(job) {
            console.log(job);
            client({method: 'POST', path: '/api/delete/'+job.id, headers: {'xAuth': 'teste'}}).then({});
            this.deleteJobFromArray(job);
    }

    addJob() {
        var thisApp = this;
        client({method: 'POST', path: '/api/insert/'+$("#newJob").val(), headers: {'xAuth': 'teste'}}).then(response => {
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

        handleChange(status, id) {
            var thisApp = this;
            client({method: 'POST', path: '/api/markDone/'+id+'/'+status, headers: {'xAuth': 'teste'}
            }).then(response => {

            });
        }



	render() {
		return (
		<div>
			<ToDoList toDoList={this.state.toDoList} deleteJob={this.deleteJob} addJob={this.addJob} handleChange={this.handleChange}/>
            <input id="newJob" className="form-control" />
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
class ToDoList extends React.Component{
    constructor(props) {
		super(props);
		this.deleteJob = this.deleteJob.bind(this);
		this.addJob = this.addJob.bind(this);
		this.handleChange = this.handleChange.bind(this);
	}

    handleChange(status, id) {
        this.props.handleChange(status, id);
    }

        deleteJob(job) {
            this.props.deleteJob(job);
        }
     addJob() {
        this.props.addJob();
     }


	render() {
		var toDoList = this.props.toDoList.map(job =>
			<ToDo key={job.id} job={job} onDelete={this.deleteJob} handleChange={this.handleChange}/>
		);
		return (
			<table className="table table-bordered">
				<tbody>
					<tr>
						<th className="col-sm-10">To Do</th>
						<th className="col-sm-1">Done</th>
						<th className="col-sm-1"></th>
					</tr>
					{toDoList}
				</tbody>
			</table>
		)
	}
}
// end::employee-list[]

// tag::employee[]
class ToDo extends React.Component{
    handleDelete(job) {
        this.props.onDelete(this.props.job);
    }

    constructor(props) {
    		super(props);
    		this.state = {checked: this.props.job.done};
    		this.handleDelete = this.handleDelete.bind(this);
    		this.handleChange = this.handleChange.bind(this);
    	}

      handleChange(event) {
        this.setState({checked: event.target.checked});
        this.props.handleChange(event.target.checked, this.props.job.id);
      }

	render() {
		return (
			<tr>
				<td>{this.props.job.content}</td>
				<td><input type="checkbox" checked={this.state.checked} onChange={this.handleChange} /></td>
				<td><button className="btn btn-danger" id={this.props.job.id} onClick={this.handleDelete}>Delete</button></td>
			</tr>
		)
	}
}
// end::employee[]

// tag::render[]
ReactDOM.render(
	<App />,
	document.getElementById('root')
)


// end::render[]