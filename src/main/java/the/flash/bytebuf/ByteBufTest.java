package the.flash.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 *
 *  ByteBuf 字节容器
 *      1。丢失字节  无效数据  readerIndex（读指针）
 *      2。可读字节  主体数据（读取的数据都是这一部分）writerIndex(写指针)
 *      3。可写字节  byteBuf的数据都会写到这一段
 *      4。capacity byteBuf的底层内存的总容量
 *      5。 byteBuf中读取一个字节，readerIndex 自增1 可读字节的数量 writerIndex - readerIndex 个字节 ,  readerIndex 与 writerIndex相等时，不可读
 *      6。 写数据是从 writerInex 开始写，每写一个字节，自增1，直到等于 capacity 这个时候，byteBuf 不可写
 *      7。 macCapacity ，向byteBuf中写数据时候，容量不足会进行扩容，直到 capacity到macCapacity，超过就会报错
 *
 *   容量api:
 *      capacity（） --> byteBuf占用的字节内存
 *      macCapacity（） --> byteBuf 底层最大能占用的字节内存
 *      readableBytes（）--> 表示当前的可读字节数
 *      idReadable() -->  方法返回 false
 *      writableBytes() --> ByteBuf 当前可写的字节数
 *      isWritable() --> 返回 false , capacity-writerIndex，如果两者相等
 *      maxWritableBytes() -->  maxWritableBytes() 就表示可写的最大字节数
 *
 *   读写指针api :
 *      readerIndex（）--> 返回当前的读指针
 *      readerIndex(int)  --> 设置读指针
 *      writeIndex() --> 返回当前的写指针
 *      writeIndex(int) --> 设置写指针
 *
 *      markReaderIndex() -->  保存当前的指针
 *      resetReaderIndex() --> 把当前的指针恢复到之前保存的值
 *      markWriterIndex() 与 resetWriterIndex()  同上
 *
 *   读写api :
 *      1。writeBytes( byte[] src )  --> 把字节数组src中的数据全部写到 byteBuf
 *      2。buffer.readeBytes(byte[] src) --> 把 ByteBuf 里面的数据全部读取到 dst
 *
 *      3。writeByte(byte b)  --> 往bytebuf中写一个字节
 *      4。buffer.readByte()  ByteBuf 中读取一个字节
 *      5。release() 与 retain() Netty 使用了堆外内存，而堆外内存是不被 jvm 直接管理的，也就是说申请到的内存无法被垃圾回收器直接回收，所以需要我们手动回收
 *                            当创建完一个 ByteBuf，它的引用为1，然后每次调用 retain() 方法， 它的引用就加一， release() 方法原理是将引用计数减一，减完之后如果发现引用计数为0，则直接回收 ByteBuf 底层的内存
 *
 *      6。slice() 与   duplicate()  与 copy()
 *          slice() 方法从原始 ByteBuf 中截取一段，这段数据是从 readerIndex 到 writeIndex，同时，返回的新的 ByteBuf 的最大容量 maxCapacity 为原始 ByteBuf 的 readableBytes()
 *          duplicate() 方法把整个 ByteBuf 都截取出来，包括所有的数据，指针信息
 *          slice() 方法与 duplicate() 方法的相同点是：底层内存以及引用计数与原始的 ByteBuf 共享，也就是说经过 slice() 或者 duplicate() 返回的 ByteBuf 调用 write 系列方法都会影响到 原始的 ByteBuf，但是它们都维持着与原始 ByteBuf 相同的内存引用计数和不同的读写指针
 *          slice() 方法与 duplicate() 不同点就是：slice() 只截取从 readerIndex 到 writerIndex 之间的数据，它返回的 ByteBuf 的最大容量被限制到 原始 ByteBuf 的 readableBytes(), 而 duplicate() 是把整个 ByteBuf 都与原始的 ByteBuf 共享
 *          slice() 方法与 duplicate() 方法不会拷贝数据，它们只是通过改变读写指针来改变读写的行为，而最后一个方法 copy() 会直接从原始的 ByteBuf 中拷贝所有的信息，包括读写指针以及底层对应的数据，因此，往 copy() 返回的 ByteBuf 中写数据不会影响到原始的 ByteBuf
 *          slice() 和 duplicate() 不会改变 ByteBuf 的引用计数，所以原始的 ByteBuf 调用 release() 之后发现引用计数为零，就开始释放内存，调用这两个方法返回的 ByteBuf 也会被释放，这个时候如果再对它们进行读写，就会报错。因此，我们可以通过调用一次 retain() 方法 来增加引用，表示它们对应的底层的内存多了一次引用，引用计数为2，在释放内存的时候，需要调用两次 release() 方法，将引用计数降到零，才会释放内存...
 *
 *      7。 retainedSlice()  === slice().retain()
 *          retainedDuplicate()  ==  duplicate().retain()
 *
 *
 */
public class ByteBufTest {
    public static void main(String[] args) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(9, 100);

        print("allocate ByteBuf(9, 100)", buffer);

        // write 方法改变写指针，写完之后写指针未到 capacity 的时候，buffer 仍然可写
        buffer.writeBytes(new byte[]{1, 2, 3, 4});
        print("writeBytes(1,2,3,4)", buffer);

        // write 方法改变写指针，写完之后写指针未到 capacity 的时候，buffer 仍然可写, 写完 int 类型之后，写指针增加4
        buffer.writeInt(12);
        print("writeInt(12)", buffer);

        // write 方法改变写指针, 写完之后写指针等于 capacity 的时候，buffer 不可写
        buffer.writeBytes(new byte[]{5});
        print("writeBytes(5)", buffer);

        // write 方法改变写指针，写的时候发现 buffer 不可写则开始扩容，扩容之后 capacity 随即改变
        buffer.writeBytes(new byte[]{6});
        print("writeBytes(6)", buffer);

        // get 方法不改变读写指针
        System.out.println("getByte(3) return: " + buffer.getByte(3));
        System.out.println("getShort(3) return: " + buffer.getShort(3));
        System.out.println("getInt(3) return: " + buffer.getInt(3));
        print("getByte()", buffer);


        // set 方法不改变读写指针
        buffer.setByte(buffer.readableBytes() + 1, 0);
        print("setByte()", buffer);

        // read 方法改变读指针
        byte[] dst = new byte[buffer.readableBytes()];
        buffer.readBytes(dst);
        print("readBytes(" + dst.length + ")", buffer);

    }

    private static void print(String action, ByteBuf buffer) {
        System.out.println("after ===========" + action + "============");
        System.out.println("capacity(): " + buffer.capacity());
        System.out.println("maxCapacity(): " + buffer.maxCapacity());
        System.out.println("readerIndex(): " + buffer.readerIndex());
        System.out.println("readableBytes(): " + buffer.readableBytes());
        System.out.println("isReadable(): " + buffer.isReadable());
        System.out.println("writerIndex(): " + buffer.writerIndex());
        System.out.println("writableBytes(): " + buffer.writableBytes());
        System.out.println("isWritable(): " + buffer.isWritable());
        System.out.println("maxWritableBytes(): " + buffer.maxWritableBytes());
        System.out.println();
    }
}
