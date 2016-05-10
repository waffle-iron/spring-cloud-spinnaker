'use strict'

const React = require('react')

class Settings extends React.Component {

	constructor(props) {
		super(props)
		this.state = {}
		this.handleChange = this.handleChange.bind(this)
	}

	handleChange(e) {
		e.preventDefault()
		this.props.updateSetting(e.target.name, e.target.value)
	}

	render() {
		return (
			<div>
				<ul>
					<li>
						<label>Redis Service</label>
						<input type="text" name="spinnaker-redis" onChange={this.handleChange} />
					</li>
					<li>
						<label>Account Name</label>
						<input type="text" name="cf.account.name" onChange={this.handleChange} />
					</li>
					<li>
						<label>Account Password</label>
						<input type="password" name="cf.account.password" onChange={this.handleChange} />
					</li>
					<li>
						<label>Repository Name/Access Code</label>
						<input type="text" name="cf.repo.username" onChange={this.handleChange} />
					</li>
					<li>
						<label>Repository Password/Secret Code</label>
						<input type="text" name="cf.repo.password" onChange={this.handleChange} />
					</li>
					<li>
						<label>Spring Config Location</label>
						<input type="text" name="spring.config.location" onChange={this.handleChange} />
					</li>
				</ul>
			</div>
		)
	}

}

module.exports = Settings