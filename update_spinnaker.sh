#!/usr/bin/env bash


# Build a module
update() {

	echo "+++ Updating $1..."
	pushd $1
    git pull
	popd

}

update clouddriver
update echo
update front50
update gate
update igor
update orca


