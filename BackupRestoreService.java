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

public class BackupRestoreService {
    private static Firestore db = FirestoreClient.getFirestore();

    // ==============================
    // BACKUP METHODS
    // ==============================

    public static ObservableList<BackupRecord> getAllBackups() throws Exception {
        ObservableList<BackupRecord> backups = FXCollections.observableArrayList();
        QuerySnapshot snapshot = db.collection("systemBackups")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .get();

        for (QueryDocumentSnapshot doc : snapshot) {
            BackupRecord backup = new BackupRecord(
                    doc.getId(),
                    formatTimestamp(doc.getTimestamp("createdAt")),
                    doc.getString("backupName"),
                    doc.getString("backupType"),
                    doc.getString("size"),
                    doc.getString("status"),
                    doc.getString("createdBy"),
                    doc.getString("location")
            );
            backups.add(backup);
        }
        return backups;
    }

    public static String createBackup(String backupName, String backupType, String createdBy) {
        try {
            String backupId = "BKP_" + System.currentTimeMillis();
            Map<String, Object> backupData = new HashMap<>();

            backupData.put("backupId", backupId);
            backupData.put("backupName", backupName);
            backupData.put("backupType", backupType);
            backupData.put("createdBy", createdBy);
            backupData.put("status", "IN_PROGRESS");
            backupData.put("createdAt", FieldValue.serverTimestamp());

            // Simuler la création de backup
            // En réalité, vous exporteriez les collections Firestore
            simulateBackupCreation(backupId, backupData);

            return backupId;
        } catch (Exception e) {
            System.err.println("❌ Error creating backup: " + e.getMessage());
            return null;
        }
    }

