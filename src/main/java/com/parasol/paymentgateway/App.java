package com.parasol.paymentgateway;

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
        System.out.printf("\n\nPayment Gateway Initialised\n\n\nPAYMENT:\n\tTransaction Id: %s\n\tAmount: %.2f %s\n\tStatus: %s\n\n",
            payment.txnId, payment.amount, payment.currency, payment.status);
    }

    private App() {
    }
}