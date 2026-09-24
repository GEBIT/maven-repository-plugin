package com.nirima.jenkins.repo.util;

public final class MavenArtifactData {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String classifier;
    private final String type;
    private final String fileName;
    private final boolean snapshot;

    public MavenArtifactData(
            String groupId,
            String artifactId,
            String version,
            String classifier,
            String type,
            String fileName,
            boolean snapshot) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.type = type;
        this.fileName = fileName;
        this.snapshot = snapshot;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getType() {
        return type;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isSnapshot() {
        return snapshot;
    }

    public boolean isPom() {
        return "pom".equalsIgnoreCase(type);
    }

    public boolean isJar() {
        return "jar".equalsIgnoreCase(type);
    }

    public String getRelativePath() {
        return groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + fileName;
    }
}
