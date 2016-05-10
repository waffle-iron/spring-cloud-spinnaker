'use strict';

const React = require('react')
const ReactDOM = require('react-dom')

const Settings = require('./Settings')
const Modules = require('./Modules')

class Application extends React.Component {

	constructor(props) {
		super(props)
		this.state = {}
		this.updateSetting = this.updateSetting.bind(this)
	}

	updateSetting(key, value) {
		let newState = {}
		newState[key] = value
		this.setState(newState)
		console.log(this.state);
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

