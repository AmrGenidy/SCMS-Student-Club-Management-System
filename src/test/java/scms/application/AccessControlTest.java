package scms.application;

import org.junit.jupiter.api.Test;
import scms.application.model.Member;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers STP T-SRS-SCMS-004 (role-based access). The same rules used by the
 * UI to hide admin buttons are exercised here so they cannot drift.
 */
class AccessControlTest
{
    private static Member admin()
    {
        return new Member("Admin", "11111111", "a@x.com", SessionManager.ROLE_ADMIN);
    }

    private static Member member()
    {
        return new Member("Member", "22222222", "m@x.com", SessionManager.ROLE_MEMBER);
    }

    @Test
    void admin_canDoEverything()
    {
        Member admin = admin();
        assertTrue(AccessControl.canManageMembers(admin));
        assertTrue(AccessControl.canManageEvents(admin));
        assertTrue(AccessControl.canManageFinances(admin));
        assertTrue(AccessControl.canSignUpForEvents(admin));
        assertTrue(AccessControl.canViewOwnProfile(admin));
    }

    @Test
    void member_cannotAccessAdminFeatures()
    {
        Member member = member();
        assertFalse(AccessControl.canManageMembers(member),
            "Members must not be able to manage other members.");
        assertFalse(AccessControl.canManageEvents(member),
            "Members must not be able to create / edit events.");
        assertFalse(AccessControl.canManageFinances(member),
            "Members must not be able to record transactions.");
    }

    @Test
    void member_canSignUpAndViewProfile()
    {
        Member member = member();
        assertTrue(AccessControl.canSignUpForEvents(member));
        assertTrue(AccessControl.canViewOwnProfile(member));
    }

    @Test
    void anonymous_canDoNothing()
    {
        assertFalse(AccessControl.canManageMembers(null));
        assertFalse(AccessControl.canManageEvents(null));
        assertFalse(AccessControl.canManageFinances(null));
        assertFalse(AccessControl.canSignUpForEvents(null));
        assertFalse(AccessControl.canViewOwnProfile(null));
    }

    @Test
    void unknownRole_treatedAsMember()
    {
        Member weirdo = new Member("?", "33333333", "?", "GUEST");
        assertFalse(AccessControl.canManageMembers(weirdo));
        assertFalse(AccessControl.canManageEvents(weirdo));
        assertFalse(AccessControl.canManageFinances(weirdo));
    }
}
