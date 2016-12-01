package SGLP;

/**
 * Created by ubufu on 11/1/2016.
 */
public class GameInfo {
    private String name;
    private String folder;
    private String description;
    private String controls;
    private IPInformation IPInfo;
    private String ImageLocation;
    private String command;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getControls() {
        return controls;
    }

    public void setControls(String controls) {
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

    public String getCommand() { return command; }

    public void setCommand(String command) { this.command = command; }

}
