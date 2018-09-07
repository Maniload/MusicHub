package de.craplezz.musichub.gui;

import de.craplezz.musichub.components.Entry;
import de.craplezz.musichub.components.Hub;
import de.craplezz.musichub.components.Role;
import de.craplezz.musichub.components.User;
import de.craplezz.musichub.media.MediaPlayer;
import de.craplezz.musichub.net.Connection;
import de.craplezz.musichub.net.packet.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MusicHubUI extends JFrame {

    private JPanel contentPane;

    private JTable hubTable;
    private JTextField hubDisplayNameField;
    private JButton joinButton;
    private JButton createButton;

    private JTable hubEntryTable;
    private JTextField nowPlayingTextField;
    private JTextField urlField;
    private JButton addButton;
    private JButton removeButton;
    private JButton skipButton;

    private JTable userTable;
    private JComboBox<Role> roleComboBox;
    private JButton kickButton;

    private JTabbedPane tabbedPane;
    private JPanel hubListTab;
    private JPanel hubTab;
    private JPanel userListTab;

    private Hub requestedHub;
    private MediaPlayer mediaPlayer;
    private Map<String, Entry> entries = new HashMap<>();

    public MusicHubUI(Connection connection) throws HeadlessException {
        super("MusicHub");

        setSize(500, 500);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setContentPane(contentPane);

//        mediaPlayer = new MediaPlayer(new MediaPlayer.Listener() {
//
//            @Override
//            public void onPlayerStart(String url) {
//                try {
//                    connection.sendPacket(new UpdateEntryListPacket(new UpdateEntryListPacket.Change(
//                            UpdateEntryListPacket.Change.Mode.NOW_PLAYING,
//                            url
//                    )));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onPlayerFinish(String url) {
//                nowPlayingTextField.setText(null);
//            }
//
//        });

        // Hub list tab
        {
            hubTable.setModel(new DefaultTableModel(new Object[]{ "Host Name" }, 0) {

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

            });
            hubTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            ActionListener createHubListener = (event) -> {

                try {
                    connection.sendPacket(new CreateHubPacket(hubDisplayNameField.getText()));

                    hubDisplayNameField.setText(null);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            };
            hubDisplayNameField.addActionListener(createHubListener);
            createButton.addActionListener(createHubListener);

            joinButton.addActionListener((event) -> {


                if (hubTable.getSelectedRow() >= 0) {
                    try {
                        Hub hub = (Hub) hubTable.getValueAt(hubTable.getSelectedRow(), 0);
                        connection.sendPacket(new JoinHubPacket(hub.getHostUserUniqueId()));
                        requestedHub = hub;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            });
        }

        // Hub tab
        {
            hubEntryTable.setModel(new DefaultTableModel(new Object[]{ "Media" }, 0) {

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

            });
            hubEntryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            ActionListener addEntryListener = (event) -> {

                try {
                    connection.sendPacket(new UpdateEntryListPacket(new UpdateEntryListPacket.Change(
                            UpdateEntryListPacket.Change.Mode.ADD,
                            urlField.getText()
                    )));

                    urlField.setText(null);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            };
            urlField.addActionListener(addEntryListener);
            addButton.addActionListener(addEntryListener);

            removeButton.addActionListener((event) -> {

                if (hubEntryTable.getSelectedRow() >= 0) {
                    try {
                        connection.sendPacket(new UpdateEntryListPacket(new UpdateEntryListPacket.Change(
                                UpdateEntryListPacket.Change.Mode.REMOVE,
                                ((Entry) hubEntryTable.getValueAt(hubEntryTable.getSelectedRow(), 0)).getUrl()
                        )));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            });

            skipButton.addActionListener((event) -> {

                try {
                    connection.sendPacket(new SkipPacket());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        }

        // User list tab
        {
            userTable.setModel(new DefaultTableModel(new Object[]{"User Name", "Role"}, 0) {

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

            });
            userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            userTable.getSelectionModel().addListSelectionListener((event) -> {

                if (!event.getValueIsAdjusting() && userTable.getSelectedRow() >= 0) {
                    roleComboBox.setSelectedItem(userTable.getValueAt(userTable.getSelectedRow(), 1));
                }

            });

            roleComboBox.setModel(new DefaultComboBoxModel<>(Role.values()));
            roleComboBox.addActionListener((event) -> {

                if (userTable.getSelectedRow() >= 0) {
                    User user = (User) userTable.getValueAt(userTable.getSelectedRow(), 0);
                    user.setRole(((Role) roleComboBox.getSelectedItem()));
                    try {
                        connection.sendPacket(new UpdateUserListPacket(new UpdateUserListPacket.Change(
                                UpdateUserListPacket.Change.Mode.UPDATE,
                                user
                        )));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            });

            kickButton.addActionListener((event) -> {

                if (userTable.getSelectedRow() >= 0) {
                    User user = (User) userTable.getValueAt(userTable.getSelectedRow(), 0);
                    try {
                        connection.sendPacket(new UpdateUserListPacket(new UpdateUserListPacket.Change(
                                UpdateUserListPacket.Change.Mode.REMOVE,
                                user
                        )));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            });
        }
    }

    public void open() {
        setVisible(true);
    }

    public void addHub(Hub hub) {
        ((DefaultTableModel) hubTable.getModel()).addRow(new Object[]{
                hub
        });
    }

    public void removeHub(Hub hub) {
        for (int row = hubTable.getRowCount() - 1; row >= 0; row--) {
            if (hubTable.getValueAt(row, 0).equals(hub)) {
                ((DefaultTableModel) hubTable.getModel()).removeRow(row);
            }
        }
    }

    public void joinHub(User user, Hub hub, boolean isLocalUser) {
        if (isLocalUser) {
            tabbedPane.setEnabledAt(tabbedPane.indexOfComponent(hubTab), true);
            tabbedPane.setSelectedComponent(hubTab);

            tabbedPane.setEnabledAt(tabbedPane.indexOfComponent(userListTab), true);

            // TODO: Temp
            if (user.getRole() == Role.ADMIN) {
                mediaPlayer = new MediaPlayer(hub);
                mediaPlayer.connect();
            }
        }

        ((DefaultTableModel) userTable.getModel()).addRow(new Object[]{
                user, user.getRole()
        });
    }

    public void leaveHub(User user, Hub hub, boolean isLocalUser) {
        if (isLocalUser) {
            tabbedPane.setEnabledAt(tabbedPane.indexOfComponent(hubTab), false);
            tabbedPane.setEnabledAt(tabbedPane.indexOfComponent(userListTab), false);

            ((DefaultTableModel) hubEntryTable.getModel()).setRowCount(0);
            ((DefaultTableModel) userTable.getModel()).setRowCount(0);

            nowPlayingTextField.setText(null);
            urlField.setText(null);

            tabbedPane.setSelectedComponent(hubListTab);
        } else {
            for (int row = userTable.getRowCount() - 1; row >= 0; row--) {
                if (userTable.getValueAt(row, 0).equals(user)) {
                    ((DefaultTableModel) userTable.getModel()).removeRow(row);
                }
            }
        }
    }

    public void updateUser(User user) {
        for (int row = userTable.getRowCount() - 1; row >= 0; row--) {
            if (((User) userTable.getValueAt(row, 0)).getUniqueId().equals(user.getUniqueId())) {
                userTable.setValueAt(user.getRole(), row, 1);
            }
        }
    }

    public void addEntry(String entry) {
        Entry objEntry = new Entry(entry);
        ((DefaultTableModel) hubEntryTable.getModel()).addRow(new Object[]{
                objEntry
        });

        objEntry.fetchData(() -> hubEntryTable.repaint());

        entries.put(entry, objEntry);
    }

    public void removeEntry(String entry) {
        removeEntryFromTable(entry);
    }

    public void nowPlayingEntry(String entry) {
        removeEntryFromTable(entry);
        Entry objEntry = entries.get(entry);

        nowPlayingTextField.setText(objEntry.toString());
        objEntry.fetchData(() -> {

            if (nowPlayingTextField.getText().equals(entry)) {
                nowPlayingTextField.setText(objEntry.toString());
            }

        });
    }

    public void skipEntry() {
        if (mediaPlayer != null) {
            mediaPlayer.skip();
        }
    }

    private void removeEntryFromTable(String entry) {
        Entry objEntry = entries.get(entry);
        for (int row = hubEntryTable.getRowCount() - 1; row >= 0; row--) {
            if (hubEntryTable.getValueAt(row, 0).equals(objEntry)) {
                ((DefaultTableModel) hubEntryTable.getModel()).removeRow(row);
            }
        }
    }

    public Hub getRequestedHub() {
        return requestedHub;
    }

}
