package SGLP;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ubufu on 11/1/2016.
 */
public class GameInfo {
    private String name;
    private String semester;
    private String folder;
    private String description;
    private HashMap<String, HashMap<String, String>> controls;
    private IPInformation IPInfo;
    private String ImageLocation;
    private String clientExecutable;

    private String serverExecutable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<String, HashMap<String, String>> getControls() {
        return controls;
    }

    public void setControls(HashMap<String, HashMap<String, String>> controls) {
        this.controls = controls;
    }

    public IPInformation getIPInfo() {
        return IPInfo;
    }

    public void setIPInfo(IPInformation IPInfo) {
        this.IPInfo = IPInfo;
    }

    public String getImageLocation() {
        return ImageLocation;
    }

    public void setImageLocation(String imageLocation) {
        ImageLocation = imageLocation;
    }

    public String getClientExecutable() {
        return clientExecutable;
    }

    public void setClientExecutable(String clientExecutable) {
        this.clientExecutable = clientExecutable;
    }

    public String getServerExecutable() {
        return serverExecutable;
    }

    public void setServerExecutable(String serverExecutable) {
        this.serverExecutable = serverExecutable;
    }
}
