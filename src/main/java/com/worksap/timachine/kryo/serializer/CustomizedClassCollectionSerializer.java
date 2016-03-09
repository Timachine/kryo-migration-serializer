package com.worksap.timachine.kryo.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CustomizedClassCollectionSerializer extends CollectionSerializer {

    private static Map<Class, Class> readAs = new HashMap<>();
    private static Map<Class, Class> writeAs = new HashMap<>();



    public static void readAs(Class origin, Class as) {
        readAs.put(origin, as);
    }

    public static void writeAs (Class origin, Class as) {
        writeAs.put(origin, as);
    }

    @Override
    public Collection read (Kryo kryo, Input input, Class<Collection> type) {
        Collection collection = create(kryo, input, type);
        kryo.reference(collection);
        int length = input.readVarInt(true);
        if (collection instanceof ArrayList) ((ArrayList)collection).ensureCapacity(length);
        for (int i = 0; i < length; i++) {
            collection.add(readObject(kryo, input));
        }
        return collection;
    }

    @Override
    public void write(Kryo kryo, Output output, Collection collection) {
        int length = collection.size();
        output.writeVarInt(length, true);
        for (Object element : collection) {
            writeObject(kryo, output, element);
        }
    }

    private void writeObject(Kryo kryo, Output output, Object object){
        if (output == null) throw new IllegalArgumentException("output cannot be null.");

        Class type = object.getClass();
        Class asType = writeAs.get(type);
        if(asType != null) {
            type = asType;
        }
        if (object == null) {
            kryo.writeClass(output, null);
            return;
        }
        kryo.writeClass(output, type);
        kryo.writeObject(output, object);
    }

    private Object readObject(Kryo kryo, Input input) {
        Registration registration = kryo.readClass(input);
        if (registration == null) return null;
        Class type = registration.getType();
        Class asType = readAs.get(type);
        if ( asType != null) {
            type = asType;
        }
        return kryo.readObject(input, type);
    }
}