    private static void simulateBackupCreation(String backupId, Map<String, Object> backupData) {
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Simuler le temps de création

                // Mettre à jour avec les données finales
                backupData.put("status", "COMPLETED");
                backupData.put("size", "2.4 GB");
                backupData.put("location", "/backups/" + backupId + ".json");
                backupData.put("completedAt", FieldValue.serverTimestamp());

                db.collection("systemBackups").document(backupId).set(backupData);
                System.out.println("✅ Backup created: " + backupId);

            } catch (Exception e) {
                backupData.put("status", "FAILED");
                backupData.put("error", e.getMessage());
                try {
                    db.collection("systemBackups").document(backupId).set(backupData);
                } catch (Exception ex) {
                    System.err.println("❌ Error updating backup status: " + ex.getMessage());
                }
            }
        }).start();
    }

    public static boolean restoreBackup(String backupId) {
        try {
            // Simuler la restauration
            System.out.println("🔄 Starting restore of backup: " + backupId);

            // Mettre à jour le statut du backup
            Map<String, Object> update = new HashMap<>();
            update.put("status", "RESTORING");
            update.put("restoreStartedAt", FieldValue.serverTimestamp());
            db.collection("systemBackups").document(backupId).update(update);

            new Thread(() -> {
                try {
                    Thread.sleep(5000); // Simuler le temps de restauration

                    Map<String, Object> finalUpdate = new HashMap<>();
                    finalUpdate.put("status", "RESTORED");
                    finalUpdate.put("restoreCompletedAt", FieldValue.serverTimestamp());
                    db.collection("systemBackups").document(backupId).update(finalUpdate);

                    System.out.println("✅ Backup restored: " + backupId);
                } catch (Exception e) {
                    System.err.println("❌ Error during restore: " + e.getMessage());
                }
            }).start();

            return true;
        } catch (Exception e) {
            System.err.println("❌ Error initiating restore: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteBackup(String backupId) {
        try {
            db.collection("systemBackups").document(backupId).delete();
            System.out.println("🗑️ Backup deleted: " + backupId);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error deleting backup: " + e.getMessage());
            return false;
        }
    }

    public static Map<String, Object> getBackupStats() throws Exception {
        Map<String, Object> stats = new HashMap<>();

        // Total backups
        stats.put("totalBackups", db.collection("systemBackups").count().get().get().getCount());

        // Successful backups
        QuerySnapshot successful = db.collection("systemBackups")
                .whereEqualTo("status", "COMPLETED")
                .get()
                .get();
        stats.put("successfulBackups", (long) successful.size());

        // Total backup size
        double totalSize = 0;
        for (DocumentSnapshot doc : successful.getDocuments()) {
            String size = doc.getString("size");
            if (size != null && size.contains("GB")) {
                totalSize += Double.parseDouble(size.replace(" GB", ""));
            } else if (size != null && size.contains("MB")) {
                totalSize += Double.parseDouble(size.replace(" MB", "")) / 1024;
            }
        }
        stats.put("totalSizeGB", String.format("%.2f GB", totalSize));

        // Last backup
        QuerySnapshot lastBackup = db.collection("systemBackups")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .get();

        if (!lastBackup.isEmpty()) {
            DocumentSnapshot doc = lastBackup.getDocuments().get(0);
            stats.put("lastBackup", formatTimestamp(doc.getTimestamp("createdAt")));
            stats.put("lastBackupSize", doc.getString("size"));
        }

        return stats;
    }

    public static void scheduleAutoBackup(String frequency, String time) {
        try {
            Map<String, Object> schedule = new HashMap<>();
            schedule.put("frequency", frequency); // daily, weekly, monthly
            schedule.put("time", time);
            schedule.put("enabled", true);
            schedule.put("lastRun", null);
            schedule.put("nextRun", calculateNextRun(frequency, time));

            db.collection("backupSchedules").document("auto_backup").set(schedule);
            System.out.println("📅 Backup scheduled: " + frequency + " at " + time);
        } catch (Exception e) {
            System.err.println("❌ Error scheduling backup: " + e.getMessage());
        }
    }

    private static Timestamp calculateNextRun(String frequency, String time) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun;

        switch (frequency.toLowerCase()) {
            case "daily":
                nextRun = now.withHour(Integer.parseInt(time.split(":")[0]))
                        .withMinute(Integer.parseInt(time.split(":")[1]));
                if (nextRun.isBefore(now)) {
                    nextRun = nextRun.plusDays(1);
                }
                break;
            case "weekly":
                nextRun = now.with(java.time.DayOfWeek.MONDAY)
                        .withHour(Integer.parseInt(time.split(":")[0]))
                        .withMinute(Integer.parseInt(time.split(":")[1]));
                if (nextRun.isBefore(now)) {
                    nextRun = nextRun.plusWeeks(1);
                }
                break;
            case "monthly":
                nextRun = now.withDayOfMonth(1)
                        .withHour(Integer.parseInt(time.split(":")[0]))
                        .withMinute(Integer.parseInt(time.split(":")[1]));
                if (nextRun.isBefore(now)) {
                    nextRun = nextRun.plusMonths(1);
                }
                break;
            default:
                nextRun = now.plusDays(1);
        }

        return Timestamp.of(Date.from(
                nextRun.atZone(java.time.ZoneId.systemDefault()).toInstant()));
    }

    // ==============================
    // HELPER METHODS
    // ==============================

    public static String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return timestamp.toDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(formatter);
        } catch (Exception e) {
            return timestamp.toString();
        }
    }

    // ==============================
    // DATA MODEL CLASS
    // ==============================

    public static class BackupRecord {
        private String id;
        private String createdAt;
        private String backupName;
        private String backupType;
        private String size;
        private String status;
        private String createdBy;
        private String location;

        public BackupRecord(String id, String createdAt, String backupName,
                            String backupType, String size, String status,
                            String createdBy, String location) {
            this.id = id;
            this.createdAt = createdAt;
            this.backupName = backupName;
            this.backupType = backupType;
            this.size = size;
            this.status = status;
            this.createdBy = createdBy;
            this.location = location;
        }

        public String getId() { return id; }
        public String getCreatedAt() { return createdAt; }
        public String getBackupName() { return backupName; }
        public String getBackupType() { return backupType; }
        public String getSize() { return size; }
        public String getStatus() { return status; }
        public String getCreatedBy() { return createdBy; }
        public String getLocation() { return location; }
    }
}