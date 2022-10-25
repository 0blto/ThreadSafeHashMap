import java.io.Serializable;
import java.util.Iterator;


public class SyncHashMap<Key, Value> implements Serializable, SyncMap<Key, Value> {
    private transient Helper<Key, Value>[] buckets;
    private final transient int default_capacity = 16;
    private transient int capacity;
    private transient int size;
    private transient int threshold;
    private transient static final float default_loadFactor = 0.75f;
    private final transient float loadFactor;

    private final transient int maximum_capacity = (int) Math.pow(2,30);

    @Override
    public synchronized int size() {
        return size;
    }

    public SyncHashMap() {
        this.threshold = (int) (default_capacity * default_loadFactor);
        this.capacity = default_capacity;
        this.loadFactor = default_loadFactor;
        buckets = new Helper[capacity];
    }

    public SyncHashMap(int capacity) {
        this(capacity,
                default_loadFactor);
    }

    public SyncHashMap(int capacity, float loadFactor) {
        if (capacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                    capacity);
        if (capacity > maximum_capacity)
            capacity = maximum_capacity;
        if (capacity == 0)
            capacity = default_capacity;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                    loadFactor);
        this.capacity = capacity;
        this.loadFactor = loadFactor;
        this.threshold = (int) (capacity * loadFactor);
        buckets = new Helper[capacity];
    }

    @Override
    public synchronized Value put(Key key, Value value) {
        int hash = hash(key);
        Helper<Key,Value>[] buck = buckets; int len = buck.length, index = (len - 1) & hash;
        Helper<Key,Value> helper = buck[index];
        if (helper == null) {
            buck[index] = new Helper<>(hash, key, value, null);
        } else {
            Key keyHelper;
            Helper<Key, Value> old;
            if (helper.hashedKey == hash &&
                    ((keyHelper = helper.key) == key || (key != null && key.equals(keyHelper)))) {
                old = helper;
                buck[index] = new Helper<>(hash, key, value, helper.next);
            } else {

                while (true) {
                    Helper<Key, Value> next = helper.next;
                    if (next == null) {
                        old = null;
                        helper.next = new Helper<>(hash, key, value, null);
                        break;
                    }

                    if (next.hashedKey == hash &&
                            ((keyHelper = next.key) == key || (key != null && key.equals(keyHelper)))) {
                        old = next;
                        helper.next = new Helper<>(hash, key, value, next.next);
                        break;
                    }


                    helper = helper.next;
                }
            }
            if (old != null) {
                Value oldValue = old.value;
                if (oldValue == null)
                    old.value = value;
                return oldValue;
            }
        }
        if (++size > threshold)
            buckets = resize();
        return null;
    }


    @Override
    public synchronized Value get(Key key) {
        Helper<Key,Value> helper = getHelper(hash(key), key);
        return helper == null ? null : helper.value;
    }


    private Helper<Key,Value> getHelper(int hash, Object key) {
        Helper<Key, Value>[] buck = buckets;
        int len = buck.length;
        int index = (len - 1) & hash;
        Helper<Key, Value> starter = buck[index];
        Helper<Key, Value> helper = starter;
        Key keyHelper;
        if (starter != null) {
            if (starter.hashedKey == hash &&
                    ((keyHelper = starter.key) == key || (key != null && key.equals(keyHelper))))
                return starter;
            while ((helper = helper.next) != null) {
                if (helper.hashedKey == hash &&
                        ((keyHelper = helper.key) == key || (key != null && key.equals(keyHelper))))
                    return starter;
            }
        }
        return null;
    }

    @Override
    public synchronized boolean containsKey(Key key) {
        return getHelper(hash(key), key) != null;
    }

    @Override
    public synchronized boolean containsValue(Value value) {
        Helper<Key, Value>[] buck = buckets;
        for (int i = 0; i < buck.length; i++) {
            Helper<Key, Value> helper;
            if ((helper = buck[i]) != null && (helper.value == value || helper.value.equals(value)))
                return true;

            while (helper != null) {
                if (helper.value == value || helper.value.equals(value))
                    return true;
                helper = helper.next;
            }
        }

        return false;
    }

    @Override
    public synchronized boolean contains(Key key, Value value) {
        Helper<Key, Value> helper = getHelper(hash(key), key);
        if (helper == null)
            return false;
        return helper.value == value || helper.value.equals(value);
    }


    @Override
    public synchronized Iterator<Helper<Key, Value>> iterator() {
        return new HelperIterator();
    }

    private class HelperIterator implements Iterator<Helper<Key, Value>> {
        int curNum;
        Helper<Key, Value> cur;
        Helper<Key, Value> next;

        private HelperIterator() {
            Helper<Key, Value>[] iterBuck = buckets;
            curNum = 0;
            while (next == null && curNum < buckets.length)
                next = iterBuck[curNum++];
        }

        @Override
        public synchronized boolean hasNext() {
            return next != null;
        }

        @Override
        public synchronized Helper<Key, Value> next() {
            Helper<Key, Value>[] iterBuck = buckets;
            Helper<Key, Value> helper = next;
            cur = helper;
            next = cur.next;
            while (next == null && iterBuck.length > curNum)
                next = iterBuck[curNum++];
            return helper;
        }

        @Override
        public synchronized void remove() {
            Helper<Key, Value> helper = cur;
            removeHelper(hash(helper.key), helper.key);
        }
    }



    @Override
    public synchronized Value remove(Key key) {
        Helper<Key, Value> removal = removeHelper(hash(key), key);
        return removal == null? null: removal.value;
    }


    private static synchronized int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    private Helper<Key, Value> removeHelper(int hash, Key key) {
        Helper<Key,Value>[] buck = buckets;
        int len = buck.length, index = (len - 1) & hash;
        Helper<Key, Value> helper = buck[index];
        if (helper != null) {
            Helper<Key, Value> old;
            Key keyHelper;
            if (helper.hashedKey == hash &&
                    ((keyHelper = helper.key) == key || (key != null && key.equals(keyHelper)))) {
                old = helper;
                buck[index] = helper.next;
            } else {
                while (true) {
                    Helper<Key, Value> next = helper.next;
                    if (next == null) {
                        old = null;
                        break;
                    }
                    if (next.hashedKey == hash &&
                            ((keyHelper = next.key) == key || (key != null && key.equals(keyHelper)))) {
                        old = next;
                        helper.next = next.next;
                        break;
                    }


                    helper = helper.next;

                }
            }
            if (old != null)
                --size;
                return old;
        }
        return null;
    }


    private synchronized Helper<Key, Value>[] resize() {
        Helper<Key, Value>[] oldBuckets = buckets;
        int oldCapacity = (oldBuckets == null)? 0: oldBuckets.length;
        int oldThreshold = threshold;
        int newCapacity = 0, newThreshold = 0;
        if (2 * oldCapacity < maximum_capacity) {
            newCapacity = 2 * oldCapacity;
            newThreshold = oldThreshold * 2;
        } else {
            newCapacity = maximum_capacity;
            newThreshold = Integer.MAX_VALUE;
        }
        threshold = newThreshold;
        capacity = newCapacity;
        Helper<Key, Value>[] newBuckets = new Helper[newCapacity];
        Helper<Key, Value> helper, newHelper;
        Key keyHelper;
        for (int i = 0; i < oldCapacity; i++) {
            helper = oldBuckets[i];
            if (helper != null) {
                do {
                    if (newBuckets[helper.hashedKey & (newCapacity - 1)] == null)
                        newBuckets[helper.hashedKey & (newCapacity - 1)] = new Helper<>(helper.hashedKey, helper.key, helper.value, null);
                    else {
                        newHelper = newBuckets[helper.hashedKey & (newCapacity - 1)];
                        while (true) {
                            Helper<Key, Value> next = newHelper.next;
                            if (next == null) {
                                newHelper.next = new Helper<>(helper.hashedKey, helper.key, helper.value, null);
                                break;
                            }

                            if (next.hashedKey == helper.hashedKey &&
                                    ((keyHelper = next.key) == helper.key || (helper.key != null && helper.key.equals(keyHelper)))) {
                                newHelper.next = new Helper<>(helper.hashedKey, helper.key, helper.value, next.next);
                                break;
                            }


                            newHelper = newHelper.next;
                        }
                    }

                } while ((helper = helper.next) != null);;
            }

        }
        return newBuckets;
    }

    @Override
    public synchronized String toString() {
        Iterator<Helper<Key, Value>> iterator = iterator();
        StringBuilder output = new StringBuilder();
        while (iterator.hasNext()) {
            output.append(iterator.next());
            if (iterator.hasNext())
                output.append("\n");
        }
        return String.valueOf(output);
    }

}
