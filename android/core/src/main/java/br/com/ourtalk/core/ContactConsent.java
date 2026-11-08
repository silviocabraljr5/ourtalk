package br.com.ourtalk.core;

/** Local consent rules; this is not authentication, identity verification or a transport. */
public final class ContactConsent {
    public enum State { UNKNOWN, PENDING, ACCEPTED, DECLINED, BLOCKED }
    private State state = State.UNKNOWN;
    public State state() { return state; }
    public void invite() {
        if (state == State.BLOCKED) throw new IllegalStateException("blocked_contact");
        if (state == State.ACCEPTED) throw new IllegalStateException("already_accepted");
        state = State.PENDING;
    }
    public void respond(boolean accepted) {
        if (state != State.PENDING) throw new IllegalStateException("no_pending_invitation");
        state = accepted ? State.ACCEPTED : State.DECLINED;
    }
    public void block() { state = State.BLOCKED; }
    public boolean mayQueueMessage() { return state == State.ACCEPTED; }
}
