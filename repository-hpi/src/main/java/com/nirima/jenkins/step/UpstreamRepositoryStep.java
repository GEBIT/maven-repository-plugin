//
// UpstreamRepositoryStep.java
//
// Copyright (C) 2020
// GEBIT Solutions GmbH,
// Berlin, Duesseldorf, Stuttgart (Germany)
// All rights reserved.
//
package com.nirima.jenkins.step;

import java.net.MalformedURLException;
import java.util.Set;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.collect.ImmutableSet;
import com.nirima.jenkins.SelectionType;
import com.nirima.jenkins.action.RepositoryAction;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributingAction;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * @author Volodja
 */
public class UpstreamRepositoryStep extends Step {

	public SelectionType upstream;

	@DataBoundConstructor
	public UpstreamRepositoryStep(SelectionType upstream) {
		this.upstream = upstream;
	}

	public SelectionType getUpstream() {
		return upstream;
	}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new SynchronousStepExecution<Void>(context) {

			@Override
			protected Void run() throws Exception {
				TaskListener listener = getContext().get(TaskListener.class);
				Run build = getContext().get(Run.class);
				try {
					RepositoryAction repositoryAction = upstream.getAction(build);
					build.addAction(repositoryAction);
					String repoUrl = repositoryAction.getUrl().toExternalForm();
					build.addAction(getEnvContributingAction(repoUrl));
					listener.getLogger().println("Setting environment Jenkins.Repository = " + repoUrl);
				} catch (SelectionType.RepositoryDoesNotExistException x) {
					listener.getLogger().println("You asked for an upstream repository, but it does not exist");
					throw new RuntimeException(x);
				} catch (MalformedURLException e) {
					listener.getLogger().println("Problem setting upstream repository URL");
					throw new RuntimeException(e);
				}
				return null;
			}
		};
	}

	protected EnvironmentContributingAction getEnvContributingAction(String repoUrl) {
		return new EnvironmentContributingAction() {

			@Override
			public String getUrlName() {
				return null;
			}

			@Override
			public String getIconFileName() {
				return null;
			}

			@Override
			public String getDisplayName() {
				return null;
			}

			@Override
			public void buildEnvironment(Run<?, ?> build, EnvVars envVars) {
				envVars.put("Jenkins.Repository", repoUrl);
			}
		};
	}


	@Extension
	public static final class DescriptorImpl extends StepDescriptor {

		@Override
		public String getFunctionName() {
			return "upstreamRepository";
		}

		@Override
		public String getDisplayName() {
			return "Define Upstream Maven Repository";
		}

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return ImmutableSet.of(Run.class, EnvVars.class, TaskListener.class);
		}

	}

}
