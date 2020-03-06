//
// ArtifactRepositoryContent.java
//
// Copyright (C) 2020
// GEBIT Solutions GmbH, 
// Berlin, Duesseldorf, Stuttgart (Germany)
// All rights reserved.
//
package com.nirima.jenkins.repo;

import hudson.model.Run;

/**
 * 
 * @author Volodja 
 */
public interface ArtifactRepositoryContent extends RepositoryContent {

	Run<?,?> getBuild();
}
