package com.nirima.jenkins.repo;

import hudson.model.Run;

public interface ArtifactRepositoryContent extends RepositoryContent {

    Run<?, ?> getBuild();
}
