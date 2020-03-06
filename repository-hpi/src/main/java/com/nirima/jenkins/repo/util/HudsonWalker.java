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
package com.nirima.jenkins.repo.util;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import com.nirima.jenkins.action.ProjectRepositoryAction;
import com.nirima.jenkins.action.RepositoryAction;
import com.nirima.jenkins.update.RepositoryArtifactRecord;

import com.nirima.jenkins.update.RepositoryArtifactRecords;

import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSetBuild;
import hudson.maven.reporters.MavenArtifact;
import hudson.maven.reporters.MavenArtifactRecord;
import hudson.model.*;
import jenkins.model.Jenkins;

import org.jenkinsci.plugins.pipeline.maven.GlobalPipelineMavenConfig;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: magnayn
 * Date: 02/03/2011
 * Time: 16:19
 * To change this template use File | Settings | File Templates.
 */
public class HudsonWalker {

    private static final Logger log = LoggerFactory.getLogger(HudsonWalker.class);

    /**
     * visit everything in order.
     *
     * @param visitor
     */
    public static void traverse(HudsonVisitor visitor) {
        for (Job item : HudsonWalker.getSupportedJobs()) {

            visitor.visitProject(item);

            List<? extends Run> runs = item.getBuilds();
            for (Run run : runs) {
                traverse(visitor, run);
            }
        }
    }

    /**
     * Visit projects and builds
     */
    public static void traverseProjectsAndBuilds(HudsonVisitor visitor ) {
        for (Job item : HudsonWalker.getSupportedJobs()) {

            visitor.visitProject(item);

            List<? extends Run> runs = item.getBuilds();
            for (Run run : runs) {
                 if (run instanceof MavenModuleSetBuild) {
                    MavenModuleSetBuild mmsb = (MavenModuleSetBuild) run;

                    visitor.visitModuleSet(mmsb);
                 }
            }
        }
    }


    /**
     * visit project chain, from current through parents.
     * @param visitor
     * @param run
     */
    public static void traverseChain(HudsonVisitor visitor, Run run)
    {
        if( run == null )
            return;

        traverse(visitor, run);

        RepositoryAction repositoryAction = run.getAction(RepositoryAction.class);

        if( repositoryAction != null ) {
            if( repositoryAction instanceof ProjectRepositoryAction ) {
                final ProjectRepositoryAction projectRepositoryAction = (ProjectRepositoryAction) repositoryAction;

                Job item = (Job)Jenkins.get().getItem(projectRepositoryAction.getProjectName(), (Item)null);
				if (item == null) {
					throw new RuntimeException("Project " + projectRepositoryAction.getProjectName() + " not found.");
				}

                Optional<Run> r = Iterables.tryFind(item.getBuilds(), new Predicate<Run>() {
                    @Override
					public boolean apply(Run run) {
                        return run.getNumber() == projectRepositoryAction.getBuildNumber();
                    }
                });

                if( r.isPresent() )
                    traverseChain(visitor, r.get());
            }
        }

    }

    /**
     * visit a run
     * @param visitor
     * @param run
     */
    public static void traverse(HudsonVisitor visitor, Run run) {
        if (run instanceof MavenModuleSetBuild) {
            MavenModuleSetBuild item = (MavenModuleSetBuild) run;

            visitor.visitModuleSet(item);

            Map<MavenModule, List<MavenBuild>> modulesMap = item.getModuleBuilds();

            for (List<MavenBuild> builds : modulesMap.values()) {
                for (MavenBuild build : builds) {

                    log.trace("Visit mavenBuild {}", build);

                    visitor.visitBuild(build);

                    MavenArtifactRecord artifacts = build.getAction(MavenArtifactRecord.class);
                    if( artifacts != null ) {
                        visitMavenArtifactRecord(visitor, build, artifacts);
                    }

                }

            }
        } else {
        	boolean handled = visitPipelineMavenArtifacts(visitor, run);
			if (!handled) {
				RepositoryArtifactRecords records = run.getAction(RepositoryArtifactRecords.class);
				if (records != null) {
					for (RepositoryArtifactRecord record : records.recordList) {
						visitRepositoryRecord(visitor, run, record);
					}
				}
			}
        }
    }

