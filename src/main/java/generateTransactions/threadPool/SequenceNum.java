package generateTransactions.threadPool;

/**
 * Created by yuan.wei on 5/17/17.
 */

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public final class SequenceNum {
    private static final Unsafe unsafe;
    @SuppressWarnings("unused")
    private int p1, p2, p3, p4, p5, p6, p7;// cache fill
    private volatile int value;
    @SuppressWarnings("unused")
    private long p8, p9, p10, p11, p12, p13, p14, p15;// cache fill
    private static final long VALUE_OFFSET;

    static {
        unsafe = Util.get_unsafe();
        try {
            VALUE_OFFSET = unsafe.objectFieldOffset(SequenceNum.class.getDeclaredField("value"));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SequenceNum() {
        value = 0;
    }

    public final boolean compareAndSet(final int expectedValue, final int newValue) {
        return unsafe.compareAndSwapInt(this, VALUE_OFFSET, expectedValue, newValue);
    }

    public final int get() {
        return value;
    }

    public final int increase() {
        int now = 0, newNum = 0;
        while (true) {
            now = get();
            newNum = now + 1;
            if (compareAndSet(now, newNum)) {
                return newNum;
            }
        }
    }
}

