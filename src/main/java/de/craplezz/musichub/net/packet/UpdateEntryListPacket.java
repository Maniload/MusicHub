package de.craplezz.musichub.net.packet;

import de.craplezz.musichub.net.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateEntryListPacket extends Packet {

    private List<Change> changes = new ArrayList<>();

    public UpdateEntryListPacket(List<Change> changes) {
        this.changes = changes;
    }

    public UpdateEntryListPacket(Change... changes) {
        this.changes = Arrays.asList(changes);
    }

    public UpdateEntryListPacket(DataInputStream inputStream) throws IOException {
        int nChanges = inputStream.readShort();
        for (int i = 0; i < nChanges; i++) {
            changes.add(new Change(
                    Change.Mode.values()[inputStream.readByte()],
                    inputStream.readUTF()
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
            outputStream.writeUTF(change.getEntry());
        }
    }

    @Override
    public byte getId() {
        return 4;
    }

    public static class Change {

        private final Mode mode;
        private final String entry;

        public Change(Mode mode, String entry) {
            this.mode = mode;
            this.entry = entry;
        }

        public Mode getMode() {
            return mode;
        }

        public String getEntry() {
            return entry;
        }

        public enum Mode {
            ADD, REMOVE, NOW_PLAYING
        }

    }

}
