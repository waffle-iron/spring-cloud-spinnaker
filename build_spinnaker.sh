#!/usr/bin/env bash


# Build a module
build() {

	echo "+++ Moving $1 to /tmp..."
	mv $1 /tmp

	echo "+++ Setting aside $1's .git folder..."
	mv /tmp/$1/.git /tmp/$1/backup.git

	echo "+++ Moving over $1's actual .git folder..."
	mv .git/modules/$1 /tmp/$1/.git

	echo "+++ Building $1..."
	pushd /tmp/$1
	./gradlew -DspringBoot.repackage=true clean build
	popd

	echo "+++ Moving $1's actual .git folder back..."
	mv /tmp/$1/.git .git/modules/$1

	echo "+++ Restoring $1's submodule .git folder..."
	mv /tmp/$1/backup.git /tmp/$1/.git

	echo "+++ Moving $1 back from /tmp..."
	mv /tmp/$1 .

}

build clouddriver
build echo
build front50
build gate
build igor
build orca


