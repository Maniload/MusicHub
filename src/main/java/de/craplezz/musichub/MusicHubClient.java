package de.craplezz.musichub;

import de.craplezz.musichub.components.Hub;
import de.craplezz.musichub.components.Role;
import de.craplezz.musichub.components.User;
import de.craplezz.musichub.gui.MusicHubUI;
import de.craplezz.musichub.net.Connection;
import de.craplezz.musichub.net.packet.*;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class MusicHubClient {

    private Socket socket;
    private Connection connection;

    private User localUser;
    private Hub joinedHub;

    public Connection connect() throws IOException {
        socket = new Socket("localhost", 8080);

        return connection = new Connection(socket);
    }

    public void login(String userName) throws IOException {
        connection.sendPacket(new LoginPacket(userName));
        LoginConfirmPacket confirmPacket = (LoginConfirmPacket) connection.waitForPacket();
        localUser = new User(confirmPacket.getUniqueId(), userName);
    }

    public void start(MusicHubUI ui) {
        new Thread(() -> {

            try {
                connection.startPacketListener((packet -> {

                    System.out.println(packet);
                    if (packet instanceof UpdateHubListPacket) {
                        for (UpdateHubListPacket.Change change : ((UpdateHubListPacket) packet).getChanges()) {
                            switch (change.getMode()) {
                                case ADD:
                                    Hub hub = change.getHub();
                                    if (hub.getHostUserUniqueId().equals(localUser.getUniqueId())) {
                                        localUser.setRole(Role.ADMIN);
                                        ui.joinHub(localUser, hub, true);
                                        joinedHub = hub;
                                    } else {
                                        ui.addHub(hub);
                                    }
                                    break;
                                case REMOVE:
                                    ui.removeHub(change.getHub());
                            }
                        }
                    } else if (packet instanceof UpdateUserListPacket) {
                        for (UpdateUserListPacket.Change change : ((UpdateUserListPacket) packet).getChanges()) {
                            switch (change.getMode()) {
                                case ADD:
                                    if (change.getUser().getUniqueId().equals(localUser.getUniqueId())) {
                                        joinedHub = ui.getRequestedHub();
                                        ui.joinHub(localUser, joinedHub, true);
                                    } else {
                                        ui.joinHub(change.getUser(), joinedHub, false);
                                    }
                                    break;
                                case REMOVE:
                                    if (change.getUser().getUniqueId().equals(localUser.getUniqueId())) {
                                        ui.leaveHub(localUser, joinedHub, true);
                                        joinedHub = null;
                                    } else {
                                        ui.leaveHub(change.getUser(), joinedHub, false);
                                    }
                                    break;
                                case UPDATE:
                                    ui.updateUser(change.getUser());
                            }
                        }
                    } else if (packet instanceof UpdateEntryListPacket) {
                        for (UpdateEntryListPacket.Change change : ((UpdateEntryListPacket) packet).getChanges()) {
                            switch (change.getMode()) {
                                case ADD:
                                    ui.addEntry(change.getEntry());
                                    break;
                                case REMOVE:
                                    ui.removeEntry(change.getEntry());
                                    break;
                                case NOW_PLAYING:
                                    ui.nowPlayingEntry(change.getEntry());
                                    break;
                            }
                        }
                    } else if (packet instanceof SkipPacket) {
                        ui.skipEntry();
                    }

                }));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }

    public static void main(String[] args) {
        try {
            MusicHubClient client = new MusicHubClient();
            Connection connection = client.connect();

            MusicHubUI ui = new MusicHubUI(connection);
            ui.open();

            String userName = JOptionPane.showInputDialog(ui, "Choose an user name.");

            client.login(userName);
            client.start(ui);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
