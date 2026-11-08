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
        var battery = new Guide("battery","Economizar bateria","Energia","Reduzir brilho do celular", "Android", "https://support.google.com/android/", "2026-11-08");
        var map = new Guide("map","Localização sem internet","Localização","Satélites e precisão do GPS", "GPS.gov", "https://gps.gov/", "2026-11-08");
        var guides=java.util.Arrays.asList(battery,map);
        check(OfflineSearch.find(guides,"LOCALIZACAO").get(0).id.equals("map"));
        check(OfflineSearch.find(guides,"bateria brilho").size()==1);
        check(OfflineSearch.find(guides,"   ").isEmpty());
        check(OfflineSearch.find(guides,"como e de").isEmpty());
        check(OfflineSearch.find(guides,"bateria GPS").isEmpty());
        check(OfflineSearch.find(guides,"pagar boleto").isEmpty());
        System.out.println("Offline search checks passed: accent folding, case, all terms, empty query and no fabricated results");
        System.out.println("Consent checks passed: unknown, pending, declined, accepted, blocked and invalid transitions");
    }
}
