package de.craplezz.musichub.net.packet;

import de.craplezz.musichub.components.Hub;
import de.craplezz.musichub.components.Role;
import de.craplezz.musichub.components.User;
import de.craplezz.musichub.net.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class UpdateHubListPacket extends Packet {

    private List<Change> changes = new ArrayList<>();

    public UpdateHubListPacket(List<Change> changes) {
        this.changes = changes;
    }

    public UpdateHubListPacket(Change... changes) {
        this.changes = Arrays.asList(changes);
    }

    public UpdateHubListPacket(DataInputStream inputStream) throws IOException {
        int nChanges = inputStream.readShort();
        for (int i = 0; i < nChanges; i++) {
            changes.add(new Change(
                    Change.Mode.values()[inputStream.readByte()],
                    new Hub(
                            inputStream.readUTF(),
                            new UUID(
                                    inputStream.readLong(),
                                    inputStream.readLong()
                            )
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
            outputStream.writeUTF(change.getHub().getDisplayName());
            outputStream.writeLong(change.getHub().getHostUserUniqueId().getLeastSignificantBits());
            outputStream.writeLong(change.getHub().getHostUserUniqueId().getMostSignificantBits());
        }
    }

    @Override
    public byte getId() {
        return 3;
    }

    public static class Change {

        private final Mode mode;
        private final Hub hub;

        public Change(Mode mode, Hub hub) {
            this.mode = mode;
            this.hub = hub;
        }

        public Mode getMode() {
            return mode;
        }

        public Hub getHub() {
            return hub;
        }

        public enum Mode {
            ADD, REMOVE
        }

    }

}
