package br.com.ourtalk.core;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Versioned application framing. Encryption/authentication are provided by Nearby, not this codec. */
public final class ChatFrame {
    public static final int MAX_BYTES=8192;
    public enum Kind { TEXT, ACK }
    public final Kind kind;
    public final UUID id;
    public final String text;
    private ChatFrame(Kind kind, UUID id, String text) { this.kind=kind; this.id=id; this.text=text; }
    public static ChatFrame text(UUID id,String text) {
        if (id==null || text==null || text.trim().isEmpty() || text.length()>2000 || text.chars().anyMatch(c->c<32 && c!='\n' && c!='\t')) throw new IllegalArgumentException("Invalid text");
        return new ChatFrame(Kind.TEXT,id,text);
    }
    public static ChatFrame ack(UUID id) { if(id==null)throw new IllegalArgumentException("Missing id"); return new ChatFrame(Kind.ACK,id,""); }
    public byte[] encode() {
        byte[] body=text.getBytes(StandardCharsets.UTF_8);
        if(body.length+22>MAX_BYTES)throw new IllegalArgumentException("Frame too large");
        return ByteBuffer.allocate(22+body.length).put((byte)1).put((byte)kind.ordinal()).putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits()).putInt(body.length).put(body).array();
    }
    public static ChatFrame decode(byte[] data) {
        if(data==null || data.length<22 || data.length>MAX_BYTES)throw new IllegalArgumentException("Invalid frame length");
        ByteBuffer in=ByteBuffer.wrap(data);
        int version=in.get() & 255, type=in.get() & 255;
        UUID id=new UUID(in.getLong(),in.getLong()); int size=in.getInt();
        if(version!=1 || type>1 || size!=in.remaining() || (type==1 && size!=0))throw new IllegalArgumentException("Invalid frame header");
        try {
            String body=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(in).toString();
            return type==0 ? text(id,body) : ack(id);
        } catch(CharacterCodingException error) { throw new IllegalArgumentException("Invalid UTF-8",error); }
    }
}
