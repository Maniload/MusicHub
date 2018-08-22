package de.craplezz.musichub.net;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Packet {

    public abstract void write(DataOutputStream outputStream) throws IOException;

    public abstract byte getId();

}
