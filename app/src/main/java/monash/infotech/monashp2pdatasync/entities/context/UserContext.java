package monash.infotech.monashp2pdatasync.entities.context;

import java.util.Map;

/**
 * Created by john on 1/26/2016.
 */
public class UserContext {
    private int Id;
    private UserRole Role;
    private Map<String,Double> Expertise;
    private DeviceProfile deviceProfile;
    private UserSituation situation;

    public UserContext(int id, UserRole role, Map<String, Double> expertise, DeviceProfile deviceProfile, UserSituation situation) {
        Id = id;
        Role = role;
        Expertise = expertise;
        this.deviceProfile = deviceProfile;
        this.situation = situation;
    }
    public UserContext(int id, UserRole role) {
        Id = id;
        Role = role;
    }
    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public UserRole getRole() {
        return Role;
    }

    public void setRole(UserRole role) {
        Role = role;
    }

    public Map<String, Double> getExpertise() {
        return Expertise;
    }

    public void setExpertise(Map<String, Double> expertise) {
        Expertise = expertise;
    }

    public DeviceProfile getDeviceProfile() {
        return deviceProfile;
    }

    public void setDeviceProfile(DeviceProfile deviceProfile) {
        this.deviceProfile = deviceProfile;
    }

    public UserSituation getSituation() {
        return situation;
    }

    public void setSituation(UserSituation situation) {
        this.situation = situation;
    }
}
