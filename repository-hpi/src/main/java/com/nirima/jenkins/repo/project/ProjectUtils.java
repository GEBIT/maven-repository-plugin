package com.nirima.jenkins.repo.project;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.nirima.jenkins.repo.RepositoryDirectory;
import com.nirima.jenkins.repo.RepositoryElement;
import hudson.model.*;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by magnayn on 12/10/2015.
 */
public class ProjectUtils {

	public static Collection<RepositoryElement> getChildren(final RepositoryDirectory parent, final Collection<?> items) {
		Iterator<RepositoryElement> iterator = Iterators.transform(items.iterator(),
				new Function<Object, RepositoryElement>() {
					public RepositoryElement apply(Object from) {
						if (from instanceof BuildableItemWithBuildWrappers) {
							return new ProjectElement(parent, ((BuildableItemWithBuildWrappers) from).asProject());
						}
						if (from instanceof ItemGroup) {
							return new ItemGroupDirectory(parent, (ItemGroup) from);
						}
						if (from instanceof Job) {
							return new ProjectElement(parent, (Job) from);
						}
						if (from instanceof ItemGroupDirectory) {
							return (ItemGroupDirectory) from;
						}

						return null;
					}
				});
		iterator = Iterators.filter(iterator, new Predicate<RepositoryElement>() {

			@Override
			public boolean apply(RepositoryElement input) {
				return input != null;
			}
		});
		List<RepositoryElement> elements = Lists.newArrayList(iterator);
		Collections.sort(elements, new Comparator<RepositoryElement>() {
			@Override
			public int compare(RepositoryElement element1, RepositoryElement element2) {
				return element1.getName().compareTo(element2.getName());
			}
		});
		return elements;
	}

	public static String sanitizeName(String name) {
		return name.replace("/", "-").replace("%2F", "-");
	}
}
