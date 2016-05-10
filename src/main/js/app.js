'use strict';

const React = require('react')
const ReactDOM = require('react-dom')

const Settings = require('./Settings')
const Modules = require('./Modules')

class Application extends React.Component {

	constructor(props) {
		super(props)
		this.state = {
			services: 'spring.cloud.deployer.cloudfoundry.defaults.services',
			accountName: 'cf.account.name',
			accountPassword: 'cf.account.password',
			repoUsername: 'cf.repo.username',
			repoPassword: 'cf.repo.password',
			springConfigLocation: 'spring.config.location'
		}
		this.updateSetting = this.updateSetting.bind(this)
	}

	updateSetting(key, value) {
		let newState = {}
		newState[key] = value
		this.setState(newState)
	}

	render() {
		return (
			<section className="box box--tiny content__container">
				<div id="settings" className="content wrapper active">
					<h1>Spinnaker Settings</h1>
					<Settings updateSetting={this.updateSetting} />
				</div>

				<div id="status" className="content wrapper">
					<h1>Spinnaker Status</h1>
					<Modules settings={this.state} />
				</div>
			</section>
		)
	}
}

ReactDOM.render(
	<Application />,
	document.getElementById('app')
)

