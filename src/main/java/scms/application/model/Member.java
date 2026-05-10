package scms.application.model;

/**
 * Plain Java entity describing a club member.
 *
 * The {@code passwordHash} field holds the SHA-256 hex digest of the member's
 * password. Plain-text passwords are never persisted or held in memory beyond
 * the moment the user types them on the login screen.
 */
public class Member
{
    private String name;
    private String studentId;
    private String email;
    private String role;
    private String passwordHash;

    /**
     * Convenience constructor used by code paths that don't carry a password
     * (e.g. table read-outs for display, unit tests that don't exercise auth).
     */
    public Member(String name, String studentId, String email, String role)
    {
        this(name, studentId, email, role, "");
    }

    public Member(String name, String studentId, String email, String role, String passwordHash)
    {
        this.name = name;
        this.studentId = studentId;
        this.email = email;
        this.role = role;
        this.passwordHash = passwordHash;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getStudentId()
    {
        return studentId;
    }

    public void setStudentId(String studentId)
    {
        this.studentId = studentId;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public String getPasswordHash()
    {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash)
    {
        this.passwordHash = passwordHash;
    }
}
