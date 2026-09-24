package com.nirima.jenkins;

import com.nirima.jenkins.bridge.BridgeRepository;
import com.nirima.jenkins.repo.build.ProjectBuildRepositoryRoot;

import hudson.Extension;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.Action;
import hudson.model.Run;

import jenkins.model.TransientActionFactory;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;
import javax.servlet.ServletException;

/**
 * Exposes a per-build repository under the URL of a build.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class TransientBuildActionFactoryImpl extends TransientActionFactory<Run> {
    @Inject
    RepositoryPlugin plugin;

    @Override
    public Class<Run> type() {
        return Run.class;
    }

    @Override
    public Collection<? extends Action> createFor(Run build) {
        if (build instanceof MavenModuleSetBuild || build.getHasArtifacts()) {
            return Collections.singleton(new BuildActionImpl(build));
        }
        return Collections.emptyList();
    }

    public class BuildActionImpl implements Action {
        private final Run build;

        public BuildActionImpl(Run build) {
            this.build = build;
        }

        public String getIconFileName() {
            return plugin.getIconFileName();
        }

        public String getDisplayName() {
            return "Build Artifacts As Maven Repository";
        }

        public String getUrlName() {
            return "maven-repository";
        }

        public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            plugin.serveRequest(
                    new BridgeRepository(new ProjectBuildRepositoryRoot(null, build, build.getFullDisplayName()), null),
                    req.findAncestor(this).getUrl());
        }
    }
}
