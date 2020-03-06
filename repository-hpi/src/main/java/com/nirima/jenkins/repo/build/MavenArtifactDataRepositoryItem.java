//
// MavenArtifactDataRepositoryItem.java
//
// Copyright (C) 2020
// GEBIT Solutions GmbH,
// Berlin, Duesseldorf, Stuttgart (Germany)
// All rights reserved.
//
package com.nirima.jenkins.repo.build;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.nirima.jenkins.repo.AbstractRepositoryElement;
import com.nirima.jenkins.repo.ArtifactRepositoryContent;
import com.nirima.jenkins.repo.util.MavenArtifactData;

import hudson.model.Run;
import jenkins.util.VirtualFile;

/**
 * @author Volodja
 */
public class MavenArtifactDataRepositoryItem extends AbstractRepositoryElement implements ArtifactRepositoryContent {

	private Run<?, ?> build;

	private MavenArtifactData artifact;

	private boolean timestampedSnapshot;

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
	public InputStream getContent() throws Exception {
		return getFile().open();
	}

	@Override
	public Date getLastModified() {
		try {
			return new Date(getFile().lastModified());
		} catch (IOException exc) {
			return new Date();
		}
	}

	@Override
	public Long getSize() {
		try {
			return getFile().length();
		} catch (IOException exc) {
			return 0L;
		}
	}

	@Override
	public String getContentType() {
		if (artifact.isPom()) {
			return "application/xml";
		} else if (artifact.isJar()) {
			return "application/java-archive";
		}
		return null; // We don't know..
	}

	@Override
	public String getPath() {
		return artifact.getGroupId().replace('.', '/')
				+ "/"
				+ artifact.getArtifactId()
				+ "/"
				+ artifact.getVersion()
				+ "/"
				+ getName();
	}

	@Override
	public String getName() {
		String fileName = artifact.getFileName();
		if (timestampedSnapshot) {
			if (artifact.isSnapshot()) {
				String vers = MetadataRepositoryItem.formatDateVersion(build);
				fileName = StringUtils.replace(fileName, "SNAPSHOT", vers);
			}
		}
		return fileName;
	}

	@Override
	public String getDescription() {
		return "From Build #" + build.getNumber() + " of " + build.getDisplayName();
	}

	private VirtualFile getFile() throws IOException {
		return build.getArtifactManager().root().child(artifact.getRelativePath());
	}

	public boolean fileExists() {
		try {
			return getFile().isFile() && getFile().canRead();
		} catch (IOException ise) {
			return false;
		}
	}

}
