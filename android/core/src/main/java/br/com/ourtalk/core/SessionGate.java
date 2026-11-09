package br.com.ourtalk.core;

/** Consent for one live, authenticated connection. Never a saved identity. */
public final class SessionGate {
    public enum State { STOPPED, DISCOVERING, CONNECTING, VERIFYING, WAITING, CONNECTED }
    private State state = State.STOPPED;
    private String endpoint;
    public State state() { return state; }
    public String endpoint() { return endpoint; }
    public void start() { require(state == State.STOPPED); state = State.DISCOVERING; }
    public void request(String id) { require(state == State.DISCOVERING && valid(id)); endpoint=id; state=State.CONNECTING; }
    public void challenge(String id) {
        require(valid(id) && (state == State.DISCOVERING || (state == State.CONNECTING && id.equals(endpoint))));
        endpoint=id; state=State.VERIFYING;
    }
    public void confirm(String id) { require(state == State.VERIFYING && endpoint.equals(id)); state=State.WAITING; }
    public void connected(String id) { require(state == State.WAITING && endpoint.equals(id)); state=State.CONNECTED; }
    public boolean permitsData(String id) { return state == State.CONNECTED && endpoint.equals(id); }
    public void stop() { state=State.STOPPED; endpoint=null; }
    private static boolean valid(String id) { return id != null && !id.isEmpty() && id.length()<=128; }
    private static void require(boolean condition) { if (!condition) throw new IllegalStateException("Invalid session transition"); }
}
