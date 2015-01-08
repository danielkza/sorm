#!/bin/sh

mvn deploy -DskipTests=true -DaltDeploymentRepository=local::default::"file://$HOME/maven-repo"
