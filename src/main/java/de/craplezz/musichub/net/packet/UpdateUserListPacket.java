package de.craplezz.musichub.net.packet;

import de.craplezz.musichub.components.Role;
import de.craplezz.musichub.components.User;
import de.craplezz.musichub.net.Packet;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class UpdateUserListPacket extends Packet {

    private List<Change> changes = new ArrayList<>();

    public UpdateUserListPacket(List<Change> changes) {
        this.changes = changes;
    }

    public UpdateUserListPacket(Change... changes) {
        this.changes = Arrays.asList(changes);
    }

    public UpdateUserListPacket(DataInputStream inputStream) throws IOException {
        int nChanges = inputStream.readShort();
        for (int i = 0; i < nChanges; i++) {
            changes.add(new Change(
                    Change.Mode.values()[inputStream.readByte()],
                    new User(
                            new UUID(
                                    inputStream.readLong(),
                                    inputStream.readLong()
                            ),
                            inputStream.readUTF(),
                            Role.values()[inputStream.readByte()]
                    )
            ));
        }
    }

    public List<Change> getChanges() {
        return changes;
    }

    @Override
    public void write(DataOutputStream outputStream) throws IOException {
        outputStream.writeShort(changes.size());
        for (Change change : changes) {
            outputStream.writeByte(change.getMode().ordinal());
            outputStream.writeLong(change.getUser().getUniqueId().getLeastSignificantBits());
            outputStream.writeLong(change.getUser().getUniqueId().getMostSignificantBits());
            outputStream.writeUTF(change.getUser().getUserName());
            outputStream.writeByte(change.getUser().getRole().ordinal());
        }
    }

    @Override
    public byte getId() {
        return 2;
    }

    public static class Change {

        private final Mode mode;
        private final User user;

        public Change(Mode mode, User user) {
            this.mode = mode;
            this.user = user;
        }

        public Mode getMode() {
            return mode;
        }

        public User getUser() {
            return user;
        }

        public enum Mode {
            ADD, REMOVE, UPDATE
        }

    }

}
