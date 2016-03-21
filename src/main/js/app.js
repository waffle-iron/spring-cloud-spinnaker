'use strict';

const React = require('react')
const client = require('./client')
const when = require('when')
const follow = require('./follow')

const root = '/api'

class App extends React.Component {

	constructor(props) {
		super(props);
		this.state = {modules: []}
		this.findModules = this.findModules.bind(this)
	}

	findModules() {
		follow(client, root, ['modules']).done(response => {
			this.setState({modules: response.entity._embedded.appStatusList})
			console.log(response.entity._embedded.appStatusList);
		})
	}

	componentDidMount() {
		this.findModules()
	}

	render() {
		var modules = this.state.modules.map(module => <Module details={module} />)

		return (
			<ul className="layout">
				{modules}
			</ul>
		)
	}

}

class Module extends React.Component {

	constructor(props) {
		super(props);
	}

	render() {
		var bits = Object.getOwnPropertyNames(this.props.details).map(name => {
			if (name !== '_links') {
				return <div>{this.props.details[name]}</div>
			}
		})

		return (
			<li className="layout__item u-1/2-lap-and-up">
				{bits}
			</li>
		)
	}

}

React.render(
	<App/>,
	document.getElementById('app')
)

