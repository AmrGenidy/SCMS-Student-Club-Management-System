package scms.presentation;

import scms.application.SessionManager;

/**
 * Tiny holder so child controllers (Members / Events / Finance / Profile) can
 * reach the active {@link SessionManager} without each one needing to be
 * constructed manually with it injected.
 *
 * <p>The dashboard sets this immediately after a successful login. It is
 * cleared on logout so a stale session never leaks into a future login.</p>
 */
public final class SessionContext
{
    private static SessionManager current;

    private SessionContext()
    {
    }

    public static void set(SessionManager session)
    {
        current = session;
    }

    public static SessionManager get()
    {
        return current;
    }

    public static void clear()
    {
        current = null;
    }
}
