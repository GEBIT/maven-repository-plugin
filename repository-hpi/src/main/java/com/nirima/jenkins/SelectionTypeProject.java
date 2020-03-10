/*
 * The MIT License
 *
 * Copyright (c) 2011, Nigel Magnay / NiRiMa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.nirima.jenkins;

import java.util.List;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.nirima.jenkins.action.ProjectRepositoryAction;
import com.nirima.jenkins.action.RepositoryAction;
import com.nirima.jenkins.repo.util.HudsonWalker;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.promoted_builds.PromotedBuildAction;
import jenkins.model.Jenkins;

public class SelectionTypeProject extends SelectionType {
    public String project;
    public String build;
    public String promoted;

    @DataBoundConstructor
    public SelectionTypeProject(String project, String build) {
        this.project = project;
        this.build = build;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String buildId) {
        this.build = buildId;
    }

    public String getPromoted() {
        return promoted;
    }

    @DataBoundSetter
    public void setPromoted(String promoted) {
        this.promoted = promoted;
    }

    @Override
    public RepositoryAction getAction(Run<?,?> theBuild) {

        int id;
        String suffix;

        if( build.equalsIgnoreCase("promotedRepository"))
        {
            suffix = "repository";
            id = getPromotedBuildNumber(project, promoted);
        }
        else if(build.equalsIgnoreCase("promotedRepositoryChain"))
        {
            suffix = "repositoryChain";
            id = getPromotedBuildNumber(project, promoted);
        }
        else
        {
            suffix = build;
            id = getLastSuccessfulBuildNumber(project);
        }

        return new ProjectRepositoryAction(project, id, suffix);
    }

	private Job getProject(final String project) {
		Job job = Iterables.find(Jenkins.get().getAllItems(Job.class), new Predicate<Job>() {

			@Override
			public boolean apply(Job buildableItem) {
				return buildableItem.getFullName().equals(project);
			}
		});
		return job;
    }

    private int getLastSuccessfulBuildNumber(final String project) {
    	Job item = getProject(project);

        return item.getLastSuccessfulBuild().getNumber();
    }

    private int getPromotedBuildNumber(final String project, final String promoted) {
    	Job item = getProject(project);

        Iterable<Run> promotedItems = Iterables.filter(item.getBuilds(), new Predicate<Run>() {
            @Override
			public boolean apply(Run run) {
                PromotedBuildAction pba = run.getAction(PromotedBuildAction.class);
                return ( pba != null && pba.getPromotion(promoted) != null );

            }
        });


        Ordering<Run> ordering = new Ordering<Run>() {
            @Override
            public int compare(Run l,  Run r) {
                return r.getNumber() - l.getNumber();
            }
        };

        try
        {
            return ordering.max(promotedItems).getNumber();
        }
        catch(Exception ex)
        {
            throw new RuntimeException("No promotion of type " + promoted + " in project " + project);
        }
    }

    @Symbol("upstreamProject")
    @Extension
    public static final class DescriptorImpl extends Descriptor<SelectionType> {

        @Override
        public String getDisplayName() {
            return "Project";
        }

        public List<Job> getJobs() {
            return HudsonWalker.getSupportedJobs();
        }
    }

}
