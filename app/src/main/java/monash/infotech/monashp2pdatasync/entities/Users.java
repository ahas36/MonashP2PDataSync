
package monash.infotech.monashp2pdatasync.entities;

import java.io.Serializable;
/**
 *
 * @author john
 */

public class Users implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer userID;
    private String userName;
    private String userPassword;
    private UserRole userRoleID;

    public Users() {
    }

    public Users(Integer userID) {
        this.userID = userID;
    }

    public Users(Integer userID, String userName, String userPassword) {
        this.userID = userID;
        this.userName = userName;
        this.userPassword = userPassword;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public UserRole getUserRoleID() {
        return userRoleID;
    }

    public void setUserRoleID(UserRole userRoleID) {
        this.userRoleID = userRoleID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userID != null ? userID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Users)) {
            return false;
        }
        Users other = (Users) object;
        if ((this.userID == null && other.userID != null) || (this.userID != null && !this.userID.equals(other.userID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Users[ userID=" + userID + " ]";
    }
    
}
