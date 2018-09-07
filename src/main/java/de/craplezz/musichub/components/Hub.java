package de.craplezz.musichub.components;

import de.craplezz.musichub.media.MediaServerConnection;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Hub {

    private final String displayName;
    private final UUID hostUserUniqueId;

    private final Queue<Entry> entries = new LinkedBlockingQueue<>();
    private final Map<UUID, User> users = new HashMap<>();

    private MediaServerConnection connection;

    public Hub(String displayName, UUID hostUserUniqueId) {
        this.displayName = displayName;
        this.hostUserUniqueId = hostUserUniqueId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UUID getHostUserUniqueId() {
        return hostUserUniqueId;
    }

    public void addEntry(Entry entry) {
        entries.add(entry);

        if (connection != null) {
            connection.append(entry.getUrl());
        }
    }

    public void removeEntry(Entry entry) {
        entries.remove(entry);

        if (connection != null) {
            connection.remove(entry.getUrl());
        }
    }

    public Entry nextEntry() {
        return entries.poll();
    }

    public Queue<Entry> getEntries() {
        return entries;
    }

    public void addUser(User user) {
        users.put(user.getUniqueId(), user);
    }

    public void removeUser(User user) {
        users.remove(user.getUniqueId());
    }

    public Collection<User> getUsers() {
        return users.values();
    }

    public void setMediaServerConnection(MediaServerConnection connection) {
        this.connection = connection;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Hub && ((Hub) o).hostUserUniqueId.equals(hostUserUniqueId);
    }

}
