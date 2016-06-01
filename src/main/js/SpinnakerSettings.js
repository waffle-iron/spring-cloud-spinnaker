'use strict'

const React = require('react')

class SpinnakerSettings extends React.Component {

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
		let lineItemLayout = 'control-group'
		return (
			<div>
				<ul className="layout">
					<li className={lineItemLayout}>
						<label className={labelLayout}>Target API</label>
						<input className={inputLayout} type="text"
							   name="spinnaker.api"
							   onChange={this.handleChange} />
					</li>
					<li className={lineItemLayout}>
						<label className={labelLayout}>Target Organization</label>
						<input className={inputLayout} type="text"
							   name="spinnaker.org"
							   onChange={this.handleChange} />
					</li>
					<li className={lineItemLayout}>
						<label className={labelLayout}>Target Space</label>
						<input className={inputLayout} type="text"
							   name="spinnaker.space"
							   onChange={this.handleChange} />
					</li>
					<li className={lineItemLayout}>
						<label className={labelLayout}>Target Email</label>
						<input className={inputLayout} type="text"
							   name="spinnaker.email"
							   onChange={this.handleChange} />
					</li>
					<li className={lineItemLayout}>
						<label className={labelLayout}>Target Password</label>
						<input className={inputLayout} type="password"
							   name="spinnaker.password"
							   onChange={this.handleChange} />
					</li>
				</ul>
			</div>
		)
	}

}

module.exports = SpinnakerSettings