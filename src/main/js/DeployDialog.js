'use strict';

const React = require('react')
const client = require('./client')

class DeployDialog extends React.Component {

	constructor(props) {
		super(props)
		this.state = {data: null}
		this.handleSubmit = this.handleSubmit.bind(this)
		this.handleFile = this.handleFile.bind(this)
	}

	handleSubmit(e) {
		e.preventDefault()
		console.log('Submitting file for deployment...')
		console.log(e)
		client({
			method: 'POST',
			path: this.props.details._links.self.href,
			entity: {file: this.state.data},
			headers: {'Content-Type': 'application/json'}
		}).done(response => {
			self.setState({data: null})
		})
		window.location = "#"
	}

	handleFile(e) {
		e.preventDefault()
		console.log(e);
		let reader = new FileReader()
		let file = e.target.files[0]

		reader.onload = upload => {
			this.setState({data: upload.target.result})
		}

		reader.readAsDataURL(file)
	}

	render() {
		let dialogId = "deployModule-" + this.props.details._links.self.href;

		return (
			<div>
				<a href={'#' + dialogId}>Deploy</a>

				<div id={dialogId} className="modalDialog">
					<div>
						<a href="#" title="Close" className="close">X</a>

						<h2>Deploy {this.props.details.deploymentId}</h2>

						<form onSubmit={this.handleSubmit} encType="multipart/form-data">
							<input type="file" onChange={this.handleFile}></input>
							<input type="submit" value="Deploy"></input>
						</form>
					</div>
				</div>
			</div>
		)
	}

}

module.exports = DeployDialog