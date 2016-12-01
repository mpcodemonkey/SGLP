package SGLP;

import java.util.HashMap;

/**
 * Created by ubufu on 11/3/2016.
 */
public class MutableProcessList {
    private HashMap<String, String> mutableProcesses;

    public MutableProcessList(){
        mutableProcesses = new HashMap<>();
    }

    public synchronized String getEntry(String needle){
        return mutableProcesses.get(needle);
    }

    public synchronized void addEntry(String key, String value){
        mutableProcesses.put(key, value);
    }

    public synchronized void removeEntry(String needle){
        mutableProcesses.remove(needle);
    }
}
