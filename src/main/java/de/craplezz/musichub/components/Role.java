package de.craplezz.musichub.components;

import java.util.Arrays;
import java.util.List;

public enum Role {
    SPECTATOR(null),
    MEMBER(SPECTATOR, Permission.ADD_ENTRY),
    MODERATOR(MEMBER, Permission.KICK_USER, Permission.REMOVE_ENTRY, Permission.SKIP_ENTRY),
    ADMIN(MODERATOR, Permission.EDIT_ROOM);

    private final Role parent;
    private final List<Permission> permissions;

    Role(Role parent, Permission... permissions) {
        this.parent = parent;
        this.permissions = Arrays.asList(permissions);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission) || (parent != null && parent.hasPermission(permission));
    }

}
