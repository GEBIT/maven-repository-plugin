package com.nirima.jenkins.repo.build;

import com.nirima.jenkins.repo.AbstractRepositoryElement;
import com.nirima.jenkins.repo.ArtifactRepositoryContent;
import com.nirima.jenkins.repo.util.MavenArtifactData;

import hudson.model.Run;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Date;

import jenkins.util.VirtualFile;

public class MavenArtifactDataRepositoryItem extends AbstractRepositoryElement implements ArtifactRepositoryContent {

    private final Run<?, ?> build;
    private final MavenArtifactData artifact;
    private final boolean timestampedSnapshot;

    public MavenArtifactDataRepositoryItem(Run<?, ?> build, MavenArtifactData artifact, boolean timestampedSnapshot) {
        super(null);
        this.build = build;
        this.artifact = artifact;
        this.timestampedSnapshot = timestampedSnapshot;
    }

    @Override
    public Run<?, ?> getBuild() {
        return build;
    }

    @Override
    public InputStream getContent() throws IOException {
        return getFile().open();
    }

    @Override
    public Date getLastModified() {
        try {
            return new Date(getFile().lastModified());
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Unable to read artifact modification time: " + artifact.getRelativePath(), e);
        }
    }

    @Override
    public Long getSize() {
        try {
            return getFile().length();
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read artifact size: " + artifact.getRelativePath(), e);
        }
    }

    @Override
    public String getContentType() {
        if (artifact.isPom()) {
            return "application/xml";
        }
        if (artifact.isJar()) {
            return "application/java-archive";
        }
        return null;
    }

    @Override
    public String getPath() {
        return artifact.getGroupId().replace('.', '/')
                + "/" + artifact.getArtifactId()
                + "/" + artifact.getVersion()
                + "/" + getName();
    }

    @Override
    public String getName() {
        String fileName = artifact.getFileName();
        if (timestampedSnapshot && artifact.isSnapshot()) {
            String version = MetadataRepositoryItem.formatDateVersion(build);
            fileName = fileName.replace("SNAPSHOT", version);
        }
        return fileName;
    }

    @Override
    public String getDescription() {
        return "From Build #" + build.getNumber() + " of " + build.getParent().getFullName();
    }

    public boolean fileExists() {
        try {
            VirtualFile file = getFile();
            return file.isFile() && file.canRead();
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to inspect artifact: " + artifact.getRelativePath(), e);
        }
    }

    private VirtualFile getFile() throws IOException {
        return build.getArtifactManager().root().child(artifact.getRelativePath());
    }
}
