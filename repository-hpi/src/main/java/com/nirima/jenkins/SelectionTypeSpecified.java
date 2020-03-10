package com.nirima.jenkins;

import com.nirima.jenkins.action.PathInRepositoryAction;
import com.nirima.jenkins.action.RepositoryAction;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Run;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;


public class SelectionTypeSpecified extends SelectionType  {
    public String path;

    @DataBoundConstructor
    public SelectionTypeSpecified(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public RepositoryAction getAction(Run<?,?> build) {
        return new PathInRepositoryAction(path);
    }

    @Symbol("upstreamPath")
    @Extension
    public static final class DescriptorImpl extends Descriptor<SelectionType> {

         @Override
         public String getDisplayName() {
             return "Specified Path in Repository";
         }
     }
}
