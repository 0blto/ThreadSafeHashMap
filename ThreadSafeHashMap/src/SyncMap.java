import java.util.Iterator;

public interface SyncMap<Key, Value> {
    Value put(Key key, Value value);

    Value get(Key key);

    boolean containsKey(Key key);

    boolean containsValue(Value value);

    boolean contains(Key key, Value value);

    Iterator<Helper<Key, Value>> iterator();

    Value remove(Key key);

    int size();


}
