/*
 * BungeeTabListPlus - a BungeeCord plugin to customize the tablist
 *
 * Copyright (C) 2014 - 2015 Florian Stober
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.protocol;

import codecrafter47.bungeetablistplus.packet.TeamPacketHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.Team;

import java.util.List;

public class TeamPacketListener extends MessageToMessageDecoder<PacketWrapper> {
    private final ServerConnection connection;
    private final TeamPacketHandler handler;
    private final int protocolVersion;

    public TeamPacketListener(ServerConnection connection, TeamPacketHandler handler, int protocolVersion) {
        this.connection = connection;
        this.handler = handler;
        this.protocolVersion = protocolVersion;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, PacketWrapper packetWrapper, List<Object> out) throws Exception {
        if (!connection.isObsolete()) {
            if (packetWrapper.packet != null && packetWrapper.packet instanceof Team) {
                if (handler.onTeamPacket((Team) packetWrapper.packet)) {
                    int readerIndex = packetWrapper.buf.readerIndex();
                    DefinedPacket.readVarInt(packetWrapper.buf);
                    packetWrapper.buf.writerIndex(packetWrapper.buf.readerIndex());
                    packetWrapper.packet.write(packetWrapper.buf, ProtocolConstants.Direction.TO_CLIENT, protocolVersion);
                    packetWrapper.buf.readerIndex(readerIndex);
                }
            }
        }
        out.add(packetWrapper);
    }
}
