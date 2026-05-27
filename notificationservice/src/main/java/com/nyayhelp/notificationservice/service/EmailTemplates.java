package com.nyayhelp.notificationservice.service;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmailTemplates {

    public static class Rendered {
        public final String subject;
        public final String body;
        public Rendered(String subject, String body) {
            this.subject = subject;
            this.body = body;
        }
    }

    public Rendered render(String event, Map<String, Object> data) {
        Map<String, Object> d = data == null ? Map.of() : data;
        String name = str(d.get("name"), "there");

        switch (event == null ? "" : event.toUpperCase()) {

            case "CLIENT_SIGNUP":
                return new Rendered(
                        "Welcome to NyayHelp",
                        wrap("Welcome, " + name + "!",
                                "<p>Your NyayHelp citizen account has been created successfully.</p>" +
                                "<p>You can now post legal cases and connect with verified advocates.</p>"));

            case "LAWYER_SIGNUP":
                return new Rendered(
                        "Welcome to NyayHelp - Advocate Registration",
                        wrap("Welcome, Adv. " + name,
                                "<p>Your NyayHelp advocate account has been created.</p>" +
                                "<p>Please complete your verification (Aadhaar, Bar Council ID, Lawyer License and live photo) " +
                                "from your dashboard. Until verification is approved, you will not be able to bid on cases.</p>"));

            case "CASE_POSTED": {
                String title = str(d.get("title"), "your case");
                return new Rendered(
                        "Case posted successfully on NyayHelp",
                        wrap("Hello " + name + ",",
                                "<p>Your case <b>" + escape(title) + "</b> has been posted successfully.</p>" +
                                "<p>Verified advocates matching your category and location can now place bids. " +
                                "You will be notified when bids come in.</p>"));
            }

            case "BID_PLACED": {
                String title = str(d.get("caseTitle"), "the case");
                return new Rendered(
                        "Bid placed successfully on NyayHelp",
                        wrap("Hello Adv. " + name + ",",
                                "<p>Your bid on <b>" + escape(title) + "</b> has been recorded.</p>" +
                                "<p>The client will review all bids and reach out to you if selected.</p>"));
            }

            case "BID_ACCEPTED_LAWYER": {
                String title = str(d.get("caseTitle"), "a case");
                return new Rendered(
                        "Congratulations - your bid was accepted",
                        wrap("Hello Adv. " + name + ",",
                                "<p>The client has selected you for <b>" + escape(title) + "</b>.</p>" +
                                "<p>You can now communicate with the client through the secure chat from your dashboard.</p>"));
            }

            case "BID_ACCEPTED_CLIENT": {
                String title = str(d.get("caseTitle"), "your case");
                String lawyer = str(d.get("lawyerName"), "the advocate");
                return new Rendered(
                        "You have hired an advocate on NyayHelp",
                        wrap("Hello " + name + ",",
                                "<p>You have selected <b>" + escape(lawyer) + "</b> for <b>" + escape(title) + "</b>.</p>" +
                                "<p>Secure chat is now enabled from your dashboard.</p>"));
            }

            case "VERIFICATION_APPROVED":
                return new Rendered(
                        "Your NyayHelp profile is verified",
                        wrap("Hello Adv. " + name + ",",
                                "<p>Good news - your verification has been <b>approved</b> by the NyayHelp admin team.</p>" +
                                "<p>You can now place bids on cases that match your specialisation.</p>"));

            case "VERIFICATION_REJECTED": {
                String reason = str(d.get("reason"), "");
                String reasonHtml = reason.isEmpty()
                        ? ""
                        : "<p><b>Reason:</b> " + escape(reason) + "</p>";
                return new Rendered(
                        "NyayHelp verification - action required",
                        wrap("Hello Adv. " + name + ",",
                                "<p>Your verification submission was <b>rejected</b>.</p>" +
                                reasonHtml +
                                "<p>Please re-submit the required documents from your dashboard.</p>"));
            }

            case "ADMIN_VERIFICATION_SUBMITTED": {
                String lawyer = str(d.get("lawyerName"), "A lawyer");
                String email = str(d.get("lawyerEmail"), "");
                return new Rendered(
                        "New lawyer verification submitted",
                        wrap("Admin notification",
                                "<p><b>" + escape(lawyer) + "</b> (" + escape(email) + ") has submitted verification documents.</p>" +
                                "<p>Please review them in the admin dashboard.</p>"));
            }

            default:
                return new Rendered(
                        "NyayHelp notification",
                        wrap("Hello " + name + ",",
                                "<p>You have a new update from NyayHelp.</p>"));
        }
    }

    private String wrap(String heading, String body) {
        return "<div style=\"font-family:Arial,sans-serif;color:#222;\">" +
                "<h2 style=\"color:#0b3d91;margin-bottom:8px;\">" + escape(heading) + "</h2>" +
                body +
                "<hr style=\"margin-top:24px;border:none;border-top:1px solid #ccc;\"/>" +
                "<p style=\"font-size:12px;color:#777;\">This is an automated message from NyayHelp. Please do not reply.</p>" +
                "</div>";
    }

    private String str(Object o, String fallback) {
        if (o == null) return fallback;
        String s = String.valueOf(o).trim();
        return s.isEmpty() ? fallback : s;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
