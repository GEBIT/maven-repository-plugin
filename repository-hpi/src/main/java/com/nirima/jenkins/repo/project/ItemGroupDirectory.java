package com.nirima.jenkins.repo.project;

import com.nirima.jenkins.repo.AbstractRepositoryDirectory;
import com.nirima.jenkins.repo.RepositoryDirectory;
import com.nirima.jenkins.repo.RepositoryElement;
import hudson.model.Item;
import hudson.model.ItemGroup;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created by marcel on 03.05.17.
 */
public class ItemGroupDirectory extends AbstractRepositoryDirectory implements RepositoryDirectory {

    private final ItemGroup<?> itemGroup;

    public ItemGroupDirectory(RepositoryDirectory parent, ItemGroup<?> itemGroup) {
        super(parent);
        this.itemGroup = itemGroup;
    }

    @Override
    public String getName() {
        String name = itemGroup instanceof Item ? ((Item) itemGroup).getName() : itemGroup.getDisplayName();
        return ProjectUtils.sanitizeName(name);
    }

    @Nonnull
    @Override
    public Collection<RepositoryElement> getChildren() {
        return ProjectUtils.getChildren(this, itemGroup.getItems());
    }

}
