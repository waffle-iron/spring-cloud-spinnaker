'use strict';

const React = require('react')

class Module extends React.Component {

	constructor(props) {
		super(props);
		this.handleRefresh = this.handleRefresh.bind(this)
		this.handleDeploy = this.handleDeploy.bind(this)
		this.handleUndeploy = this.handleUndeploy.bind(this)
	}

	handleRefresh(e) {
		e.preventDefault()
		this.props.refresh(this.props.details)
	}

	handleDeploy(e) {
		e.preventDefault()
		this.props.deploy(this.props.details)
	}
	handleUndeploy(e) {
		e.preventDefault()
		this.props.undeploy(this.props.details)
	}

	render() {
		let bits = ['deploymentId', 'state'].map(name => {
			return <td key={name}>{this.props.details[name]}</td>
		})

		// TODO: Disable Refresh button in the midst of a refresh operation

		return (
			<tr>
				{bits}
				<td><button onClick={this.handleRefresh}>Refresh</button></td>
				<td><button onClick={this.handleDeploy}>Deploy</button></td>
				<td><button onClick={this.handleUndeploy}>Undeploy</button></td>
			</tr>
		)
	}
}

module.exports = Module