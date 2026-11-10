package br.com.ourtalk.core;
public final class CoreChecks {
    private static void check(boolean value) { if (!value) throw new AssertionError(); }
    private static void fails(Runnable action) { try { action.run(); } catch (IllegalStateException expected) { return; } throw new AssertionError("Expected rejection"); }
    private static void invalid(Runnable action) { try { action.run(); } catch (IllegalArgumentException expected) { return; } throw new AssertionError("Expected malformed frame rejection"); }
    private static void sessionChecks() {
        var gate=new SessionGate();
        check(!gate.permitsData("peer")); fails(()->gate.connected("peer"));
        gate.start(); gate.request("peer"); fails(()->gate.challenge("stranger"));
        gate.challenge("peer"); check(!gate.permitsData("peer")); fails(()->gate.connected("peer"));
        gate.confirm("peer"); check(!gate.permitsData("peer")); fails(()->gate.connected("stranger"));
        gate.connected("peer"); check(gate.permitsData("peer")); check(!gate.permitsData("stranger"));
        gate.stop(); check(!gate.permitsData("peer")); fails(()->gate.connected("peer"));
        gate.start(); gate.challenge("incoming"); gate.confirm("incoming"); gate.connected("incoming"); check(gate.permitsData("incoming"));
        System.out.println("Session gate checks passed: consent, matching endpoint, verified connection, stop and incoming requests");
    }
    private static void frameChecks() {
        var id=java.util.UUID.randomUUID(); var data=ChatFrame.text(id,"Olá!\nTudo bem? 🌱").encode();
        var decoded=ChatFrame.decode(data); check(decoded.id.equals(id)); check(decoded.text.equals("Olá!\nTudo bem? 🌱"));
        check(ChatFrame.decode(ChatFrame.ack(id).encode()).kind==ChatFrame.Kind.ACK);
        invalid(()->ChatFrame.decode(null)); invalid(()->ChatFrame.decode(new byte[8193]));
        invalid(()->ChatFrame.text(id," ")); invalid(()->ChatFrame.text(id,"x".repeat(2001))); invalid(()->ChatFrame.text(id,"bad\u0000text"));
        for(int size=0;size<data.length;size++){final byte[] truncated=java.util.Arrays.copyOf(data,size);invalid(()->ChatFrame.decode(truncated));}
        byte[] wrongVersion=data.clone();wrongVersion[0]=2;invalid(()->ChatFrame.decode(wrongVersion));
        byte[] wrongType=data.clone();wrongType[1]=99;invalid(()->ChatFrame.decode(wrongType));
        byte[] wrongLength=data.clone();wrongLength[21]=0;invalid(()->ChatFrame.decode(wrongLength));
        byte[] badUtf=ChatFrame.text(id,"xx").encode();badUtf[22]=(byte)0xC3;badUtf[23]=0x28;invalid(()->ChatFrame.decode(badUtf));
        byte[] ackWithText=data.clone();ackWithText[1]=1;invalid(()->ChatFrame.decode(ackWithText));
        System.out.println("Frame checks passed: Unicode, ACK, size limits, truncation, version, type, length and invalid UTF-8");
    }
    private static void routeChecks() throws java.io.IOException {
        var bytes = new java.io.ByteArrayOutputStream();
        var out = new java.io.DataOutputStream(bytes);
        out.writeInt(0x4F545731);
        for (double bound : new double[]{-21.2, -47.83, -21.16, -47.78}) out.writeDouble(bound);
        out.writeInt(4);
        out.writeInt(3);
        for (int value : new int[]{-21180000, -47810000, -21180000, -47809000,
                -21180000, -47808000, -21190000, -47800000}) out.writeInt(value);
        for (int value : new int[]{0, 1, 1, 0, 1, 2}) out.writeInt(value);
        var graph = new WalkGraph(new java.io.ByteArrayInputStream(bytes.toByteArray()));
        var origin = new WalkGraph.Point(-21.18, -47.81);
        var target = new WalkGraph.Point(-21.18, -47.808);
        var route = graph.route(origin, target);
        check(route != null && route.points().size() == 3 && route.meters() > 200 && route.meters() < 215);
        check(graph.route(target, origin) == null);
        check(graph.route(origin, new WalkGraph.Point(-21.19, -47.8)) == null);
        check(graph.route(origin, new WalkGraph.Point(-21.21, -47.8)) == null);
        System.out.println("Walking route checks passed: directed paths, distance, disconnection and coverage");
    }
    public static void main(String[] args) throws java.io.IOException {
        sessionChecks(); frameChecks(); routeChecks();
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
