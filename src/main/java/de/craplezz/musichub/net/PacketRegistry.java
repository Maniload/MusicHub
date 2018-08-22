package de.craplezz.musichub.net;

import de.craplezz.musichub.net.packet.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PacketRegistry {

    private static final PacketRegistry INSTANCE = new PacketRegistry();

    private final Map<Integer, InstanceCreator> instanceCreators = new HashMap<>();

    public PacketRegistry() {
        register(0, CreateHubPacket::new);
        register(1, JoinHubPacket::new);
        register(2, UpdateUserListPacket::new);
        register(3, UpdateHubListPacket::new);
        register(4, UpdateEntryListPacket::new);
        register(5, LoginPacket::new);
        register(6, LoginConfirmPacket::new);
        register(7, SkipPacket::new);
    }

    public Packet create(int id, DataInputStream inputStream) throws IOException {
        return instanceCreators.containsKey(id) ? instanceCreators.get(id).createInstance(inputStream) : null;
    }

    private void register(int id, InstanceCreator creator) {
        instanceCreators.put(id, creator);
    }

    public static PacketRegistry getInstance() {
        return INSTANCE;
    }

    private interface InstanceCreator {

        Packet createInstance(DataInputStream inputStream) throws IOException;

    }

}
