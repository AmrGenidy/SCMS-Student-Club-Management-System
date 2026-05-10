package scms.application;

import scms.application.model.Member;

/**
 * Pure-Java policy helper that answers "is this user allowed to do X?"
 * questions. Lives in the application layer so the same rules can be
 * exercised both by the JavaFX UI and by automated tests without spinning
 * up a JavaFX stage.
 *
 * <p>The mapping between roles and capabilities is intentionally explicit
 * and centralised so it is impossible to drift between the UI hiding a
 * button and the manager actually rejecting the call.</p>
 */
public final class AccessControl
{
    private AccessControl()
    {
    }

    public static boolean canManageMembers(Member user)
    {
        return isAdmin(user);
    }

    public static boolean canManageEvents(Member user)
    {
        // Admins create events; members can only view & sign up.
        return isAdmin(user);
    }

    public static boolean canSignUpForEvents(Member user)
    {
        return user != null;
    }

    public static boolean canManageFinances(Member user)
    {
        return isAdmin(user);
    }

    public static boolean canViewOwnProfile(Member user)
    {
        return user != null;
    }

    private static boolean isAdmin(Member user)
    {
        return user != null && SessionManager.ROLE_ADMIN.equalsIgnoreCase(user.getRole());
    }
}
