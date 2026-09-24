package com.nirima.jenkins.repo.build;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nirima.jenkins.repo.ArtifactRepositoryContent;
import com.nirima.jenkins.repo.util.MavenArtifactData;

import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.model.Run;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MetadataRepositoryItemTest {
    @Ignore
    @Test
    public void formatDateVersion_run_formatted() {
        //arrange
        String expected = "19700101.010001-0";
        long timeInMilliSeconds = 1000L;
        Run<?, ?> buildRun = createMavenBuildMock(timeInMilliSeconds);
        //act
        String actual = MetadataRepositoryItem.formatDateVersion(buildRun);
        //assert
        assertEquals(expected, actual);
    }

    @Test
    public void generateContent_usesLatestPipelineArtifactMetadata() {
        Run<?, ?> buildRun = mock(Run.class);
        when(buildRun.getTime()).thenReturn(new Date(1_000L));
        when(buildRun.getNumber()).thenReturn(1);

        Run<?, ?> latestBuildRun = mock(Run.class);
        when(latestBuildRun.getTime()).thenReturn(new Date(2_000L));
        when(latestBuildRun.getNumber()).thenReturn(2);

        ArtifactRepositoryContent firstItem = mock(ArtifactRepositoryContent.class);
        doReturn(buildRun).when(firstItem).getBuild();
        when(firstItem.getLastModified()).thenReturn(new Date(1_000L));

        ArtifactRepositoryContent latestItem = mock(ArtifactRepositoryContent.class);
        doReturn(latestBuildRun).when(latestItem).getBuild();
        when(latestItem.getLastModified()).thenReturn(new Date(2_000L));

        MavenArtifactData artifact = new MavenArtifactData(
                "com.example", "demo", "1.0-SNAPSHOT", "sources", "jar", "demo-1.0-SNAPSHOT-sources.jar", true);
        MetadataRepositoryItem metadata =
                new MetadataRepositoryItem(buildRun, "com.example", "demo", "1.0-SNAPSHOT");
        metadata.addArtifact(artifact, firstItem);
        metadata.addArtifact(artifact, latestItem);

        String actual = metadata.generateContent();
        String expectedValue = "<value>1.0-"
                + MetadataRepositoryItem.formatDateVersion(latestBuildRun)
                + "</value>";

        assertTrue(actual.contains("<classifier>sources</classifier>"));
        assertTrue(actual.contains("<extension>jar</extension>"));
        assertTrue(actual.contains(expectedValue));
        assertFalse(actual.contains("<value>1.0-SNAPSHOT</value>"));
    }

    
//    @Test
//    public void getContent_artifactWithModificatedDate_formattedInUpdatedElement() throws Exception {
//        //arrange
//        String expected = "<updated>19700101010001</updated>";
//        long timeInMilliSeconds = 1000L;
//        MavenArtifact artifact=new MavenArtifact("","","","","","","");
//        MetadataRepositoryItem testee = new MetadataRepositoryItem(mock(MavenBuild.class), artifact);
//        ArtifactRepositoryItem item = createArtifactRepositoryItemMock(timeInMilliSeconds);
//        testee.addArtifact(artifact, item);
//        //act
//        String actual = testee.generateContent();
//        //assert
//        assertTrue(actual.contains(expected));
//    }

//    private ArtifactRepositoryItem createArtifactRepositoryItemMock(long timeInMilliSeconds) {
//        ArtifactRepositoryItem item=mock(ArtifactRepositoryItem.class);
//        MavenBuild mavenBuild = createMavenBuildMock(timeInMilliSeconds);
//        when(item.getBuild()).thenReturn(mavenBuild);
//        when(item.getLastModified()).thenReturn(mavenBuild.getTime());
//        return item;
//    }


    private MavenBuild createMavenBuildMock(long timeInMilliSeconds) {
        MavenModule job=mock(MavenModule.class);
        Calendar calendar=new GregorianCalendar();
        calendar.setTimeInMillis(timeInMilliSeconds);
        return new MavenBuild(job,calendar);
    }

}
