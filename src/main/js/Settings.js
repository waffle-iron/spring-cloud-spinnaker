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
		let labelLayout = 'layout__item u-1/2-lap-and-up u-1/4-desk'
		let inputLayout = 'layout__item u-1/2-lap-and-up u-1/2-desk'
		return (
			<div>
				<ul className="layout">
					<li>
						<label className={labelLayout}>Redis Service</label>
						<input className={inputLayout} type="text" name="spring.cloud.deployer.cloudfoundry.defaults.services" onChange={this.handleChange} />
					</li>
					<li>
						<label className={labelLayout}>Account Name</label>
						<input className={inputLayout} type="text" name="cf.account.name" onChange={this.handleChange} />
					</li>
					<li>
						<label className={labelLayout}>Account Password</label>
						<input className={inputLayout} type="password" name="cf.account.password" onChange={this.handleChange} />
					</li>
					<li>
						<label className={labelLayout}>Repository Name/Access Code</label>
						<input className={inputLayout} type="text" name="cf.repo.username" onChange={this.handleChange} />
					</li>
					<li>
						<label className={labelLayout}>Repository Password/Secret Code</label>
						<input className={inputLayout} type="text" name="cf.repo.password" onChange={this.handleChange} />
					</li>
					<li>
						<label className={labelLayout}>Spring Config Location</label>
						<input className={inputLayout} type="text" name="spring.config.location" onChange={this.handleChange} />
					</li>
				</ul>
			</div>
		)
	}

}

module.exports = Settings