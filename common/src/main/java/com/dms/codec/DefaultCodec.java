package com.dms.codec;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import org.nustaq.serialization.FSTConfiguration;

import java.io.Serializable;


public class DefaultCodec<S extends Serializable, R extends Serializable> implements MessageCodec<S, R> {

    private final ThreadLocal<FSTConfiguration> conf = new ThreadLocal<FSTConfiguration>() {
        public FSTConfiguration initialValue() {
            return FSTConfiguration.createDefaultConfiguration();
        }
    };
    private String name;

    public DefaultCodec(String name) {
        this.name = name;
    }

    @Override
    public void encodeToWire(Buffer buffer, S o) {
        byte[] encoded = conf.get().asByteArray(o);
        buffer.appendInt(encoded.length);
        Buffer buff = Buffer.buffer(encoded);
        buffer.appendBuffer(buff);
    }

    @Override
    @SuppressWarnings("unchecked")
    public R decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        pos += 4;
        byte[] encoded = buffer.getBytes(pos, pos + length);
        return (R) conf.get().asObject(encoded);
    }

    @Override
    @SuppressWarnings("unchecked")
    public R transform(S o) {
        return (R) conf.get().asObject(conf.get().asByteArray(o));
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }

}
