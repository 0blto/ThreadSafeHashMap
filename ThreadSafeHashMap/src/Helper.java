

public class Helper<Key, Value> {
    int hashedKey;
    Key key;
    Value value;
    Helper<Key, Value> next;

    @Override
    public String toString() {
        return key + "=" + value;
    }


    public Helper(int hashedKey, Key key, Value value, Helper<Key, Value> helper) {
        this.hashedKey = hashedKey;
        this.key = key;
        this.value = value;
        this.next = helper;
    }

}
