package com.worksap.timachine.kryo.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Created by liuyang on 16-3-9.
 */
public class KryoMigrationSerializerTest {

    static class Boo {
        private List<Foo> foos;

        public List<Foo> getFoos() {
            return foos;
        }

        public void setFoos(List<Foo> foos) {
            this.foos = foos;
        }

        @Override
        public String toString() {
            return "Boo{" +
                    "foos=" + foos +
                    '}';
        }
    }

    static class Foo {
        private String id;
        private String name;

//        private int age;
//
//        public int getAge() {
//            return age;
//        }
//
//        public void setAge(int age) {
//            this.age = age;
//        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Foo{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    static class OldBoo {


        @FieldSerializer.Bind(CustomizedClassCollectionSerializer.class)
        private List<FooForMigrationRead> foos;

        public List<FooForMigrationRead> getFoos() {
            return foos;
        }

        public void setFoos(List<FooForMigrationRead> foos) {
            this.foos = foos;
        }

        @Override
        public String toString() {
            return "OldBoo{" +
                    "foos=" + foos +
                    '}';
        }
    }

    static class NewBoo {
        @FieldSerializer.Bind(CustomizedClassCollectionSerializer.class)
        private List<FooForMigrationWrite> foos;

        public List<FooForMigrationWrite> getFoos() {
            return foos;
        }

        public void setFoos(List<FooForMigrationWrite> foos) {
            this.foos = foos;
        }

        @Override
        public String toString() {
            return "newBoo{" +
                    "foos=" + foos +
                    '}';
        }
    }

    static class FooForMigrationRead {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "FooForMigrationRead{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    static class FooForMigrationWrite {
        private String id;
        private String name;
        private int age;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }


        @Override
        public String toString() {
            return "FooForMigrationWrite{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
    private final Kryo kryo = new Kryo();

    @Test
    public void testKryoMigrationSerializerRead() throws IOException {
        List<Foo> list = new ArrayList<>();
        Foo foo = new Foo();
        foo.setId("1");
        foo.setName("foo");
        list.add(foo);
        Boo boo = new Boo();
        boo.setFoos(list);
        byte[] source = writeObject(boo);

        CustomizedClassCollectionSerializer.readAs(Foo.class, FooForMigrationRead.class);
        OldBoo read = readObject(source, OldBoo.class);
        assertSame(FooForMigrationRead.class, read.getFoos().get(0).getClass());
    }

    @Test
    public void testKryoMigrationSerializerWrite() throws IOException {
        List<FooForMigrationWrite> list = new ArrayList<>();
        FooForMigrationWrite foo = new FooForMigrationWrite();
        foo.setId("1");
        foo.setName("foo");
        foo.setAge(18);
        list.add(foo);
        NewBoo newBoo = new NewBoo();
        newBoo.setFoos(list);

        CustomizedClassCollectionSerializer.writeAs(FooForMigrationWrite.class, Foo.class);

        byte[] written = writeObject(newBoo);

        CustomizedClassCollectionSerializer.readAs(Foo.class, FooForMigrationWrite.class);
        NewBoo read = readObject(written, NewBoo.class);
        assertSame(FooForMigrationWrite.class, read.getFoos().get(0).getClass());
    }


    private byte[] writeObject(Object obj) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output output = new Output(stream);
        kryo.writeObject(output, obj);
        output.close();
        return stream.toByteArray();
    }

    private <T> T readObject(byte[] source, Class<T> clazz) {
        Input input = new Input(source);
        return kryo.readObject(input, clazz);
    }

    //manual test
    //Run write first, and then change Foo definition, add "int age", and then run read
    public static void main(String[] args) throws FileNotFoundException {
        write();
        //read();
    }

    private static void write() throws FileNotFoundException {
        List<FooForMigrationWrite> list = new ArrayList<>();
        FooForMigrationWrite foo = new FooForMigrationWrite();
        foo.setId("1");
        foo.setName("foo");
        foo.setAge(18);
        list.add(foo);
        NewBoo newBoo = new NewBoo();
        newBoo.setFoos(list);

        CustomizedClassCollectionSerializer.writeAs(FooForMigrationWrite.class, Foo.class);
        Kryo kryo = new Kryo();
        Output output = new Output(new FileOutputStream("out.bin"));
        kryo.writeObject(output, newBoo);
        output.close();
    }

    private static void read() throws FileNotFoundException {
        Kryo kryo = new Kryo();
        Input input = new Input(new FileInputStream("out.bin"));
        Boo boo = kryo.readObject(input, Boo.class);
        System.out.println(boo);
    }

}
