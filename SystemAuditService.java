package com.sample.demo3.configuration;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SystemAuditService {
    private static Firestore db = FirestoreClient.getFirestore();

    // ==============================
    // SECURITY & AUDIT METHODS
    // ==============================

    public static ObservableList<SecurityLog> getAllSecurityLogs() throws Exception {
        ObservableList<SecurityLog> logs = FXCollections.observableArrayList();

        try {
            Query query = db.collection("securityLogs")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(100);

            QuerySnapshot snapshot = query.get().get();

            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                try {
                    // Récupération sécurisée des données
                    String id = doc.getId();
                    String timestamp = safeGetTimestampString(doc, "timestamp");
                    String userId = safeGetString(doc, "userId");
                    String action = safeGetString(doc, "action");
                    String details = safeGetString(doc, "details");
                    String ipAddress = safeGetString(doc, "ipAddress");
                    String severity = safeGetString(doc, "severity");

                    SecurityLog log = new SecurityLog(
                            id,
                            timestamp,
                            userId,
                            action,
                            details,
                            ipAddress,
                            severity
                    );
                    logs.add(log);

                } catch (Exception e) {
                    System.err.println("⚠️ Erreur traitement document " + doc.getId() + ": " + e.getMessage());
                }
            }

            System.out.println("✅ " + logs.size() + " logs de sécurité chargés");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement logs sécurité: " + e.getMessage());
            throw e;
        }

        return logs;
    }

    /**
     * Récupère une chaîne de caractères en toute sécurité
     */
    private static String safeGetString(DocumentSnapshot doc, String field) {
        try {
            Object value = doc.get(field);
            if (value == null) {
                return "";
            }
            return value.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Récupère et formate un timestamp en toute sécurité
     */
    private static String safeGetTimestampString(DocumentSnapshot doc, String field) {
        try {
            Object value = doc.get(field);

            if (value == null) {
                return "N/A";
            }

            if (value instanceof Timestamp) {
                Timestamp timestamp = (Timestamp) value;
                // Formater la date
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return timestamp.toDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .format(formatter);
            } else if (value instanceof com.google.cloud.Timestamp) {
                com.google.cloud.Timestamp timestamp = (com.google.cloud.Timestamp) value;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return timestamp.toDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .format(formatter);
            } else if (value instanceof Date) {
                Date date = (Date) value;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return date.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .format(formatter);
            } else {
                // Si c'est déjà une chaîne, la retourner
                return value.toString();
            }

        } catch (Exception e) {
            System.err.println("⚠️ Erreur formatage timestamp '" + field + "': " + e.getMessage());
            return "Error";
        }
    }

    public static ObservableList<Map<String, Object>> getAuditTrail(String userId) throws Exception {
        ObservableList<Map<String, Object>> trail = FXCollections.observableArrayList();

        Query query = db.collection("securityLogs");
        if (userId != null && !userId.isEmpty()) {
            query = query.whereEqualTo("userId", userId);
        }

        QuerySnapshot snapshot = query
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .get();

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Map<String, Object> log = new HashMap<>();

            // Copier toutes les données
            Map<String, Object> data = doc.getData();
            if (data != null) {
                log.putAll(data);
            }

            // Ajouter l'ID du document
            log.put("id", doc.getId());

            // Formater le timestamp si présent
            if (log.containsKey("timestamp")) {
                Object timestamp = log.get("timestamp");
                if (timestamp instanceof Timestamp) {
                    log.put("timestampFormatted", formatTimestamp((Timestamp) timestamp));
                }
            }

            trail.add(log);
        }

        return trail;
    }

    public static void logSecurityEvent(String userId, String action, String details,
                                        String ipAddress, String severity) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("userId", userId);
            logData.put("action", action);
            logData.put("details", details);
            logData.put("ipAddress", ipAddress);
            logData.put("severity", severity);
            logData.put("timestamp", FieldValue.serverTimestamp());

            db.collection("securityLogs").document().set(logData);
            System.out.println("🔒 Security log added: " + action + " by " + userId);
        } catch (Exception e) {
            System.err.println("❌ Error logging security event: " + e.getMessage());
        }
    }

    public static Map<String, Long> getSecurityStats() throws Exception {
        Map<String, Long> stats = new HashMap<>();

        // Total security events
        long totalEvents = db.collection("securityLogs").count().get().get().getCount();
        stats.put("totalEvents", totalEvents);

        try {
            // Failed login attempts (last 24h)
            Timestamp yesterday = Timestamp.of(Date.from(
                    java.time.Instant.now().minusSeconds(24 * 60 * 60)));

            QuerySnapshot failedLogins = db.collection("securityLogs")
                    .whereEqualTo("action", "login_failed")
                    .whereGreaterThan("timestamp", yesterday)
                    .get()
                    .get();
            stats.put("failedLogins24h", (long) failedLogins.size());

            // High severity events
            QuerySnapshot highSeverity = db.collection("securityLogs")
                    .whereEqualTo("severity", "HIGH")
                    .whereGreaterThan("timestamp", yesterday)
                    .get()
                    .get();
            stats.put("highSeverityEvents", (long) highSeverity.size());

            // Unique users with activities
            QuerySnapshot allLogs = db.collection("securityLogs")
                    .whereGreaterThan("timestamp", yesterday)
                    .get()
                    .get();
            Set<String> uniqueUsers = new HashSet<>();
            for (DocumentSnapshot doc : allLogs.getDocuments()) {
                String userId = safeGetString(doc, "userId");
                if (!userId.isEmpty()) {
                    uniqueUsers.add(userId);
                }
            }
            stats.put("activeUsers24h", (long) uniqueUsers.size());

        } catch (Exception e) {
            System.err.println("⚠️ Erreur calcul statistiques: " + e.getMessage());
            // Valeurs par défaut
            stats.put("failedLogins24h", 0L);
            stats.put("highSeverityEvents", 0L);
            stats.put("activeUsers24h", 0L);
        }

        return stats;
    }

    // ==============================
    // COMPLIANCE METHODS
    // ==============================

    public static ObservableList<ComplianceRecord> getComplianceRecords() throws Exception {
        ObservableList<ComplianceRecord> records = FXCollections.observableArrayList();

        try {
            QuerySnapshot snapshot = db.collection("complianceRecords")
                    .orderBy("checkDate", Query.Direction.DESCENDING)
                    .limit(20)
                    .get()
                    .get();

            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                try {
                    ComplianceRecord record = new ComplianceRecord(
                            doc.getId(),
                            safeGetTimestampString(doc, "checkDate"),
                            safeGetString(doc, "checkType"),
                            safeGetString(doc, "status"),
                            safeGetString(doc, "description"),
                            safeGetString(doc, "checkedBy")
                    );
                    records.add(record);
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur traitement compliance record: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement compliance: " + e.getMessage());
            throw e;
        }

        return records;
    }

    public static void addComplianceCheck(String checkType, String description,
                                          String status, String checkedBy) {
        try {
            Map<String, Object> record = new HashMap<>();
            record.put("checkType", checkType);
            record.put("description", description);
            record.put("status", status);
            record.put("checkedBy", checkedBy);
            record.put("checkDate", FieldValue.serverTimestamp());

            db.collection("complianceRecords").document().set(record);
            System.out.println("✅ Compliance check added: " + checkType);
        } catch (Exception e) {
            System.err.println("❌ Error adding compliance check: " + e.getMessage());
        }
    }

    // ==============================
    // HELPER METHODS
    // ==============================

    private static String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return timestamp.toDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(formatter);
        } catch (Exception e) {
            return "Invalid date";
        }
    }

    // ==============================
    // DATA MODEL CLASSES
    // ==============================

    public static class SecurityLog {
        private String id;
        private String timestamp;
        private String userId;
        private String action;
        private String details;
        private String ipAddress;
        private String severity;

        public SecurityLog(String id, String timestamp, String userId, String action,
                           String details, String ipAddress, String severity) {
            this.id = id;
            this.timestamp = timestamp;
            this.userId = userId;
            this.action = action;
            this.details = details;
            this.ipAddress = ipAddress;
            this.severity = severity;
        }

        public String getId() { return id; }
        public String getTimestamp() { return timestamp; }
        public String getUserId() { return userId; }
        public String getAction() { return action; }
        public String getDetails() { return details; }
        public String getIpAddress() { return ipAddress; }
        public String getSeverity() { return severity; }
    }

    public static class ComplianceRecord {
        private String id;
        private String checkDate;
        private String checkType;
        private String status;
        private String description;
        private String checkedBy;

        public ComplianceRecord(String id, String checkDate, String checkType,
                                String status, String description, String checkedBy) {
            this.id = id;
            this.checkDate = checkDate;
            this.checkType = checkType;
            this.status = status;
            this.description = description;
            this.checkedBy = checkedBy;
        }

        public String getId() { return id; }
        public String getCheckDate() { return checkDate; }
        public String getCheckType() { return checkType; }
        public String getStatus() { return status; }
        public String getDescription() { return description; }
        public String getCheckedBy() { return checkedBy; }
    }
}