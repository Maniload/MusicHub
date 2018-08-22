package de.craplezz.musichub.net.packet;

import de.craplezz.musichub.net.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LoginPacket extends Packet {

    private String userName;

    public LoginPacket(String userName) {
        this.userName = userName;
    }

    public LoginPacket(DataInputStream inputStream) throws IOException {
        userName = inputStream.readUTF();
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public void write(DataOutputStream outputStream) throws IOException {
        outputStream.writeUTF(userName);
    }

    @Override
    public byte getId() {
        return 5;
    }

}
