package de.craplezz.musichub;

import de.craplezz.musichub.components.*;
import de.craplezz.musichub.net.Connection;
import de.craplezz.musichub.net.Packet;
import de.craplezz.musichub.net.packet.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class MusicHubServer {

    private ServerSocket server;

    private final Map<Connection, User> usersByConnection = new HashMap<>();
    private final Map<UUID, User> usersByUniqueId = new HashMap<>();
    private final Map<UUID, Hub> hubs = new HashMap<>();

    public void start() {
        try {
            server = new ServerSocket(8080, 0);

            while (true) {
                Socket socket = server.accept();
                new Thread(() -> {

                    try (Connection connection = new Connection(socket)) {
                        try {
                            LoginPacket loginPacket = (LoginPacket) connection.waitForPacket();
                            UUID uniqueId = UUID.randomUUID();
                            connection.sendPacket(new LoginConfirmPacket(uniqueId));
                            User user = new User(uniqueId, loginPacket.getUserName());
                            user.bindConnection(connection);
                            usersByUniqueId.put(user.getUniqueId(), user);
                            usersByConnection.put(connection, user);

                            List<UpdateHubListPacket.Change> hubChanges = new ArrayList<>();
                            for (Hub hub : hubs.values()) {
                                hubChanges.add(new UpdateHubListPacket.Change(
                                        UpdateHubListPacket.Change.Mode.ADD,
                                        hub
                                ));
                            }
                            connection.sendPacket(new UpdateHubListPacket(hubChanges));

                            connection.startPacketListener((packet -> {

                                try {
                                    if (packet instanceof CreateHubPacket) {
                                        if (!hubs.containsKey(user.getUniqueId())) {
                                            Hub hub = new Hub(
                                                    ((CreateHubPacket) packet).getDisplayName(),
                                                    user.getUniqueId()
                                            );
                                            hub.addUser(user);
                                            user.setRole(Role.ADMIN);
                                            hubs.put(user.getUniqueId(), hub);
                                            for (Connection onlineConnection : usersByConnection.keySet()) {
                                                onlineConnection.sendPacket(new UpdateHubListPacket(new UpdateHubListPacket.Change(
                                                        UpdateHubListPacket.Change.Mode.ADD,
                                                        hub
                                                )));
                                            }
                                        }
                                    } else if (packet instanceof JoinHubPacket) {
                                        Hub hub = hubs.get(((JoinHubPacket) packet).getHostUserUniqueId());
                                        if (hub != null && !hub.getUsers().contains(user)) {
                                            hub.addUser(user);
                                            List<UpdateUserListPacket.Change> changes = new ArrayList<>();
                                            for (User hubUser : hub.getUsers()) {
                                                hubUser.getConnection().sendPacket(new UpdateUserListPacket(new UpdateUserListPacket.Change(
                                                        UpdateUserListPacket.Change.Mode.ADD,
                                                        user
                                                )));
                                                if (!user.equals(hubUser)) {
                                                    changes.add(new UpdateUserListPacket.Change(
                                                            UpdateUserListPacket.Change.Mode.ADD,
                                                            hubUser
                                                    ));
                                                }
                                            }
                                            connection.sendPacket(new UpdateUserListPacket(changes));

                                            List<UpdateEntryListPacket.Change> entryChanges = new ArrayList<>();
                                            for (Entry entry : hub.getEntries()) {
                                                entryChanges.add(new UpdateEntryListPacket.Change(
                                                        UpdateEntryListPacket.Change.Mode.ADD,
                                                        entry.getUrl()
                                                ));
                                            }
                                            Entry nowPlaying = hub.getEntries().peek();
                                            if (nowPlaying != null) {
                                                entryChanges.add(new UpdateEntryListPacket.Change(
                                                        UpdateEntryListPacket.Change.Mode.NOW_PLAYING,
                                                        nowPlaying.getUrl()
                                                ));
                                            }
                                            connection.sendPacket(new UpdateEntryListPacket(entryChanges));
                                        }
                                    } else if (packet instanceof UpdateEntryListPacket) {
                                        Hub hub = getHubByUser(user);
                                        if (hub != null) {
                                            List<UpdateEntryListPacket.Change> changes = new ArrayList<>();
                                            for (UpdateEntryListPacket.Change change : ((UpdateEntryListPacket) packet).getChanges()) {
                                                switch (change.getMode()) {
                                                    case ADD:
                                                        if (user.getRole().hasPermission(Permission.ADD_ENTRY)) {
                                                            changes.add(change);
                                                            hub.addEntry(new Entry(change.getEntry()));
                                                        }
                                                        break;
                                                    case REMOVE:
                                                        if (user.getRole().hasPermission(Permission.REMOVE_ENTRY)) {
                                                            changes.add(change);
                                                            hub.removeEntry(new Entry(change.getEntry()));
                                                        }
                                                        break;
                                                    case NOW_PLAYING:
                                                        if (user.getRole() == Role.ADMIN) {
                                                            changes.add(change);
                                                        }
                                                        break;
                                                }
                                            }
                                            Packet newPacket = new UpdateEntryListPacket(changes);
                                            for (User hubUser : hub.getUsers()) {
                                                hubUser.getConnection().sendPacket(newPacket);
                                            }

                                        }
                                    } else if (packet instanceof UpdateUserListPacket) {
                                        Hub hub = getHubByUser(user);
                                        if (hub != null) {
                                            Collection<User> hubUsers = new ArrayList<>(hub.getUsers());
                                            List<UpdateUserListPacket.Change> changes = new ArrayList<>();
                                            for (UpdateUserListPacket.Change change : ((UpdateUserListPacket) packet).getChanges()) {
                                                switch (change.getMode()) {
                                                    case REMOVE:
                                                        User kickUser = usersByUniqueId.get(change.getUser().getUniqueId());
                                                        if (user.getRole().hasPermission(Permission.KICK_USER) &&
                                                                user.getRole().ordinal() > kickUser.getRole().ordinal()) {
                                                            hub.removeUser(kickUser);
                                                            changes.add(change);
                                                        }
                                                        break;
                                                    case UPDATE:
                                                        User updateUser = usersByUniqueId.get(change.getUser().getUniqueId());
                                                        if (user.getRole().hasPermission(Permission.EDIT_ROOM)) {
                                                            updateUser.setRole(change.getUser().getRole());
                                                            changes.add(change);
                                                        }
                                                        break;
                                                }
                                            }
                                            for (User hubUser : hubUsers) {
                                                hubUser.getConnection().sendPacket(new UpdateUserListPacket(changes));
                                            }
                                        }
                                    } else if (packet instanceof SkipPacket) {
                                        Hub hub = getHubByUser(user);
                                        if (hub != null && user.getRole().hasPermission(Permission.SKIP_ENTRY)) {
                                            for (User hubUser : hub.getUsers()) {
                                                if (hubUser.getRole() == Role.ADMIN) {
                                                    hubUser.getConnection().sendPacket(packet);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }));
                        } finally {
                            User user = usersByConnection.remove(connection);
                            usersByUniqueId.remove(user.getUniqueId());

                            // Check if user is in hub
                            Hub hub = getHubByUser(user);
                            if (hub != null) {
                                hub.removeUser(user);
                                for (User hubUser : hub.getUsers()) {
                                    hubUser.getConnection().sendPacket(new UpdateUserListPacket(new UpdateUserListPacket.Change(
                                            UpdateUserListPacket.Change.Mode.REMOVE,
                                            user
                                    )));
                                }
                            }

                            // Check if user is owner of hub
                            hub = hubs.remove(user.getUniqueId());
                            if (hub != null) {
                                // Kick all usersByConnection in this hub
                                for (User hubUser : hub.getUsers()) {
                                    hubUser.getConnection().sendPacket(new UpdateUserListPacket(new UpdateUserListPacket.Change(
                                            UpdateUserListPacket.Change.Mode.REMOVE,
                                            hubUser
                                    )));
                                }
                                for (Connection onlineConnection : usersByConnection.keySet()) {
                                    onlineConnection.sendPacket(new UpdateHubListPacket(new UpdateHubListPacket.Change(
                                            UpdateHubListPacket.Change.Mode.REMOVE,
                                            hub
                                    )));
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Hub getHubByUser(User user) {
        for (Hub hub : hubs.values()) {
            if (hub.getUsers().contains(user)) {
                return hub;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        new MusicHubServer().start();
    }

}
