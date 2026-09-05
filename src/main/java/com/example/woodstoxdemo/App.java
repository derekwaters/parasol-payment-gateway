package com.example.woodstoxdemo;

import java.net.URL;
import java.nio.file.Paths;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Small demo: parses a payment XML document with Jackson's XML module
 * (the "parent library"), which delegates to Woodstox as its StAX engine.
 */
public final class App {

    private static final String PAYMENT_XML = String.join(System.lineSeparator(),
            "<payment>",
            "  <txnId>PX-1001</txnId>",
            "  <amount>129.99</amount>",
            "  <currency>USD</currency>",
            "  <status>APPROVED</status>",
            "</payment>");

    public static final class Payment {
        public String txnId;
        public String currency = "USD";
        public double amount;
        public String status;

        @Override
        public String toString() {
            return String.format("Payment{txnId='%s', amount=%.2f %s, status='%s'}",
                    txnId, amount, currency, status);
        }
    }

    public static void main(String[] args) throws Exception {
        XmlMapper mapper = new XmlMapper();
        Payment payment = mapper.readValue(PAYMENT_XML, Payment.class);
        System.out.println("Parsed         : " + payment);

        System.out.println("Jackson (parent): " + resolveVersion(XmlMapper.class));
        System.out.println("Woodstox (engine): " + resolveVersion(com.ctc.wstx.stax.WstxInputFactory.class));

        String woodstox = resolveVersion(com.ctc.wstx.stax.WstxInputFactory.class);
        if (isVulnerableToCve202240152(woodstox)) {
            System.out.println("CVE-2022-40152 : VULNERABLE (woodstox < 5.4.0 or 6.0.0-6.3.x)");
        } else {
            System.out.println("CVE-2022-40152 : NOT vulnerable");
        }
    }

    private static boolean isVulnerableToCve202240152(String version) {
        if (version == null || version.isBlank()) {
            return true;
        }
        int[] v = parseVersion(version);
        return (v[0] == 5 && v[1] < 4) || (v[0] == 6 && v[1] < 4);
    }

    private static int[] parseVersion(String version) {
        String[] parts = version.split("\\.");
        int[] v = new int[3];
        for (int i = 0; i < v.length; i++) {
            try {
                v[i] = i < parts.length ? Integer.parseInt(parts[i].replaceAll("[^0-9].*$", "")) : 0;
            } catch (NumberFormatException e) {
                v[i] = 0;
            }
        }
        return v;
    }

    private static String resolveVersion(Class<?> referenced) throws Exception {
        URL location = referenced.getProtectionDomain().getCodeSource().getLocation();
        String fileName = Paths.get(location.toURI()).getFileName().toString();
        if (fileName.endsWith(".jar")) {
            String base = fileName.substring(0, fileName.length() - ".jar".length());
            int dash = base.lastIndexOf('-');
            if (dash >= 0) {
                return base.substring(dash + 1);
            }
        }
        java.lang.Package pkg = referenced.getPackage();
        String impl = pkg == null ? null : pkg.getImplementationVersion();
        return impl != null ? impl : fileName;
    }

    private App() {
    }
}