	private static boolean visitPipelineMavenArtifacts(HudsonVisitor visitor, Run build) {
		boolean handled = false;
		try {
			if (Jenkins.get().getPlugin("workflow-job") != null && build instanceof WorkflowRun) {
				if (Jenkins.get().getPlugin("pipeline-maven") != null) {
					List<org.jenkinsci.plugins.pipeline.maven.MavenArtifact> artifacts =
							GlobalPipelineMavenConfig.get()
									.getDao()
									.getGeneratedArtifacts(build.getParent().getFullName(), build.getNumber());
					log.trace("Visit Build {} artifacts {}", build, artifacts);
					if (artifacts != null && artifacts.size() > 0) {
						for (org.jenkinsci.plugins.pipeline.maven.MavenArtifact artifact : artifacts) {
							visitor.visitArtifact(build,
									new MavenArtifactData(artifact.getGroupId(),
											artifact.getArtifactId(),
											artifact.getBaseVersion(),
											artifact.getClassifier(),
											artifact.getType(),
											artifact.getFileNameWithBaseVersion(),
											artifact.isSnapshot()));
						}
					}
					handled = true;
				}
			}
		} catch (Throwable exc) {
            log.error("Error fetching artifact details");
            log.error("Error", exc);
			// class loading errors becaue of missing plugins? ignore!
		}
		return handled;
	}

	private static void visitRepositoryRecord(HudsonVisitor visitor, Run build,
                                              RepositoryArtifactRecord artifacts) {
        log.trace("Visit Build {} artifacts {}", build, artifacts);
        try {
            visitor.visitArtifact(build, artifacts.pomArtifact);

            if (artifacts.mainArtifact != artifacts.pomArtifact) {
                // Sometimes the POM is the only thing being made..
                visitor.visitArtifact(build, artifacts.mainArtifact);
            }
            for (MavenArtifact art : artifacts.attachedArtifacts) {
                visitor.visitArtifact(build, art);
            }
        }
        catch(Exception ex) {
            log.error("Error fetching artifact details");
            log.error("Error", ex);
        }

    }

    private static void visitMavenArtifactRecord(HudsonVisitor visitor, Run build, MavenArtifactRecord artifacts) {

        log.trace("Visit Build {} artifacts {}", build, artifacts);
        try {
            visitor.visitArtifact(build, artifacts.pomArtifact);

            if (artifacts.mainArtifact != artifacts.pomArtifact) {
                // Sometimes the POM is the only thing being made..
                visitor.visitArtifact(build, artifacts.mainArtifact);
            }
            for (MavenArtifact art : artifacts.attachedArtifacts) {
                visitor.visitArtifact(build, art);
            }
        }
        catch(Exception ex) {
            log.error("Error fetching artifact details");
            log.error("Error", ex);
        }

    }

    public static List<Job> getSupportedJobs() {
    	List<Job> jobs = new LinkedList<>();
		jobs.addAll(Jenkins.get()
				.getAllItems(BuildableItemWithBuildWrappers.class)
				.stream()
				.map(job -> (Job) job.asProject())
				.collect(Collectors.toList()));
		if (Jenkins.get().getPlugin("workflow-job") != null) {
			// NOTE: we are using iterator here to avoid class loading triggered by class instantiating and not by
			// execution.
			// See https://wiki.jenkins.io/display/JENKINS/Tips+for+optional+dependencies
			List<WorkflowJob> pipelineJobs = Jenkins.get().getAllItems(WorkflowJob.class);
			for (Iterator<WorkflowJob> it = pipelineJobs.iterator(); it.hasNext();) {
				jobs.add(it.next());
			}
		}
        Collections.sort(jobs, new Comparator<Job>() {
            @Override
            public int compare(Job item1, Job item2) {
                return item1.getFullName().compareTo(item2.getFullName());
            }
        });
        return jobs;
    }

}
