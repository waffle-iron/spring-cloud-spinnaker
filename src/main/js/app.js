'use strict';

const React = require('react')
const ReactDOM = require('react-dom')

const Settings = require('./Settings')

const Modules = require('./Modules')

ReactDOM.render(
	<Settings/>,
	document.getElementById('all-settings')
)

ReactDOM.render(
	<Modules/>,
	document.getElementById('modules')
)

