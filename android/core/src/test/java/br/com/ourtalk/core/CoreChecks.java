package br.com.ourtalk.core;
public final class CoreChecks {
    private static void check(boolean value) { if (!value) throw new AssertionError(); }
    private static void fails(Runnable action) { try { action.run(); } catch (IllegalStateException expected) { return; } throw new AssertionError("Expected rejection"); }
    public static void main(String[] args) {
        var contact = new ContactConsent();
        check(!contact.mayQueueMessage());
        fails(() -> contact.respond(true));
        contact.invite(); check(!contact.mayQueueMessage());
        contact.respond(false); check(!contact.mayQueueMessage());
        contact.invite(); contact.respond(true); check(contact.mayQueueMessage());
        contact.block(); check(!contact.mayQueueMessage());
        fails(contact::invite); fails(() -> contact.respond(true));
        System.out.println("Consent checks passed: unknown, pending, declined, accepted, blocked and invalid transitions");
    }
}
