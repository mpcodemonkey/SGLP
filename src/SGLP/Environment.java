package SGLP;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Created by ubufu on 12/4/2016.
 */
public class Environment {
    private HashMap<String, String> launchEnvironment;

    public Environment(String path){
        Gson environmentAsJson = new Gson();
        launchEnvironment = null;
        try{
            launchEnvironment = environmentAsJson.fromJson(new FileReader(path + File.separator + "Environments.json"), new TypeToken<HashMap<String, String>>(){}.getType());
        }
        catch(FileNotFoundException f){
            f.printStackTrace();
        }

    }

    public String getPath(String needle){
        String path = launchEnvironment.get(needle);

        if(path == null || path.equals("")){
            System.out.println("Could not find environment referred to by " + needle + ". Setting environment to default");
            path = launchEnvironment.get("Default");
        }
        return path;
    }
}
