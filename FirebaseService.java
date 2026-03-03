package com.sample.demo3.configuration;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.sample.demo3.models.*;
import com.sample.demo3.views.admin.SystemControlView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import com.google.cloud.Timestamp;
import java.time.LocalDate;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.sample.demo3.models.Incident;
import com.sample.demo3.views.admin.SystemControlView.SystemLog;

import static com.sample.demo3.configuration.BackupRestoreService.formatTimestamp;
import static com.sample.demo3.configuration.SystemAuditService.logSecurityEvent;
import static org.apache.http.client.utils.DateUtils.formatDate;

public class FirebaseService {

    private static Firestore db = FirestoreClient.getFirestore();
    private static final Executor executor = Executors.newSingleThreadExecutor();


    // Écouter les incidents en temps réel
    public static void listenToIncidentsRealtime(IncidentListener listener) {
        db.collection("incidents")
                .orderBy("reportedDate", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        System.err.println("❌ Erreur écoute incidents: " + error.getMessage());
                        return;
                    }

                    if (snapshot != null && !snapshot.isEmpty()) {
                        ObservableList<Incident> incidents = FXCollections.observableArrayList();

                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Timestamp timestamp = doc.getTimestamp("reportedDate");
                            String date = timestamp != null ?
                                    timestamp.toDate().toString() : doc.getString("date");

                            Incident incident = new Incident(
                                    doc.getString("incidentId"),
                                    doc.getString("type"),
                                    doc.getString("description"),
                                    date,
                                    doc.getString("status"),
                                    doc.getString("assignedTo"),
                                    doc.getString("driverId"),
                                    doc.getString("location")
                            );
                            incidents.add(incident);
                        }

                        listener.onIncidentsChanged(incidents);
                        System.out.println("🔄 " + incidents.size() + " incidents synchronisés en temps réel");
                    }
                });
    }

    // Interface pour recevoir les mises à jour
    public interface IncidentListener {
        void onIncidentsChanged(ObservableList<Incident> incidents);
    }

    private static String convertToString(DocumentSnapshot doc, String fieldName) {
        try {
            Object value = doc.get(fieldName);
            if (value == null) {
                return "";
            }

            // Convertir selon le type
            if (value instanceof String) {
                return (String) value;
            } else if (value instanceof Long) {
                return String.valueOf((Long) value);
            } else if (value instanceof Integer) {
                return String.valueOf((Integer) value);
            } else if (value instanceof Double) {
                return String.valueOf((Double) value);
            } else if (value instanceof Boolean) {
                return ((Boolean) value) ? "true" : "false";
            } else {
                return value.toString();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur conversion champ '" + fieldName + "': " + e.getMessage());
            return "";
        }
    }
    // ==============================
    // 1. MÉTHODES D'AUTHENTIFICATION
    // ==============================

    public static DocumentSnapshot findUser(String username) throws Exception {
        System.out.println("FirebaseService.findUser() appelé avec username: " + username);

        try {
            Firestore db = FirestoreClient.getFirestore();
            System.out.println(" Firestore client obtenu");

            CollectionReference usersCollection = db.collection("users");
            System.out.println(" Collection: 'users'");

            Query query = usersCollection
                    .whereEqualTo("username", username)
                    .limit(1);

            System.out.println(" Requête créée: where username=" + username);

            System.out.println(" Exécution de la requête...");
            ApiFuture<QuerySnapshot> future = query.get();
            QuerySnapshot querySnapshot = future.get();
            System.out.println(" Requête exécutée");
            System.out.println(" Nombre de documents trouvés: " + querySnapshot.size());

            if (querySnapshot.isEmpty()) {
                System.out.println(" Aucun utilisateur trouvé avec username: " + username);
                return null;
            }

            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
            System.out.println(" Document trouvé!");
            System.out.println(" Document ID: " + document.getId());
            return document;

        } catch (Exception e) {
            System.err.println(" ERREUR dans FirebaseService.findUser():");
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // ==============================
    // 2. MÉTHODES COACHES
    // ==============================


    public static void addCoach(Coach coach) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("id", coach.getId());
        data.put("model", coach.getModel());
        data.put("capacity", coach.getCapacity());
        data.put("licensePlate", coach.getLicensePlate());
        data.put("status", coach.getStatus());

        db.collection("coaches").document(coach.getId()).set(data);
        System.out.println("✅ Coach ajouté dans Firestore: " + coach.getId());
    }

    public static void updateCoach(Coach coach) throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("model", coach.getModel());
        updates.put("capacity", coach.getCapacity());
        updates.put("licensePlate", coach.getLicensePlate());
        updates.put("status", coach.getStatus());

        db.collection("coaches").document(coach.getId()).update(updates);
        System.out.println("✅ Coach modifié dans Firestore: " + coach.getId());
    }

    public static void deleteCoach(String coachId) throws Exception {
        db.collection("coaches").document(coachId).delete();
        System.out.println("✅ Coach supprimé de Firestore: " + coachId);
    }
    public static ObservableList<Coach> loadAllCoaches() throws Exception {
        ObservableList<Coach> coaches = FXCollections.observableArrayList();
        QuerySnapshot snapshot = db.collection("coaches").get().get();

        for (QueryDocumentSnapshot doc : snapshot) {
            // Gestion sécurisée des types
            String id = convertToString(doc, "id");
            String model = convertToString(doc, "model");
            String capacity = convertToString(doc, "capacity");
            String licensePlate = convertToString(doc, "licensePlate");
            String status = convertToString(doc, "status");

            Coach coach = new Coach(id, model, capacity, licensePlate, status);
            coaches.add(coach);
        }
        System.out.println("✅ " + coaches.size() + " coaches chargés depuis Firestore");
        return coaches;
    }

    // ==============================
    // 3. MÉTHODES TRANSPORT LINES
    // ==============================

    public static ObservableList<TransportLine> loadAllLines() throws Exception {
        ObservableList<TransportLine> lines = FXCollections.observableArrayList();
        QuerySnapshot snapshot = db.collection("transportLines").get().get();

        for (QueryDocumentSnapshot doc : snapshot) {
            TransportLine line = new TransportLine(
                    convertToString(doc, "lineId"),
                    convertToString(doc, "route"),
                    convertToString(doc, "schedule"),
                    convertToString(doc, "price"),
                    convertToString(doc, "stops")
            );
            lines.add(line);
        }
        System.out.println("✅ " + lines.size() + " lignes chargées depuis Firestore");
        return lines;
    }

    public static void addLine(TransportLine line) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("lineId", line.getLineId());
        data.put("route", line.getRoute());
        data.put("schedule", line.getSchedule());
        data.put("price", line.getPrice());
        data.put("stops", line.getStops());

        db.collection("transportLines").document(line.getLineId()).set(data);
        System.out.println("✅ Ligne ajoutée dans Firestore: " + line.getLineId());
    }

    public static void updateLine(TransportLine line) throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("route", line.getRoute());
        updates.put("schedule", line.getSchedule());
        updates.put("price", line.getPrice());
        updates.put("stops", line.getStops());

        db.collection("transportLines").document(line.getLineId()).update(updates);
        System.out.println(" Ligne modifiée dans Firestore: " + line.getLineId());
    }

    public static void deleteLine(String lineId) throws Exception {
        db.collection("transportLines").document(lineId).delete();
        System.out.println(" Ligne supprimée de Firestore: " + lineId);
    }

    // ==============================
    // 4. MÉTHODES AGENCIES
    // ==============================

    public static ObservableList<Agency> loadAllAgencies() throws Exception {
        ObservableList<Agency> agencies = FXCollections.observableArrayList();
        QuerySnapshot snapshot = db.collection("agencies").get().get();

        for (QueryDocumentSnapshot doc : snapshot) {
            Agency agency = new Agency(
                    convertToString(doc, "agencyId"),
                    convertToString(doc, "name"),
                    convertToString(doc, "location"),
                    convertToString(doc, "manager"),
                    convertToString(doc, "phone")
            );
            agencies.add(agency);
        }
        System.out.println(" " + agencies.size() + " agences chargées depuis Firestore");
        return agencies;
    }

    public static void addAgency(Agency agency) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("agencyId", agency.getAgencyId());
        data.put("name", agency.getName());
        data.put("location", agency.getLocation());
        data.put("manager", agency.getManager());
        data.put("phone", agency.getPhone());

        db.collection("agencies").document(agency.getAgencyId()).set(data);
        System.out.println("✅ Agence ajoutée dans Firestore: " + agency.getAgencyId());
    }

    public static void updateAgency(Agency agency) throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", agency.getName());
        updates.put("location", agency.getLocation());
        updates.put("manager", agency.getManager());
        updates.put("phone", agency.getPhone());

        db.collection("agencies").document(agency.getAgencyId()).update(updates);
        System.out.println("✅ Agence modifiée dans Firestore: " + agency.getAgencyId());
    }

    public static void deleteAgency(String agencyId) throws Exception {
        db.collection("agencies").document(agencyId).delete();
        System.out.println("✅ Agence supprimée de Firestore: " + agencyId);
    }

    // ==============================
    // 5. MÉTHODES DRIVERS
    // ==============================

    public static ObservableList<Driver> loadAllDrivers() throws Exception {
        ObservableList<Driver> drivers = FXCollections.observableArrayList();
        QuerySnapshot snapshot = db.collection("drivers").get().get();

        for (QueryDocumentSnapshot doc : snapshot) {
            // Gestion du salaire (Double/Long)
            Double salary = 0.0;
            Object salaryObj = doc.get("salary");
            if (salaryObj instanceof Double) {
                salary = (Double) salaryObj;
            } else if (salaryObj instanceof Long) {
                salary = ((Long) salaryObj).doubleValue();
            } else if (salaryObj instanceof Integer) {
                salary = ((Integer) salaryObj).doubleValue();
            }

            Driver driver = new Driver(
                    convertToString(doc, "driverId"),
                    convertToString(doc, "fullName"),
                    convertToString(doc, "licenseNumber"),
                    convertToString(doc, "assignedLineId"),
                    convertToString(doc, "phone"),
                    convertToString(doc, "email"),
                    convertToString(doc, "status"),
                    salary,
                    convertToString(doc, "assignedCoachId"),
                    convertToString(doc, "userId")
            );
            drivers.add(driver);
        }
        System.out.println("✅ " + drivers.size() + " chauffeurs chargés depuis Firestore");
        return drivers;
    }


    public static Driver getDriverDetails(String driverId) throws Exception {
        DocumentSnapshot doc = db.collection("drivers").document(driverId).get().get();

        if (doc.exists()) {
            return new Driver(
                    doc.getString("driverId"),
                    doc.getString("fullName"),
                    doc.getString("licenseNumber"),
                    doc.getString("assignedLineId"),
                    doc.getString("phone"),
                    doc.getString("email"),
                    doc.getString("status"),
                    doc.getDouble("salary"),
                    doc.getString("assignedCoachId"),
                    doc.getString("userId")
            );
        }
        return null;
    }

    public static void addDriver(Driver driver) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("driverId", driver.getDriverId());
        data.put("fullName", driver.getName());
        data.put("licenseNumber", driver.getLicense());
        data.put("assignedLineId", driver.getAssignedLine());
        data.put("phone", driver.getPhone());
        data.put("email", driver.getEmail());
        data.put("status", driver.getStatus());
        data.put("salary", driver.getSalary());
        data.put("assignedCoachId", driver.getAssignedCoachId());
        data.put("userId", driver.getUserId());

        db.collection("drivers").document(driver.getDriverId()).set(data);
        System.out.println("✅ Chauffeur ajouté dans Firestore: " + driver.getDriverId());
    }

    public static void updateDriver(Driver driver) throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", driver.getName());
        updates.put("licenseNumber", driver.getLicense());
        updates.put("assignedLineId", driver.getAssignedLine());
        updates.put("phone", driver.getPhone());
        updates.put("email", driver.getEmail());
        updates.put("status", driver.getStatus());
        updates.put("salary", driver.getSalary());
        updates.put("assignedCoachId", driver.getAssignedCoachId());

        db.collection("drivers").document(driver.getDriverId()).update(updates);
        System.out.println("✅ Chauffeur modifié dans Firestore: " + driver.getDriverId());
    }

    public static void deleteDriver(String driverId) throws Exception {
        db.collection("drivers").document(driverId).delete();
        System.out.println("✅ Chauffeur supprimé de Firestore: " + driverId);
    }

    // Méthode pour créer un compte utilisateur et un profil driver
    public static void createCompleteDriverAccount(String username, String password,
                                                   String fullName, String email, String phone, String licenseNumber,
                                                   String licenseExpiry, String hireDate, double salary,
                                                   String assignedCoachId, String assignedLineId) throws Exception {

        // 1. Générer un ID driver
        String driverId = generateNextDriverId();

        // 2. Créer le compte utilisateur dans 'users'
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("password", password);
        userData.put("fullname", fullName);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("role", "driver");

        db.collection("users").document(username).set(userData);

        // 3. Créer le profil driver dans 'drivers'
        Map<String, Object> driverData = new HashMap<>();
        driverData.put("driverId", driverId);
        driverData.put("userId", username);
        driverData.put("fullName", fullName);
        driverData.put("licenseNumber", licenseNumber);
        driverData.put("licenseExpiry", licenseExpiry);
        driverData.put("hireDate", hireDate);
        driverData.put("salary", salary);
        driverData.put("status", "active");
        driverData.put("assignedCoachId", assignedCoachId);
        driverData.put("assignedLineId", assignedLineId);
        driverData.put("phone", phone);
        driverData.put("email", email);

        db.collection("drivers").document(driverId).set(driverData);

        System.out.println("✅ Compte driver complet créé:");
        System.out.println("   Username: " + username);
        System.out.println("   Driver ID: " + driverId);
    }

    private static String generateNextDriverId() throws Exception {
        QuerySnapshot snapshot = db.collection("drivers").get().get();
        int maxId = 0;

        for (QueryDocumentSnapshot doc : snapshot) {
            String id = doc.getString("driverId");
            if (id != null && id.startsWith("D")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxId) maxId = num;
                } catch (NumberFormatException e) {
                    // Ignore non-numeric IDs
                }
            }
        }
        return "D" + String.format("%03d", maxId + 1);
    }

    public static Coach getAssignedCoach(String driverId) throws Exception {
        Driver driver = getDriverDetails(driverId);
        if (driver != null && driver.getAssignedCoachId() != null) {
            DocumentSnapshot doc = db.collection("coaches")
                    .document(driver.getAssignedCoachId()).get().get();

            if (doc.exists()) {
                return new Coach(
                        doc.getString("id"),
                        doc.getString("model"),
                        doc.getString("capacity"),
                        doc.getString("licensePlate"),
                        doc.getString("status")
                );
            }
        }
        return null;
    }

    // ==============================
    // 6. MÉTHODES RECLAMATIONS (CLAIMS)
    // ==============================

    public static ObservableList<Reclamation> loadAllReclamations() throws Exception {
        ObservableList<Reclamation> reclamations = FXCollections.observableArrayList();
        QuerySnapshot snapshot = db.collection("reclamations").get().get();

        for (QueryDocumentSnapshot doc : snapshot) {
            Reclamation reclamation = new Reclamation(
                    convertToString(doc, "id"),
                    convertToString(doc, "date"),
                    convertToString(doc, "customer"),
                    convertToString(doc, "type"),
                    convertToString(doc, "description"),
                    convertToString(doc, "status")
            );
            reclamations.add(reclamation);
        }
        System.out.println("✅ " + reclamations.size() + " réclamations chargées depuis Firestore");
        return reclamations;
    }


    public static void addReclamation(Reclamation reclamation) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("id", reclamation.getId());
        data.put("date", reclamation.getDate());
        data.put("customer", reclamation.getCustomer());
        data.put("type", reclamation.getType());
        data.put("description", reclamation.getDescription());
        data.put("status", reclamation.getStatus());

        db.collection("reclamations").document(reclamation.getId()).set(data);
        System.out.println("✅ Réclamation ajoutée dans Firestore: " + reclamation.getId());
    }

    public static void updateReclamationStatus(String reclamationId, String newStatus) throws Exception {
        Map<String, Object> update = new HashMap<>();
        update.put("status", newStatus);

        db.collection("reclamations").document(reclamationId).update(update);
        System.out.println("✅ Statut réclamation modifié dans Firestore: " + reclamationId + " -> " + newStatus);
    }

    public static void deleteReclamation(String reclamationId) throws Exception {
        db.collection("reclamations").document(reclamationId).delete();
        System.out.println("✅ Réclamation supprimée de Firestore: " + reclamationId);
    }

    // ==============================
    // 7. MÉTHODES INCIDENTS (Nouveau)
    // ==============================

    public static ObservableList<Incident> loadAllIncidents() throws Exception {
        ObservableList<Incident> incidents = FXCollections.observableArrayList();
        QuerySnapshot snapshot = db.collection("incidents")
                .orderBy("reportedDate", Query.Direction.DESCENDING)
                .get()
                .get();

        for (QueryDocumentSnapshot doc : snapshot) {
            Timestamp timestamp = doc.getTimestamp("reportedDate");
            String date = timestamp != null ?
                    timestamp.toDate().toString() : doc.getString("date");

            Incident incident = new Incident(
                    doc.getString("incidentId"),
                    doc.getString("type"),
                    doc.getString("description"),
                    date,
                    doc.getString("status"),
                    doc.getString("assignedTo"),
                    doc.getString("driverId"),
                    doc.getString("location")
            );
            incidents.add(incident);
        }
        System.out.println("✅ " + incidents.size() + " incidents chargés depuis Firestore");
        return incidents;
    }

    public static ObservableList<Incident> getDriverIncidents(String driverId) throws Exception {
        ObservableList<Incident> incidents = FXCollections.observableArrayList();

        Query query = db.collection("incidents")
                .whereEqualTo("driverId", driverId)
                .orderBy("reportedDate", Query.Direction.DESCENDING);

        QuerySnapshot snapshot = query.get().get();

        for (QueryDocumentSnapshot doc : snapshot) {
            Timestamp timestamp = doc.getTimestamp("reportedDate");
            String date = timestamp != null ?
                    timestamp.toDate().toString() : "Unknown";

            Incident incident = new Incident(
                    doc.getString("incidentId"),
                    doc.getString("type"),
                    doc.getString("description"),
                    date,
                    doc.getString("status"),
                    doc.getString("assignedTo"),
                    doc.getString("driverId"),
                    doc.getString("location")
            );
            incidents.add(incident);
        }

        return incidents;
    }

    public static void reportIncident(String incidentId, String driverId, String type,
                                      String description, String location) throws Exception {
        Map<String, Object> incidentData = new HashMap<>();
        incidentData.put("incidentId", incidentId);
        incidentData.put("driverId", driverId);
        incidentData.put("type", type);
        incidentData.put("description", description);
        incidentData.put("location", location);
        incidentData.put("status", "pending");
        incidentData.put("reportedDate", FieldValue.serverTimestamp());
        incidentData.put("assignedTo", "");

        db.collection("incidents").document(incidentId).set(incidentData);

        System.out.println("✅ Incident reported by driver " + driverId + ": " + incidentId);
    }

    public static void updateIncidentStatus(String incidentId, String newStatus) throws Exception {
        Map<String, Object> update = new HashMap<>();
        update.put("status", newStatus);

        db.collection("incidents").document(incidentId).update(update);
        System.out.println("✅ Incident status updated: " + incidentId + " -> " + newStatus);
    }

    public static void assignIncidentToManager(String incidentId, String managerUsername) throws Exception {
        Map<String, Object> update = new HashMap<>();
        update.put("assignedTo", managerUsername);
        update.put("status", "in_progress");

        db.collection("incidents").document(incidentId).update(update);
        System.out.println("✅ Incident assigned to manager: " + incidentId + " -> " + managerUsername);
    }

    public static String generateIncidentId() throws Exception {
        QuerySnapshot snapshot = db.collection("incidents").get().get();
        int maxId = 0;

        for (QueryDocumentSnapshot doc : snapshot) {
            String id = doc.getString("incidentId");
            if (id != null && id.startsWith("INC")) {
                try {
                    int num = Integer.parseInt(id.substring(3));
                    if (num > maxId) maxId = num;
                } catch (NumberFormatException e) {
                    // Ignore non-numeric IDs
                }
            }
        }

        return "INC" + String.format("%03d", maxId + 1);
    }

    // ==============================
    // 8. MÉTHODES STATISTIQUES ET RAPPORTS
    // ==============================

    public static Map<String, Object> getDashboardStats() throws Exception {
        Map<String, Object> stats = new HashMap<>();

        // Nombre total de drivers - gérer les types différents
        ApiFuture<AggregateQuerySnapshot> driversFuture = db.collection("drivers").count().get();
        AggregateQuerySnapshot driversSnapshot = driversFuture.get();
        stats.put("totalDrivers", (long) driversSnapshot.getCount());

        // Nombre de drivers actifs
        QuerySnapshot activeDriversSnapshot = db.collection("drivers")
                .whereEqualTo("status", "active")
                .get()
                .get();
        stats.put("activeDrivers", (long) activeDriversSnapshot.size());

        // Nombre total de coaches
        ApiFuture<AggregateQuerySnapshot> coachesFuture = db.collection("coaches").count().get();
        AggregateQuerySnapshot coachesSnapshot = coachesFuture.get();
        stats.put("totalCoaches", (long) coachesSnapshot.getCount());

        // Nombre de coaches actifs
        QuerySnapshot activeCoachesSnapshot = db.collection("coaches")
                .whereEqualTo("status", "active")
                .get()
                .get();
        stats.put("activeCoaches", (long) activeCoachesSnapshot.size());

        // Nombre d'incidents en attente
        QuerySnapshot pendingIncidentsSnapshot = db.collection("incidents")
                .whereEqualTo("status", "pending")
                .get()
                .get();
        stats.put("pendingIncidents", (long) pendingIncidentsSnapshot.size());

        // Nombre total d'incidents
        ApiFuture<AggregateQuerySnapshot> incidentsFuture = db.collection("incidents").count().get();
        AggregateQuerySnapshot incidentsSnapshot = incidentsFuture.get();
        stats.put("totalIncidents", (long) incidentsSnapshot.getCount());

        System.out.println("✅ Statistiques chargées depuis Firestore: " + stats);
        return stats;
    }

    /**
     * Convertit n'importe quel objet numérique en Long
     */
    public static Long safeConvertToLong(Object value) {
        if (value == null) return 0L;

        try {
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof Integer) {
                return ((Integer) value).longValue();
            } else if (value instanceof Double) {
                return ((Double) value).longValue();
            } else if (value instanceof String) {
                try {
                    return Long.parseLong((String) value);
                } catch (NumberFormatException e) {
                    return 0L;
                }
            } else {
                return 0L;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error converting to Long: " + e.getMessage());
            return 0L;
        }
    }

    /**
     * Version sécurisée de getDashboardStats qui retourne tous les valeurs comme Long
     */
    public static Map<String, Long> getSafeDashboardStats() throws Exception {
        Map<String, Long> stats = new HashMap<>();

        Map<String, Object> rawStats = getDashboardStats();

        for (Map.Entry<String, Object> entry : rawStats.entrySet()) {
            stats.put(entry.getKey(), safeConvertToLong(entry.getValue()));
        }

        return stats;
    }

    // ==============================
    // 9. MÉTHODES UTILITAIRES
    // ==============================

    public static boolean checkUserExists(String username) throws Exception {
        Query query = db.collection("users")
                .whereEqualTo("username", username)
                .limit(1);

        QuerySnapshot snapshot = query.get().get();
        return !snapshot.isEmpty();
    }

    public static void updateUserPassword(String username, String newPassword) throws Exception {
        Map<String, Object> update = new HashMap<>();
        update.put("password", newPassword);

        db.collection("users").document(username).update(update);
        System.out.println("✅ Password updated for user: " + username);
    }


    public static void generateSampleData() throws Exception {
        System.out.println("🎲 Génération de données de test...");

        // Vérifier si des données existent déjà
        long driversCount = db.collection("drivers").count().get().get().getCount();
        long coachesCount = db.collection("coaches").count().get().get().getCount();

        if (driversCount > 0 || coachesCount > 0) {
            System.out.println("⚠️ Des données existent déjà, génération annulée");
            return;
        }
    }

