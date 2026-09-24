package com.nirima.jenkins;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nirima.jenkins.repo.RepositoryDirectory;

import hudson.Util;

import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class RepositoryPluginTest {

    @Test
    public void printHeaderEscapesDirectoryPath() throws Exception {
        RepositoryDirectory directory = mock(RepositoryDirectory.class);
        when(directory.getPath()).thenReturn("<script>alert(1)</script>");
        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getContextPath()).thenReturn("");
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        new RepositoryPlugin().printHeader(output, request, directory);

        String html = output.toString(StandardCharsets.UTF_8);
        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("Index of &lt;script&gt;alert(1)&lt;/script&gt;"));
    }

    @Test
    public void printDirEntryEscapesNameAndDescriptionAndEncodesHref() throws Exception {
        String name = "javascript:alert(1)\"><script>alert(1)</script>";
        String description = "Project <img src=x onerror=alert(1)>";
        RepositoryDirectory item = mock(RepositoryDirectory.class);
        when(item.getName()).thenReturn(name);
        when(item.getDescription()).thenReturn(description);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        new RepositoryPlugin().printDirEntry(output, item);

        String html = output.toString(StandardCharsets.UTF_8);
        assertFalse(html.contains("<script>"));
        assertFalse(html.contains("<img"));
        assertFalse(html.contains("href=\"javascript:"));
        assertTrue(html.contains("href=\"" + Util.escape(Util.fullEncode(name) + "/") + "\""));
        assertTrue(html.contains(Util.escape(name + "/") + "</a>"));
        assertTrue(html.contains(Util.escape(description)));
    }
}
