package monash.infotech.monashp2pdatasync.entities;

import java.io.Serializable;
import java.util.List;


/**
 *
 * @author john
 */
public class UserRole implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer userRoleID;
    private String userRoleTtile;

    public UserRole() {
    }

    public UserRole(Integer userRoleID) {
        this.userRoleID = userRoleID;
    }

    public UserRole(Integer userRoleID, String userRoleTtile) {
        this.userRoleID = userRoleID;
        this.userRoleTtile = userRoleTtile;
    }

    public Integer getUserRoleID() {
        return userRoleID;
    }

    public void setUserRoleID(Integer userRoleID) {
        this.userRoleID = userRoleID;
    }

    public String getUserRoleTtile() {
        return userRoleTtile;
    }

    public void setUserRoleTtile(String userRoleTtile) {
        this.userRoleTtile = userRoleTtile;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userRoleID != null ? userRoleID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserRole)) {
            return false;
        }
        UserRole other = (UserRole) object;
        if ((this.userRoleID == null && other.userRoleID != null) || (this.userRoleID != null && !this.userRoleID.equals(other.userRoleID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.UserRole[ userRoleID=" + userRoleID + " ]";
    }
    
}
