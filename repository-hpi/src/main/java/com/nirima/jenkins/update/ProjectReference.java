package com.nirima.jenkins.update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

import hudson.model.Job;
import hudson.model.Run;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;

/**
 * Store a reference to a project.
 */
public class ProjectReference implements Serializable {
  public final String project;
  public final String buildNumber;

  private ProjectReference(String project, String buildNumber) {
    this.project = project;
    this.buildNumber = buildNumber;
  }

  public static ProjectReference read(BufferedReader br) throws IOException {

    String project = br.readLine();
    String buildNumber = br.readLine();
    return new ProjectReference(project, buildNumber);
  }

  public Run getBuild() {
    return getBuild(project, buildNumber);
  }

  private Run getBuild(String project, String buildNumber) {

    if( project.indexOf(' ') > 0 ) {
      String[] elements = project.split(" ");
      return getMultiBranchProject(elements[0],elements[1], Integer.parseInt(buildNumber));
    }

    Run build = null;
    for (Job j : Jenkins.getInstance().getAllItems(Job.class)) {
      if (j.getFullName().equals(project)) {
        // Correct job
        build = j.getBuildByNumber(Integer.parseInt(buildNumber));
      }
    }
    return build;
  }

  private Run getMultiBranchProject(String element, String job, int buildNumber) {
    for (MultiBranchProject j : Jenkins.getInstance().getAllItems(MultiBranchProject.class)) {
      if (j.getFullName().equals(element)) {
        for (Object j2 : j.getAllJobs() )  {
          if (((Job)j2).getFullName().equals(job)) {
            // Correct job
            return ((Job)j2).getBuildByNumber(buildNumber);
          }
        }
      }
    }
    return null;
  }



}
