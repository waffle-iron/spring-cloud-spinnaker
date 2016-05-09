'use strict'

const React = require('react')

class Settings extends React.Component {

	constructor(props) {
		super(props)
	}

	render() {
		return (
			<div>
				<ul>
					<li>
						<p><label>Redis Service</label></p>
						<input type="text" name="spinnaker-redis" />
					</li>
					<li>
						<label>Account Name</label>
						<input type="text" name="cf.account.name" />
					</li>
					<li>
						<label>Account Password</label>
						<input type="password" name="cf.account.password" />
					</li>
					<li>
						<label>Repository Name/Access Code</label>
						<input type="text" name="cf.repo.username" />
					</li>
					<li>
						<label>Repository Password/Secret Code</label>
						<input type="text" name="cf.repo.password" />
					</li>
					<li>
						<label>Spring Config Location</label>
						<input type="text" name="spring.config.location" />
					</li>
				</ul>
			</div>
		)
	}

}

module.exports = Settings