// ==============================
// 10. MÉTHODES USER MANAGEMENT POUR ADMIN
// ==============================

    public static ObservableList<Map<String, Object>> getAllUsersWithDetails() throws Exception {
        ObservableList<Map<String, Object>> users = FXCollections.observableArrayList();
        QuerySnapshot snapshot = db.collection("users").get().get();

        for (QueryDocumentSnapshot doc : snapshot) {
            Map<String, Object> userData = new HashMap<>(doc.getData());
            userData.put("documentId", doc.getId());
            userData.put("createdAt", doc.getCreateTime() != null ?
                    formatDate(doc.getCreateTime().toDate()) : "N/A");

            // Vérifier le statut pour les drivers
            if ("driver".equals(userData.get("role"))) {
                userData.put("driverStatus", getDriverStatusByUsername((String) userData.get("username")));
            } else {
                userData.put("driverStatus", "N/A");
            }

            users.add(userData);
        }

        System.out.println("✅ " + users.size() + " utilisateurs chargés avec détails");
        return users;
    }

    private static String getDriverStatusByUsername(String username) throws Exception {
        try {
            Query query = db.collection("drivers")
                    .whereEqualTo("userId", username)
                    .limit(1);

            QuerySnapshot snapshot = query.get().get();
            if (!snapshot.isEmpty()) {
                return snapshot.getDocuments().get(0).getString("status");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error getting driver status: " + e.getMessage());
        }
        return "Unknown";
    }

    public static void createNewUser(String username, String password, String fullName,
                                     String email, String phone, String role) throws Exception {

        // Vérifier si l'utilisateur existe déjà
        if (checkUserExists(username)) {
            throw new Exception("Username already exists: " + username);
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("password", password);
        userData.put("fullname", fullName);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("role", role);
        userData.put("status", "active");
        userData.put("createdAt", FieldValue.serverTimestamp());
        userData.put("lastLogin", null);

        db.collection("users").document(username).set(userData);
        System.out.println("✅ User created: " + username);

        // Si c'est un driver, créer un profil driver
        if ("driver".equals(role)) {
            createDriverProfileForUser(username, fullName, email, phone);
        }
    }

    private static void createDriverProfileForUser(String username, String fullName,
                                                   String email, String phone) throws Exception {
        String driverId = generateNextDriverId();

        Map<String, Object> driverData = new HashMap<>();
        driverData.put("driverId", driverId);
        driverData.put("userId", username);
        driverData.put("fullName", fullName);
        driverData.put("email", email);
        driverData.put("phone", phone);
        driverData.put("status", "active");
        driverData.put("hireDate", java.time.LocalDate.now().toString());
        driverData.put("salary", 5000.0);
        driverData.put("licenseNumber", "TO-BE-ASSIGNED");
        driverData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("drivers").document(driverId).set(driverData);
        System.out.println("✅ Driver profile created: " + driverId + " for user: " + username);
    }

    public static void updateUserDetails(String username, Map<String, Object> updates) throws Exception {
        updates.put("updatedAt", FieldValue.serverTimestamp());
        db.collection("users").document(username).update(updates);
        System.out.println("✅ User updated: " + username);

        // Si le rôle change, mettre à jour les profils associés
        if (updates.containsKey("role")) {
            String newRole = (String) updates.get("role");
            if ("driver".equals(newRole)) {
                // Vérifier si un profil driver existe déjà
                Query query = db.collection("drivers").whereEqualTo("userId", username);
                QuerySnapshot snapshot = query.get().get();
                if (snapshot.isEmpty()) {
                    // Créer un profil driver
                    String fullName = (String) updates.getOrDefault("fullname", "");
                    String email = (String) updates.getOrDefault("email", "");
                    String phone = (String) updates.getOrDefault("phone", "");
                    createDriverProfileForUser(username, fullName, email, phone);
                }
            }
        }
    }

    public static void deleteUserAndAssociatedData(String username) throws Exception {
        // 1. Récupérer le rôle de l'utilisateur
        DocumentSnapshot userDoc = db.collection("users").document(username).get().get();
        if (!userDoc.exists()) {
            throw new Exception("User not found: " + username);
        }

        String role = userDoc.getString("role");

        // 2. Supprimer l'utilisateur
        db.collection("users").document(username).delete();
        System.out.println("✅ User deleted: " + username);

        // 3. Supprimer les données associées selon le rôle
        if ("driver".equals(role)) {
            // Supprimer le profil driver
            Query query = db.collection("drivers").whereEqualTo("userId", username);
            QuerySnapshot snapshot = query.get().get();
            for (DocumentSnapshot driverDoc : snapshot.getDocuments()) {
                db.collection("drivers").document(driverDoc.getId()).delete();
                System.out.println("✅ Driver profile deleted: " + driverDoc.getId());
            }
        }
    }

    public static Map<String, Long> getUserStatistics() throws Exception {
        Map<String, Long> stats = new HashMap<>();
        Firestore db = FirestoreClient.getFirestore();

        // Total users - méthode simplifiée
        List<QueryDocumentSnapshot> allUsers = db.collection("users").get().get().getDocuments();
        stats.put("totalUsers", (long) allUsers.size());

        // Users by role
        long admins = 0, managers = 0, drivers = 0, activeUsers = 0;

        for (QueryDocumentSnapshot doc : allUsers) {
            String role = doc.getString("role");
            String status = doc.getString("status");

            if (role != null) {
                switch (role.toLowerCase()) {
                    case "admin":
                        admins++;
                        break;
                    case "manager":
                        managers++;
                        break;
                    case "driver":
                        drivers++;
                        break;
                }
            }

            if ("active".equals(status)) {
                activeUsers++;
            }
        }

        stats.put("admins", admins);
        stats.put("managers", managers);
        stats.put("drivers", drivers);
        stats.put("activeUsers", activeUsers);

        System.out.println("📊 User statistics calculated: " + stats);
        return stats;
    }
    // System Control
    public static Map<String, Object> getSystemStatus() throws Exception {
        Map<String, Object> status = new HashMap<>();

        // Récupérer les statistiques en temps réel
        status.put("totalUsers", db.collection("users").count().get().get().getCount());
        status.put("activeDrivers", db.collection("drivers").whereEqualTo("status", "active").count().get().get().getCount());
        status.put("activeIncidents", db.collection("incidents").whereEqualTo("status", "pending").count().get().get().getCount());
        status.put("totalCoaches", db.collection("coaches").count().get().get().getCount());

        return status;
    }

    // ==============================
// SYSTEM SETTINGS METHODS
// ==============================

    public static void saveSystemSettings(Map<String, Object> settings) throws Exception {
        // Ajouter des métadonnées
        settings.put("lastModified", FieldValue.serverTimestamp());
        settings.put("modifiedBy", "admin");

        db.collection("systemSettings").document("current").set(settings);
        System.out.println("✅ System settings saved");
    }

    public static Map<String, Object> loadSystemSettings() throws Exception {
        DocumentSnapshot doc = db.collection("systemSettings").document("current").get().get();

        if (doc.exists()) {
            return doc.getData();
        } else {
            // Retourner des paramètres par défaut
            Map<String, Object> defaultSettings = new HashMap<>();
            defaultSettings.put("appName", "Translink Transport System");
            defaultSettings.put("companyName", "Translink Inc.");
            defaultSettings.put("supportEmail", "support@translink.com");
            defaultSettings.put("supportPhone", "+1-800-123-4567");
            defaultSettings.put("systemUrl", "https://system.translink.com");
            defaultSettings.put("maintenanceMode", false);
            defaultSettings.put("autoBackup", true);
            defaultSettings.put("notifications", true);
            defaultSettings.put("auditLog", true);
            defaultSettings.put("sessionTimeout", 30);
            defaultSettings.put("maxLoginAttempts", 5);
            defaultSettings.put("backupRetention", 30);
            defaultSettings.put("emailTemplate", "Dear {user},\\n\\nSystem notification: {message}\\n\\nBest regards,\\nTranslink Team");
            defaultSettings.put("smsTemplate", "Translink Alert: {message}. Reply STOP to unsubscribe.");

            return defaultSettings;
        }
    }


    //SYSTEMCONTROL
       public static Map<String, Object> getLiveSystemStats() throws Exception {
           Map<String, Object> stats = new HashMap<>();

           // Nombre total d'utilisateurs
           ApiFuture<AggregateQuerySnapshot> totalUsersFuture = db.collection("users").count().get();
           stats.put("totalUsers", (long) totalUsersFuture.get().getCount());

           // Drivers actifs
           QuerySnapshot activeDriversSnapshot = db.collection("drivers")
                   .whereEqualTo("status", "Active")
                   .get()
                   .get();
           stats.put("activeDrivers", (long) activeDriversSnapshot.size());

           // Managers actifs
           QuerySnapshot activeManagersSnapshot = db.collection("users")
                   .whereEqualTo("role", "manager")
                   .get()
                   .get();
           stats.put("activeManagers", (long) activeManagersSnapshot.size());

           // Coaches actifs
           QuerySnapshot activeCoachesSnapshot = db.collection("coaches")
                   .whereEqualTo("status", "Active")
                   .get()
                   .get();
           stats.put("activeCoaches", (long) activeCoachesSnapshot.size());

           // Incidents en attente
           QuerySnapshot pendingIncidentsSnapshot = db.collection("incidents")
                   .whereEqualTo("status", "pending")
                   .get()
                   .get();
           stats.put("pendingIncidents", (long) pendingIncidentsSnapshot.size());

           // Total des incidents
           ApiFuture<AggregateQuerySnapshot> totalIncidentsFuture = db.collection("incidents").count().get();
           stats.put("totalIncidents", (long) totalIncidentsFuture.get().getCount());

           // Statistiques CPU et mémoire (simulées)
           Random random = new Random();
           stats.put("cpuUsage", 40 + random.nextInt(20));
           stats.put("memoryUsage", 60 + random.nextInt(20));

           System.out.println("📊 Live system stats loaded from Firestore");
           return stats;
       }

    /**
     * Récupère les positions GPS en temps réel de tous les véhicules
     */
    public static void listenToVehiclePositions(RealtimeListener<VehiclePosition> listener) {
        db.collection("vehiclePositions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50) // Limiter à 50 dernières positions
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        System.err.println("❌ Erreur écoute positions véhicules: " + error.getMessage());
                        return;
                    }

                    if (snapshot != null && !snapshot.isEmpty()) {
                        ObservableList<VehiclePosition> positions = FXCollections.observableArrayList();

                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Timestamp timestamp = doc.getTimestamp("timestamp");
                            VehiclePosition position = new VehiclePosition(
                                    doc.getId(),
                                    convertToString(doc, "coachId"),
                                    convertToString(doc, "driverId"),
                                    doc.getDouble("latitude"),
                                    doc.getDouble("longitude"),
                                    doc.getDouble("speed"),
                                    convertToString(doc, "heading"),
                                    timestamp != null ? timestamp.toDate().toString() : "",
                                    convertToString(doc, "status")
                            );
                            positions.add(position);
                        }

                        listener.onDataChanged(positions);
                    }
                });
    }

    /**
     * Récupère les activités utilisateur en temps réel
     */
    public static void listenToUserActivities(RealtimeListener<UserActivity> listener) {
        db.collection("userActivities")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        System.err.println("❌ Erreur écoute activités utilisateur: " + error.getMessage());
                        return;
                    }

                    if (snapshot != null && !snapshot.isEmpty()) {
                        ObservableList<UserActivity> activities = FXCollections.observableArrayList();

                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Timestamp timestamp = doc.getTimestamp("timestamp");
                            UserActivity activity = new UserActivity(
                                    doc.getId(),
                                    convertToString(doc, "userId"),
                                    convertToString(doc, "activityType"),
                                    convertToString(doc, "details"),
                                    timestamp != null ? timestamp.toDate().toString() : "",
                                    convertToString(doc, "location"),
                                    convertToString(doc, "sessionId"),
                                    convertToString(doc, "details"),
                                    convertToString(doc, "location")
                            );
                            activities.add(activity);
                        }

                        listener.onDataChanged(activities);
                    }
                });
    }

    /**
     * Récupère les métriques système en temps réel
     */
    public static void listenToSystemMetrics(RealtimeListener<SystemMetric> listener) {
        db.collection("systemMetrics")
                .whereGreaterThan("timestamp",
                        Timestamp.of(Date.from(Instant.now().minus(1, ChronoUnit.HOURS))))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        System.err.println("❌ Erreur écoute métriques système: " + error.getMessage());
                        return;
                    }

                    if (snapshot != null && !snapshot.isEmpty()) {
                        ObservableList<SystemMetric> metrics = FXCollections.observableArrayList();

                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Timestamp timestamp = doc.getTimestamp("timestamp");
                            SystemMetric metric = new SystemMetric(
                                    doc.getId(),
                                    convertToString(doc, "type"),
                                    doc.getDouble("value"),
                                    convertToString(doc, "unit"),
                                    timestamp != null ? timestamp.toDate().toString() : "",
                                    convertToString(doc, "source")
                            );
                            metrics.add(metric);
                        }

                        listener.onDataChanged(metrics);
                    }
                });
    }

    // Interface générique pour les écouteurs temps réel
    public interface RealtimeListener<T> {
        void onDataChanged(ObservableList<T> data);
    }

