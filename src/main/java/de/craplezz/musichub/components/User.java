package de.craplezz.musichub.components;

import de.craplezz.musichub.net.Connection;

import java.util.UUID;

public class User {

    private final UUID uniqueId;
    private final String userName;

    private Role role;

    private Connection connection;

    public User(UUID uniqueId, String userName, Role role) {
        this.uniqueId = uniqueId;
        this.userName = userName;
        this.role = role;
    }

    public User(UUID uniqueId, String userName) {
        this(uniqueId, userName, Role.SPECTATOR);
    }


    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getUserName() {
        return userName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void bindConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public String toString() {
        return getUserName();
    }

    @Override
    public boolean equals(Object o) {
        return
                o instanceof User &&
                ((User) o).uniqueId.equals(uniqueId) &&
                ((User) o).userName.equals(userName) &&
                ((User) o).role == role;
    }

}
