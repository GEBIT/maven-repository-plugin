//
// MavenArtifactData.java
//
// Copyright (C) 2020
// GEBIT Solutions GmbH,
// Berlin, Duesseldorf, Stuttgart (Germany)
// All rights reserved.
//
package com.nirima.jenkins.repo.util;

/**
 * @author Volodja
 */
public class MavenArtifactData {

	private String groupId;

	private String artifactId;

	private String version;

	private String classifier;

	private String type;

	private String fileName;

	private boolean snapshot;

	public MavenArtifactData(String groupId, String artifactId, String version, String classifier, String type,
			String fileName, boolean snapshot) {
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

	public String getFileName() {
		return fileName;
	}

	public boolean isPom() {
		return "pom".equalsIgnoreCase(type);
	}

	public boolean isJar() {
		return "jar".equalsIgnoreCase(type);
	}

	public String getRelativePath() {
		String fileSeparatorOnAgent = "/";
		return getGroupId().replace(".", fileSeparatorOnAgent)
				+ fileSeparatorOnAgent
				+ getArtifactId()
				+ fileSeparatorOnAgent
				+ getVersion()
				+ fileSeparatorOnAgent
				+ getFileName();
	}

	public boolean isSnapshot() {
		return snapshot;
	}
	
	public String getClassifier() {
		return classifier;
	}
	
	public String getType() {
		return type;
	}

}