// ==============================
// ANALYTICS METHODS
// ==============================

    /**
     * Récupère les statistiques d'utilisation par période
     */
    public static Map<String, Object> getUsageAnalytics(String period) throws Exception {
        Map<String, Object> analytics = new HashMap<>();

        LocalDate now = LocalDate.now();
        LocalDate startDate;

        switch (period.toLowerCase()) {
            case "today":
                startDate = now;
                break;
            case "week":
                startDate = now.minusDays(7);
                break;
            case "month":
                startDate = now.minusDays(30);
                break;
            default:
                startDate = now.minusDays(7);
        }

        Timestamp startTimestamp = Timestamp.of(Date.from(
                startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));

        // Nombre de connexions utilisateur
        QuerySnapshot logins = db.collection("userActivities")
                .whereEqualTo("activityType", "login")
                .whereGreaterThan("timestamp", startTimestamp)
                .get()
                .get();
        analytics.put("userLogins", logins.size());

        // Nombre d'incidents signalés
        QuerySnapshot incidents = db.collection("incidents")
                .whereGreaterThan("reportedDate", startTimestamp)
                .get()
                .get();
        analytics.put("incidentsReported", incidents.size());

        // Nombre de réclamations
        QuerySnapshot claims = db.collection("reclamations")
                .whereGreaterThan("date", startDate.toString())
                .get()
                .get();
        analytics.put("customerClaims", claims.size());

        // Utilisation par rôle
        Map<String, Long> usageByRole = new HashMap<>();
        QuerySnapshot allActivities = db.collection("userActivities")
                .whereGreaterThan("timestamp", startTimestamp)
                .get()
                .get();

        for (DocumentSnapshot doc : allActivities) {
            String userId = doc.getString("userId");
            if (userId != null) {
                // Récupérer le rôle de l'utilisateur
                DocumentSnapshot userDoc = db.collection("users")
                        .document(userId)
                        .get()
                        .get();
                if (userDoc.exists()) {
                    String role = userDoc.getString("role");
                    usageByRole.merge(role, 1L, Long::sum);
                }
            }
        }
        analytics.put("usageByRole", usageByRole);

        // Performance système moyenne
        QuerySnapshot cpuMetrics = db.collection("systemMetrics")
                .whereEqualTo("type", "cpu")
                .whereGreaterThan("timestamp", startTimestamp)
                .get()
                .get();

        double cpuTotal = 0;
        for (DocumentSnapshot doc : cpuMetrics) {
            cpuTotal += doc.getDouble("value");
        }
        analytics.put("avgCpuUsage", cpuMetrics.isEmpty() ? 0 : cpuTotal / cpuMetrics.size());

        return analytics;
    }

    /**
     * Récupère les données pour les graphiques
     */
    public static Map<String, List<Map<String, Object>>> getChartData() throws Exception {
        Map<String, List<Map<String, Object>>> chartData = new HashMap<>();

        // Données pour le graphique des incidents par jour
        List<Map<String, Object>> incidentsByDay = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.toString();

            // Compter les incidents pour cette date
            QuerySnapshot incidents = db.collection("incidents")
                    .whereGreaterThanOrEqualTo("reportedDate",
                            Timestamp.of(Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())))
                    .whereLessThan("reportedDate",
                            Timestamp.of(Date.from(date.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())))
                    .get()
                    .get();

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dateStr);
            dayData.put("incidents", incidents.size());
            incidentsByDay.add(dayData);
        }
        chartData.put("incidentsByDay", incidentsByDay);

        // Données pour le graphique des utilisateurs par rôle
        List<Map<String, Object>> usersByRole = new ArrayList<>();

        // Compter les utilisateurs par rôle
        long admins = db.collection("users").whereEqualTo("role", "admin").count().get().get().getCount();
        long managers = db.collection("users").whereEqualTo("role", "manager").count().get().get().getCount();
        long drivers = db.collection("users").whereEqualTo("role", "driver").count().get().get().getCount();

        usersByRole.add(Map.of("role", "Admins", "count", admins));
        usersByRole.add(Map.of("role", "Managers", "count", managers));
        usersByRole.add(Map.of("role", "Drivers", "count", drivers));
        chartData.put("usersByRole", usersByRole);

        // Données pour le graphique de performance système
        List<Map<String, Object>> systemPerformance = new ArrayList<>();
        QuerySnapshot recentMetrics = db.collection("systemMetrics")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .get();

        for (DocumentSnapshot doc : recentMetrics) {
            Map<String, Object> metricData = new HashMap<>();
            metricData.put("time", doc.getTimestamp("timestamp").toDate().toString());
            metricData.put("value", doc.getDouble("value"));
            metricData.put("type", doc.getString("type"));
            systemPerformance.add(metricData);
        }
        chartData.put("systemPerformance", systemPerformance);

        return chartData;
    }

    /**
     * Génère un rapport analytique complet
     */
    public static Map<String, Object> generateAnalyticsReport(String reportType) throws Exception {
        Map<String, Object> report = new HashMap<>();
        report.put("generatedAt", new Date().toString());
        report.put("reportType", reportType);

        switch (reportType) {
            case "daily":
                report.putAll(getDailyReport());
                break;
            case "weekly":
                report.putAll(getWeeklyReport());
                break;
            case "monthly":
                report.putAll(getMonthlyReport());
                break;
            default:
                report.putAll(getDailyReport());
        }

        // Enregistrer le rapport dans Firestore
        db.collection("analyticsReports").document().set(report);

        return report;
    }

    private static Map<String, Object> getDailyReport() throws Exception {
        Map<String, Object> daily = new HashMap<>();

        LocalDate today = LocalDate.now();

        // Statistiques du jour
        daily.put("date", today.toString());
        daily.put("activeUsers", getActiveUsersCount(today));
        daily.put("newIncidents", getNewIncidentsCount(today));
        daily.put("resolvedIncidents", getResolvedIncidentsCount(today));
        daily.put("systemUptime", 99.8); // Valeur fixe pour l'instant
        daily.put("peakUsageTime", "14:30"); // Valeur fixe pour l'instant

        return daily;
    }

    // Méthodes auxiliaires
    private static long getActiveUsersCount(LocalDate date) throws Exception {
        Timestamp start = Timestamp.of(Date.from(
                date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        Timestamp end = Timestamp.of(Date.from(
                date.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));

        QuerySnapshot activities = db.collection("userActivities")
                .whereEqualTo("activityType", "login")
                .whereGreaterThanOrEqualTo("timestamp", start)
                .whereLessThan("timestamp", end)
                .get()
                .get();

        return activities.getDocuments().stream()
                .map(doc -> doc.getString("userId"))
                .distinct()
                .count();
    }

    private static long getNewIncidentsCount(LocalDate date) throws Exception {
        Timestamp start = Timestamp.of(Date.from(
                date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        Timestamp end = Timestamp.of(Date.from(
                date.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));

        return db.collection("incidents")
                .whereGreaterThanOrEqualTo("reportedDate", start)
                .whereLessThan("reportedDate", end)
                .count()
                .get()
                .get()
                .getCount();
    }

    private static double calculateSystemUptime(LocalDate date) throws Exception {
        // Pour l'instant, retourner une valeur simulée
        // En production, vous calculeriez cela basé sur les logs système
        return 99.8;
    }

    private static String getPeakUsageTime(LocalDate date) throws Exception {
        // Analyse des activités pour trouver l'heure de pointe
        // Retourne l'heure avec le plus d'activités
        return "14:30";
    }
// ==============================
// MONITORING DATA GENERATION
// ==============================

    /**
     * Génère des données de monitoring pour les nouvelles collections
     * À exécuter une seule fois pour initialiser les collections
     */
    public static void generateMonitoringData() throws Exception {
        System.out.println("🎲 Génération de données de monitoring...");

        LocalDate today = LocalDate.now();
        Random random = new Random();

        // 1. Générer des positions de véhicule
        System.out.println("📍 Génération des positions de véhicules...");

        // Récupérer les coaches existants
        ObservableList<Coach> coaches = loadAllCoaches();
        // Récupérer les drivers existants
        ObservableList<Driver> drivers = loadAllDrivers();

        if (coaches.isEmpty() || drivers.isEmpty()) {
            System.out.println("⚠️ Aucun coach ou driver trouvé, génération de données de test...");
            // Générer des coaches si aucun n'existe
            if (coaches.isEmpty()) {
                for (int i = 1; i <= 3; i++) {
                    Coach coach = new Coach(
                            "C" + (800 + i),
                            "Model " + i,
                            String.valueOf(40 + i * 3),
                            "PLATE-" + i,
                            "Active"
                    );
                    addCoach(coach);
                    coaches.add(coach);
                }
            }

            // Générer des drivers si aucun n'existe
            if (drivers.isEmpty()) {
                String[] driverNames = {"James Wilson", "Robert Brown", "Michael Johnson"};
                for (int i = 0; i < 3; i++) {
                    String username = "driver" + (i + 1);

                    // Créer le compte utilisateur
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("username", username);
                    userData.put("password", "1234");
                    userData.put("fullname", driverNames[i]);
                    userData.put("email", driverNames[i].toLowerCase().replace(" ", ".") + "@translink.com");
                    userData.put("phone", "+123456789" + i);
                    userData.put("role", "driver");
                    db.collection("users").document(username).set(userData);

                    // Créer le profil driver
                    Driver driver = new Driver(
                            "D00" + (i + 1),
                            driverNames[i],
                            "LIC-" + (1000 + i),
                            "L001",
                            "+123456789" + i,
                            driverNames[i].toLowerCase().replace(" ", ".") + "@translink.com",
                            "active",
                            5000.0 + (i * 1000),
                            "C" + (800 + i),
                            username
                    );
                    addDriver(driver);
                    drivers.add(driver);
                }
            }
        }

        // Générer 10 positions de véhicule
        for (int i = 0; i < 10; i++) {
            Map<String, Object> position = new HashMap<>();

            Coach coach = coaches.get(random.nextInt(coaches.size()));
            Driver driver = drivers.get(random.nextInt(drivers.size()));

            // Coordonnées autour d'un point central (simulation Paris)
            double baseLat = 48.8566;
            double baseLng = 2.3522;

            position.put("coachId", coach.getId());
            position.put("driverId", driver.getDriverId());
            position.put("latitude", baseLat + (random.nextDouble() * 0.1 - 0.05));
            position.put("longitude", baseLng + (random.nextDouble() * 0.1 - 0.05));
            position.put("speed", 30 + random.nextDouble() * 60);
            position.put("heading", String.valueOf(random.nextInt(360)));
            position.put("timestamp", FieldValue.serverTimestamp());
            position.put("status", random.nextDouble() > 0.3 ? "moving" : "stopped");

            db.collection("vehiclePositions").document("POS_" + System.currentTimeMillis() + "_" + i).set(position);
        }
        System.out.println("✅ 10 positions de véhicule générées");

        // 2. Générer des activités utilisateur
        System.out.println("👤 Génération des activités utilisateur...");

        // Récupérer tous les utilisateurs
        ObservableList<Map<String, Object>> users = getAllUsersWithDetails();
        String[] activities = {"login", "logout", "view_dashboard", "report_incident",
                "update_profile", "check_schedule", "view_reports", "send_message"};

        // Générer 20 activités
        for (int i = 0; i < 20; i++) {
            Map<String, Object> user = users.get(random.nextInt(users.size()));
            Map<String, Object> activity = new HashMap<>();

            String username = (String) user.get("username");
            String role = (String) user.get("role");

            activity.put("userId", username);
            activity.put("activityType", activities[random.nextInt(activities.length)]);
            activity.put("details", role + " " + username + " performed activity");
            activity.put("timestamp", FieldValue.serverTimestamp());
            activity.put("location", "IP: 192.168.1." + (random.nextInt(254) + 1));
            activity.put("sessionId", "SESSION_" + (1000 + i));

            db.collection("userActivities").document("ACT_" + System.currentTimeMillis() + "_" + i).set(activity);
        }
        System.out.println("✅ 20 activités utilisateur générées");

        // 3. Générer des métriques système
        System.out.println("⚙️ Génération des métriques système...");

        String[] metricTypes = {"cpu", "memory", "network", "database", "disk", "api_response"};
        String[] servers = {"server1", "server2", "firestore", "api_gateway"};

        // Générer 50 métriques sur les dernières 24 heures
        for (int i = 0; i < 50; i++) {
            Map<String, Object> metric = new HashMap<>();

            String type = metricTypes[random.nextInt(metricTypes.length)];

            metric.put("type", type);

            // Valeurs réalistes selon le type
            switch (type) {
                case "cpu":
                    metric.put("value", 20 + random.nextDouble() * 50); // 20-70%
                    metric.put("unit", "%");
                    break;
                case "memory":
                    metric.put("value", 40 + random.nextDouble() * 40); // 40-80%
                    metric.put("unit", "%");
                    break;
                case "network":
                    metric.put("value", 50 + random.nextDouble() * 150); // 50-200 Mbps
                    metric.put("unit", "Mbps");
                    break;
                case "database":
                    metric.put("value", 5 + random.nextDouble() * 15); // 5-20 ms
                    metric.put("unit", "ms");
                    break;
                case "disk":
                    metric.put("value", 30 + random.nextDouble() * 50); // 30-80%
                    metric.put("unit", "%");
                    break;
                case "api_response":
                    metric.put("value", 50 + random.nextDouble() * 150); // 50-200 ms
                    metric.put("unit", "ms");
                    break;
            }

            // Timestamp aléatoire dans les dernières 24 heures
            long offset = random.nextInt(24 * 60 * 60 * 1000); // 0-24 heures en millisecondes
            Date timestamp = new Date(System.currentTimeMillis() - offset);
            metric.put("timestamp", timestamp);

            metric.put("source", servers[random.nextInt(servers.length)]);

            db.collection("systemMetrics").document("METRIC_" + System.currentTimeMillis() + "_" + i).set(metric);
        }
        System.out.println("✅ 50 métriques système générées");

        // 4. Générer des événements analytiques
        System.out.println("📊 Génération des événements analytiques...");

        String[] categories = {"performance", "usage", "security", "business", "user_behavior"};
        String[] actions = {"page_view", "button_click", "form_submit", "search", "download", "export"};
        String[] labels = {"Admin Dashboard", "Incident Report", "User Management",
                "Analytics Center", "Real-time Monitor", "System Control"};

        // Générer 30 événements
        for (int i = 0; i < 30; i++) {
            Map<String, Object> event = new HashMap<>();

            Map<String, Object> user = users.get(random.nextInt(users.size()));

            event.put("category", categories[random.nextInt(categories.length)]);
            event.put("action", actions[random.nextInt(actions.length)]);
            event.put("label", labels[random.nextInt(labels.length)]);
            event.put("value", random.nextInt(100) + 1);
            event.put("userId", user.get("username"));
            event.put("timestamp", FieldValue.serverTimestamp());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("browser", random.nextBoolean() ? "Chrome" : "Firefox");
            metadata.put("os", random.nextBoolean() ? "Windows" : "macOS");
            metadata.put("screen_resolution", "1920x1080");

            event.put("metadata", metadata);

            db.collection("analyticsEvents").document("EVENT_" + System.currentTimeMillis() + "_" + i).set(event);
        }
        System.out.println("✅ 30 événements analytiques générés");

        System.out.println("🎉 Données de monitoring générées avec succès !");
        System.out.println("📋 Résumé:");
        System.out.println("   • vehiclePositions: 10 documents");
        System.out.println("   • userActivities: 20 documents");
        System.out.println("   • systemMetrics: 50 documents");
        System.out.println("   • analyticsEvents: 30 documents");
    }

    /**
     * Vérifie si les collections de monitoring existent et sont vides
     */
    public static boolean needMonitoringData() throws Exception {
        System.out.println("ℹ️ Monitoring data check disabled");
        return false;
    }
    // ==============================
// ANALYTICS REPORT METHODS
// ==============================

    private static Map<String, Object> getWeeklyReport() throws Exception {
        Map<String, Object> weekly = new HashMap<>();

        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);

        weekly.put("period", "Weekly Report");
        weekly.put("startDate", startOfWeek.toString());
        weekly.put("endDate", now.toString());
        weekly.put("totalUsers", getTotalUsersCount());
        weekly.put("weeklyActiveUsers", getWeeklyActiveUsersCount(startOfWeek));
        weekly.put("incidentsReported", getIncidentsCount(startOfWeek, now));
        weekly.put("incidentsResolved", getResolvedIncidentsCount(startOfWeek));
        weekly.put("averageResponseTime", getAverageResponseTime(startOfWeek, now));
        weekly.put("peakActivityDay", getPeakActivityDay(startOfWeek, now));
        weekly.put("driverPerformance", getDriverPerformance(startOfWeek, now));

        return weekly;
    }

    private static Map<String, Object> getMonthlyReport() throws Exception {
        Map<String, Object> monthly = new HashMap<>();

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);

        monthly.put("period", "Monthly Report");
        monthly.put("month", now.getMonth().toString());
        monthly.put("year", now.getYear());
        monthly.put("totalUsers", getTotalUsersCount());
        monthly.put("monthlyActiveUsers", getMonthlyActiveUsersCount(startOfMonth));
        monthly.put("newUsers", getNewUsersCount(startOfMonth));
        monthly.put("incidentsReported", getIncidentsCount(startOfMonth, now));
        monthly.put("incidentsResolved", getResolvedIncidentsCount(startOfMonth));
        monthly.put("resolutionRate", calculateResolutionRate(startOfMonth, now));
        monthly.put("revenueEstimate", calculateMonthlyRevenue(startOfMonth, now));
        monthly.put("topPerformingDriver", getTopPerformingDriver(startOfMonth, now));
        monthly.put("mostCommonIncidentType", getMostCommonIncidentType(startOfMonth, now));

        return monthly;
    }

// ==============================
// HELPER METHODS FOR ANALYTICS
// ==============================

    public static long getTotalUsersCount() throws Exception {
        return db.collection("users").count().get().get().getCount();
    }

    private static long getWeeklyActiveUsersCount(LocalDate startOfWeek) throws Exception {
        try {
            // SIMPLIFIER la requête pour éviter l'index composite
            Timestamp startTimestamp = Timestamp.of(Date.from(
                    startOfWeek.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
            Timestamp endTimestamp = Timestamp.of(Date.from(
                    LocalDate.now().plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));

            // Option 1: Compter tous les utilisateurs qui ont une activité (sans filtre activityType)
            QuerySnapshot activities = db.collection("userActivities")
                    .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
                    .whereLessThan("timestamp", endTimestamp)
                    .get()
                    .get();

            // Option 2: Si la collection userActivities n'existe pas ou est vide
            if (activities.isEmpty()) {
                System.out.println("ℹ️ No userActivities found, estimating active users...");
                // Retourner une estimation basée sur les users existants
                long totalUsers = db.collection("users").count().get().get().getCount();
                return totalUsers / 3; // Estimation: 1/3 des users sont actifs
            }

            return activities.getDocuments().stream()
                    .map(doc -> doc.getString("userId"))
                    .filter(userId -> userId != null && !userId.isEmpty())
                    .distinct()
                    .count();

        } catch (Exception e) {
            System.err.println("⚠️ Error in getWeeklyActiveUsersCount, using fallback: " + e.getMessage());
            // Fallback: compter tous les users
            long totalUsers = db.collection("users").count().get().get().getCount();
            return Math.max(1, totalUsers / 4); // Estimation sécurisée
        }
    }

    private static long getMonthlyActiveUsersCount(LocalDate startOfMonth) throws Exception {
        Timestamp startTimestamp = Timestamp.of(Date.from(
                startOfMonth.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        Timestamp endTimestamp = Timestamp.of(Date.from(
                LocalDate.now().plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));

        QuerySnapshot activities = db.collection("userActivities")
                .whereEqualTo("activityType", "login")
                .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
                .whereLessThan("timestamp", endTimestamp)
                .get()
                .get();

        return activities.getDocuments().stream()
                .map(doc -> doc.getString("userId"))
                .distinct()
                .count();
    }

    private static long getNewUsersCount(LocalDate startOfMonth) throws Exception {
        Timestamp startTimestamp = Timestamp.of(Date.from(
                startOfMonth.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));

        QuerySnapshot newUsers = db.collection("users")
                .whereGreaterThanOrEqualTo("createdAt", startTimestamp)
                .get()
                .get();

        return newUsers.size();
    }

    private static long getIncidentsCount(LocalDate startDate, LocalDate endDate) throws Exception {
        Timestamp startTimestamp = Timestamp.of(Date.from(
                startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        Timestamp endTimestamp = Timestamp.of(Date.from(
                endDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));

        return db.collection("incidents")
                .whereGreaterThanOrEqualTo("reportedDate", startTimestamp)
                .whereLessThan("reportedDate", endTimestamp)
                .count()
                .get()
                .get()
                .getCount();
    }

    private static long getResolvedIncidentsCount(LocalDate date) throws Exception {
        Timestamp start = Timestamp.of(Date.from(
                date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        Timestamp end = Timestamp.of(Date.from(
                date.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));

        QuerySnapshot resolvedIncidents = db.collection("incidents")
                .whereEqualTo("status", "resolved")
                .whereGreaterThanOrEqualTo("reportedDate", start)
                .whereLessThan("reportedDate", end)
                .get()
                .get();

        return resolvedIncidents.size();
    }

    private static double calculateResolutionRate(LocalDate startDate, LocalDate endDate) throws Exception {
        long totalIncidents = getIncidentsCount(startDate, endDate);
        long resolvedIncidents = getResolvedIncidentsCount(startDate);

        if (totalIncidents == 0) return 0.0;

        return (double) resolvedIncidents / totalIncidents * 100;
    }

    private static double getAverageResponseTime(LocalDate startDate, LocalDate endDate) throws Exception {
        // Pour l'instant, retourner une valeur simulée
        // En production, vous calculeriez le temps moyen de réponse aux incidents
        Random random = new Random();
        return 120 + random.nextDouble() * 60; // 120-180 minutes
    }

    private static String getPeakActivityDay(LocalDate startDate, LocalDate endDate) throws Exception {
        // Simuler le jour de pointe
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        Random random = new Random();
        return days[random.nextInt(days.length)];
    }

    private static Map<String, Object> getDriverPerformance(LocalDate startDate, LocalDate endDate) throws Exception {
        Map<String, Object> performance = new HashMap<>();

        // Simuler les performances des drivers
        performance.put("averageOnTime", 85 + new Random().nextInt(15)); // 85-99%
        performance.put("bestDriver", "James Wilson");
        performance.put("bestDriverScore", 98);
        performance.put("tripsCompleted", 245);
        performance.put("averageRating", 4.7);

        return performance;
    }

    private static double calculateMonthlyRevenue(LocalDate startDate, LocalDate endDate) throws Exception {
        // Simuler le revenu mensuel basé sur le nombre de drivers et jours
        long activeDrivers = db.collection("drivers")
                .whereEqualTo("status", "active")
                .count()
                .get()
                .get()
                .getCount();

        long daysInPeriod = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // Estimation: chaque driver génère $200 par jour
        return activeDrivers * daysInPeriod * 200.0;
    }

    private static Map<String, Object> getTopPerformingDriver(LocalDate startDate, LocalDate endDate) throws Exception {
        Map<String, Object> topDriver = new HashMap<>();

        // Récupérer tous les drivers
        QuerySnapshot driversSnapshot = db.collection("drivers").get().get();

        if (!driversSnapshot.isEmpty()) {
            DocumentSnapshot driverDoc = driversSnapshot.getDocuments().get(0);
            topDriver.put("name", driverDoc.getString("fullName"));
            topDriver.put("driverId", driverDoc.getString("driverId"));
            topDriver.put("incidentsResolved", new Random().nextInt(10) + 5);
            topDriver.put("onTimePercentage", 95 + new Random().nextInt(5));
            topDriver.put("customerRating", 4.8 + new Random().nextDouble() * 0.2);
        } else {
            topDriver.put("name", "No data available");
            topDriver.put("driverId", "N/A");
            topDriver.put("incidentsResolved", 0);
            topDriver.put("onTimePercentage", 0);
            topDriver.put("customerRating", 0);
        }

        return topDriver;
    }

    private static String getMostCommonIncidentType(LocalDate startDate, LocalDate endDate) throws Exception {
        Timestamp startTimestamp = Timestamp.of(Date.from(
                startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        Timestamp endTimestamp = Timestamp.of(Date.from(
                endDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));

        QuerySnapshot incidents = db.collection("incidents")
                .whereGreaterThanOrEqualTo("reportedDate", startTimestamp)
                .whereLessThan("reportedDate", endTimestamp)
                .get()
                .get();

        if (incidents.isEmpty()) {
            return "No incidents reported";
        }

        // Compter les types d'incidents
        Map<String, Integer> typeCount = new HashMap<>();
        for (DocumentSnapshot doc : incidents.getDocuments()) {
            String type = doc.getString("type");
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
        }

        // Trouver le type le plus commun
        return typeCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    /**
     * Méthode pour charger les données analytiques depuis Firestore
     */
    public static Map<String, Object> loadAnalyticsData(String period) throws Exception {
        Map<String, Object> analytics = new HashMap<>();

        switch (period.toLowerCase()) {
            case "daily":
                analytics = getDailyReport();
                break;
            case "weekly":
                analytics = getWeeklyReport();
                break;
            case "monthly":
                analytics = getMonthlyReport();
                break;
            default:
                analytics = getWeeklyReport();
        }

        return analytics;
    }
    /**
     * Récupère toutes les activités utilisateur
     */
    public static ObservableList<Map<String, Object>> getAllUserActivities() throws Exception {
        ObservableList<Map<String, Object>> activities = FXCollections.observableArrayList();
        QuerySnapshot snapshot = db.collection("userActivities")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .get();

        for (QueryDocumentSnapshot doc : snapshot) {
            Map<String, Object> activity = new HashMap<>(doc.getData());
            activity.put("id", doc.getId());
            activities.add(activity);
        }

        return activities;
    }

    /**
     * Récupère les métriques de performance
     */
    public static Map<String, Object> getPerformanceMetrics() throws Exception {
        Map<String, Object> metrics = new HashMap<>();

        // Performance mensuelle (simulée pour l'instant)
        List<Map<String, Object>> monthlyPerformance = new ArrayList<>();

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul"};
        Random random = new Random();

        for (String month : months) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month);
            monthData.put("score", 70 + random.nextInt(30));
            monthlyPerformance.add(monthData);
        }

        metrics.put("monthlyPerformance", monthlyPerformance);

        return metrics;
    }

    /**
     * Récupère les statistiques détaillées pour l'analytics
     */
    public static Map<String, Object> getAnalyticsStats() throws Exception {
        Map<String, Object> stats = new HashMap<>();

        // Récupérer les stats de base
        stats.putAll(getDashboardStats());

        // Ajouter des stats supplémentaires
        stats.put("userStatistics", getUserStatistics());

        // Calculer le taux de résolution d'incidents
        long totalIncidents = (Long) stats.getOrDefault("totalIncidents", 0L);
        long pendingIncidents = (Long) stats.getOrDefault("pendingIncidents", 0L);

        if (totalIncidents > 0) {
            double resolutionRate = ((double) (totalIncidents - pendingIncidents) / totalIncidents) * 100;
            stats.put("incidentResolutionRate", Math.round(resolutionRate * 10.0) / 10.0);
        }

        return stats;
    }

    public static void logManagerAction(String username, String action, String details) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("userId", username);
            logData.put("action", "manager_" + action);
            logData.put("details", details);
            logData.put("timestamp", FieldValue.serverTimestamp());
            logData.put("ipAddress", "system");
            logData.put("severity", "INFO");

            db.collection("managerLogs").document().set(logData);
            System.out.println("📝 Manager action logged: " + username + " - " + action);
        } catch (Exception e) {
            System.err.println("❌ Error logging manager action: " + e.getMessage());
        }
    }

    public static void listenToManagerChanges(Consumer<Map<String, Object>> onChange) {
        try {
            Firestore db = FirestoreClient.getFirestore();

            db.collection("users")
                    .whereEqualTo("role", "manager")
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null) {
                            System.err.println("❌ Listen failed: " + error);
                            return;
                        }

                        if (snapshots != null && !snapshots.isEmpty()) {
                            for (DocumentChange change : snapshots.getDocumentChanges()) {
                                DocumentSnapshot doc = change.getDocument();
                                Map<String, Object> data = doc.getData();
                                data.put("changeType", change.getType().toString()); // ADDED, MODIFIED, REMOVED
                                data.put("userId", doc.getId());

                                // Notifier du changement
                                onChange.accept(data);

                                // Loguer l'action du manager
                                if (change.getType() == DocumentChange.Type.MODIFIED) {
                                    logManagerAction(data.get("username").toString(),
                                            "updated_data",
                                            "Manager made changes");
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Écouter les assignations de lignes aux drivers
    public static void listenToDriverAssignments(Consumer<Map<String, Object>> onChange) {
        try {
            Firestore db = FirestoreClient.getFirestore();

            db.collection("drivers")
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null) {
                            System.err.println("❌ Driver assignments listen failed: " + error);
                            return;
                        }

                        if (snapshots != null && !snapshots.isEmpty()) {
                            for (DocumentChange change : snapshots.getDocumentChanges()) {
                                if (change.getType() == DocumentChange.Type.MODIFIED) {
                                    DocumentSnapshot doc = change.getDocument();
                                    Map<String, Object> data = doc.getData();

                                    // Vérifier si une ligne a été assignée
                                    if (data.containsKey("assignedLine") &&
                                            !data.get("assignedLine").toString().isEmpty()) {

                                        Map<String, Object> assignment = new HashMap<>();
                                        assignment.put("driverId", doc.getId());
                                        assignment.put("driverName", data.get("name"));
                                        assignment.put("assignedLine", data.get("assignedLine"));
                                        assignment.put("assignedBy", "manager");
                                        assignment.put("timestamp", new Date());

                                        onChange.accept(assignment);

                                        // Notifier l'admin
                                        logSecurityEvent("SYSTEM",
                                                "line_assigned",
                                                "Manager assigned line " + data.get("assignedLine") +
                                                        " to driver " + data.get("name"),
                                                "127.0.0.1",
                                                "INFO");
                                    }
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Object> getLoginAttempts(String username) {
        try {
            // Dernières 24 heures
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, -24);
            Date yesterday = calendar.getTime();

            // Tentatives échouées
            Query failedLoginsQuery = db.collection("securityLogs")
                    .whereEqualTo("userId", username)
                    .whereEqualTo("action", "login_failed")
                    .whereGreaterThan("timestamp", yesterday);

            long failedAttempts = failedLoginsQuery.get().get().size();

            // Dernière connexion réussie
            Query successfulLoginsQuery = db.collection("securityLogs")
                    .whereEqualTo("userId", username)
                    .whereEqualTo("action", "login_success")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1);

            String lastLogin = "Never";
            QuerySnapshot successSnap = successfulLoginsQuery.get().get();
            if (!successSnap.isEmpty()) {
                Timestamp lastTimestamp = successSnap.getDocuments().get(0).getTimestamp("timestamp");
                lastLogin = formatTimestamp(lastTimestamp);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("failedAttempts24h", failedAttempts);
            result.put("lastSuccessfulLogin", lastLogin);
            result.put("username", username);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("failedAttempts24h", 0L);
            result.put("lastSuccessfulLogin", "Error");
            return result;
        }
    }

    public static void updateDriverAssignment(String driverId, String lineId, String assignedBy) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("assignedLine", lineId);
            updates.put("lastAssignment", FieldValue.serverTimestamp());
            updates.put("assignedBy", assignedBy);

            db.collection("drivers").document(driverId).update(updates);

            // Log de l'assignation
            Map<String, Object> assignmentLog = new HashMap<>();
            assignmentLog.put("driverId", driverId);
            assignmentLog.put("lineId", lineId);
            assignmentLog.put("assignedBy", assignedBy);
            assignmentLog.put("timestamp", FieldValue.serverTimestamp());

            db.collection("assignments").document().set(assignmentLog);

            System.out.println("✅ Driver assignment updated: " + driverId + " -> " + lineId);

        } catch (Exception e) {
            System.err.println("❌ Error updating driver assignment: " + e.getMessage());
            throw e;
        }
    }

    // Écouter les logins en temps réel
    public static void listenToUserLogins(Consumer<Map<String, Object>> onLogin) {
        try {
            Firestore db = FirestoreClient.getFirestore();

            db.collection("securityLogs")
                    .whereEqualTo("action", "login_success")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(10)
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null) {
                            System.err.println("❌ Login listener failed: " + error);
                            return;
                        }

                        if (snapshots != null && !snapshots.isEmpty()) {
                            for (DocumentChange change : snapshots.getDocumentChanges()) {
                                if (change.getType() == DocumentChange.Type.ADDED) {
                                    DocumentSnapshot doc = change.getDocument();
                                    Map<String, Object> data = doc.getData();

                                    // Créer une notification de login
                                    Map<String, Object> loginInfo = new HashMap<>();
                                    loginInfo.put("userId", data.get("userId"));
                                    loginInfo.put("timestamp", formatTimestamp(doc.getTimestamp("timestamp")));
                                    loginInfo.put("ipAddress", data.get("ipAddress"));
                                    loginInfo.put("role", getUserRole(data.get("userId").toString()));

                                    onLogin.accept(loginInfo);

                                    System.out.println("🔔 User logged in: " + data.get("userId"));
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getUserRole(String username) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentSnapshot userDoc = db.collection("users")
                    .whereEqualTo("username", username)
                    .limit(1)
                    .get()
                    .get()
                    .getDocuments()
                    .get(0);

            return userDoc.getString("role");
        } catch (Exception e) {
            return "unknown";
        }
    }

    // Dans FirebaseService.java
    public static ObservableList<SystemLog> getRealSystemLogs() throws Exception {
        ObservableList<SystemLog> logs = FXCollections.observableArrayList();

        try {
            // Charger depuis la collection systemLogs si elle existe
            QuerySnapshot snapshot = db.collection("systemLogs")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .get();

            if (!snapshot.isEmpty()) {
                for (QueryDocumentSnapshot doc : snapshot) {
                    Timestamp timestamp = doc.getTimestamp("timestamp");
                    String timeStr = timestamp != null ?
                            formatTimestamp(timestamp) :
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    SystemLog log = new SystemLog(
                            timeStr,
                            doc.getString("level"),
                            doc.getString("source"),
                            doc.getString("message"),
                            doc.getString("user")
                    );
                    logs.add(log);
                }
            } else {
                // Si pas de logs système, créer des logs à partir des incidents
                QuerySnapshot incidents = db.collection("incidents")
                        .orderBy("reportedDate", Query.Direction.DESCENDING)
                        .limit(10)
                        .get()
                        .get();

                for (QueryDocumentSnapshot doc : incidents) {
                    Timestamp timestamp = doc.getTimestamp("reportedDate");
                    String timeStr = timestamp != null ?
                            timestamp.toDate().toString() :
                            doc.getString("date");

                    SystemLog log = new SystemControlView.SystemLog(
                            timeStr,
                            "WARNING",
                            "Incident System",
                            doc.getString("type") + ": " + doc.getString("description"),
                            doc.getString("driverId") != null ? doc.getString("driverId") : "system"
                    );
                    logs.add(log);
                }
            }

            System.out.println("✅ " + logs.size() + " real system logs loaded");
            return logs;

        } catch (Exception e) {
            System.err.println("❌ Error loading system logs: " + e.getMessage());
            return logs;
        }
    }

}


