'use strict';

const React = require('react')
const ReactDOM = require('react-dom')
const client = require('./client')
const when = require('when')
const follow = require('./follow')

const Module = require('./Module')

const root = '/api'

class App extends React.Component {

	constructor(props) {
		super(props);
		this.state = {modules: {}} // TODO: Split up state between each module
		this.findModules = this.findModules.bind(this)
		this.refresh = this.refresh.bind(this)
		this.undeploy = this.undeploy.bind(this)
	}

	findModules() {
		follow(client, root, ['modules']).done(response => {
			this.setState({modules: response.entity._embedded.appStatusList.reduce((prev, curr) => {
				prev[curr.deploymentId] = curr
				return prev
			}, {})})
		})
	}

	refresh(moduleDetails) {
		client({method: 'GET', path: moduleDetails._links.self.href}).done(response => {
			let newModules = this.state.modules
			newModules[response.entity.deploymentId] = response.entity
			this.setState({modules: newModules})
		})
	}

	undeploy(moduleDetails) {
		client({method: 'DELETE', path: moduleDetails._links.self.href}).done(success => {
			this.refresh(moduleDetails)
		}, failure => {
			alert('FAILURE: ' + failure.entity.message)
		})
	}

	componentDidMount() {
		this.findModules()
	}

	render() {
		let modules = Object.keys(this.state.modules).map(name =>
			<Module key={name}
					details={this.state.modules[name]}
					refresh={this.refresh}
					undeploy={this.undeploy} />)

		return (
			<table className="table table--cosy table--rows">
				<tbody>
					{modules}
				</tbody>
			</table>
		)
	}

}

ReactDOM.render(
	<App/>,
	document.getElementById('app')
)

