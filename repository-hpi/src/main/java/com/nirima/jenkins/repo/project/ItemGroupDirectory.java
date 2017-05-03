package com.nirima.jenkins.repo.project;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.nirima.jenkins.repo.AbstractRepositoryDirectory;
import com.nirima.jenkins.repo.RepositoryDirectory;
import com.nirima.jenkins.repo.RepositoryElement;
import hudson.model.BuildableItem;
import hudson.model.ItemGroup;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;

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
        return itemGroup.getDisplayName();
    }

    @Nonnull
    public Collection<RepositoryElement> getChildren() {
        Collection<?> items = filterItemsOfType(itemGroup.getItems(), BuildableItem.class);
        Collection<?> itemGroups = filterItemsOfType(itemGroup.getItems(), ItemGroup.class);

        return ProjectUtils.getChildren(this, (Sets.union(Sets.newHashSet(items), Sets.newHashSet(itemGroups))));
    }

    private static <T> Collection<T> filterItemsOfType(Collection<?> items, Class<T> theClass) {
        return transform(filter(items, isInstanceOf(theClass)), new Function<Object, T>() {
            @Override
            public T apply(@Nullable Object o) {
                return (T) o;
            }
        });
    }

    private static <T> Predicate<Object> isInstanceOf(final Class<T> buildableItemClass) {
        return new Predicate<Object>() {
            @Override
            public boolean apply(@Nullable Object t) {
                return buildableItemClass.isInstance(t);
            }
        };
    }

}
