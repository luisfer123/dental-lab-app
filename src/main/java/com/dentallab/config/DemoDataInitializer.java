package com.dentallab.config;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.api.enums.BuildingTechnique;
import com.dentallab.api.model.BridgeWorkModel;
import com.dentallab.api.model.CrownWorkModel;
import com.dentallab.api.model.FullWorkModel;
import com.dentallab.api.model.WorkModel;
import com.dentallab.domain.enums.FixProstheticConstitution;
import com.dentallab.persistence.entity.ClientEntity;
import com.dentallab.persistence.entity.DentistProfileEntity;
import com.dentallab.persistence.entity.MaterialEntity;
import com.dentallab.persistence.entity.RoleEntity;
import com.dentallab.persistence.entity.StudentProfileEntity;
import com.dentallab.persistence.entity.TechnicianProfileEntity;
import com.dentallab.persistence.entity.UserAccountEntity;
import com.dentallab.persistence.entity.WorkerEntity;
import com.dentallab.persistence.repository.ClientRepository;
import com.dentallab.persistence.repository.DentistProfileRepository;
import com.dentallab.persistence.repository.MaterialRepository;
import com.dentallab.persistence.repository.RoleRepository;
import com.dentallab.persistence.repository.StudentProfileRepository;
import com.dentallab.persistence.repository.TechnicianProfileRepository;
import com.dentallab.persistence.repository.UserAccountRepository;
import com.dentallab.persistence.repository.WorkOrderRepository;
import com.dentallab.persistence.repository.WorkerRepository;
import com.dentallab.service.WorkService;

@Configuration
@Profile("!test")
public class DemoDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
    
    private static final List<String> workStatusList = List.of(
    		"RECEIVED",
    		"ASSIGNED",
    		"IN_PROGRESS",
    		"FINISHED",
    		"DELIVERING",
    		"DELIVERED"
    );

    private final RoleRepository roleRepository;
    private final UserAccountRepository userRepository;
    private final WorkerRepository workerRepository;

    private final ClientRepository clientRepository;
    private final DentistProfileRepository dentistRepo;
    private final StudentProfileRepository studentRepo;
    private final TechnicianProfileRepository techProfileRepo;

    private final WorkService workService;
    private final PasswordEncoder passwordEncoder;

    private final WorkOrderRepository orderRepository;
    private final MaterialRepository materialRepository;

    public DemoDataInitializer(
            RoleRepository roleRepository,
            UserAccountRepository userRepository,
            WorkerRepository workerRepository,
            ClientRepository clientRepository,
            DentistProfileRepository dentistRepo,
            StudentProfileRepository studentRepo,
            TechnicianProfileRepository techProfileRepo,
            WorkService workService,
            PasswordEncoder passwordEncoder,
            WorkOrderRepository orderRepository,
            MaterialRepository materialRepository
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.workerRepository = workerRepository;
        this.clientRepository = clientRepository;
        this.dentistRepo = dentistRepo;
        this.studentRepo = studentRepo;
        this.techProfileRepo = techProfileRepo;
        this.workService = workService;
        this.passwordEncoder = passwordEncoder;
        this.orderRepository = orderRepository;
        this.materialRepository = materialRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {

        log.info("=== DEMO DATA INITIALIZER START ===");

        if (clientRepository.count() > 0) {
            log.info("Database has data — skipping demo creation.");
            return;
        }

        insertRoles();
        UserAccountEntity admin = insertAdmin();
        createAdminTechnician(admin);

        List<ClientEntity> dentists = createDentists(15);
        List<ClientEntity> students = createStudents(15);
        createTechnicians(9);

        createTestMaterials();   // MUST be before creating works

        createCrownWorks(dentists, 15);
        createBridgeWorks(students, 15);

        log.info("=== DEMO DATA INITIALIZER COMPLETED ===");
    }

    // -------------------------------------------------------------
    // ROLES
    // -------------------------------------------------------------
    private void insertRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.save(new RoleEntity("ADMIN"));
            roleRepository.save(new RoleEntity("TECHNICIAN"));
            roleRepository.save(new RoleEntity("CLIENT"));
            log.info("Inserted default roles");
        }
    }

    // -------------------------------------------------------------
    // ADMIN USER
    // -------------------------------------------------------------
    private UserAccountEntity insertAdmin() {
        return userRepository.findByUsername("admin")
                .orElseGet(() -> {
                    UserAccountEntity admin = new UserAccountEntity();
                    admin.setUsername("admin");
                    admin.setEmail("admin@demo.com");
                    admin.setPasswordHash(passwordEncoder.encode("password"));
                    admin.setEnabled(true);

                    admin = userRepository.save(admin);
                    roleRepository.assignRole(admin.getId(), "ADMIN");

                    log.info("Inserted admin user");
                    return admin;
                });
    }

    // -------------------------------------------------------------
    // ADMIN TECHNICIAN PROFILE
    // -------------------------------------------------------------
    private void createAdminTechnician(UserAccountEntity admin) {

        if (workerRepository.findByUser_Id(admin.getId()).isPresent())
            return;

        WorkerEntity w = new WorkerEntity();
        w.setUser(admin);
        w.setFirstName("Admin");
        w.setLastName("User");
        w.setDisplayName("Admin User");
        workerRepository.save(w);

        TechnicianProfileEntity tp = new TechnicianProfileEntity();
        tp.setWorker(w);
        tp.setLabName("Demo Lab Admin");
        tp.setSpecialization("Management");
        techProfileRepo.save(tp);

        roleRepository.assignRole(admin.getId(), "TECHNICIAN");

        log.info("Created technician profile for admin");
    }

    // -------------------------------------------------------------
    // CREATE CLIENTS → DENTISTS
    // -------------------------------------------------------------
    private List<ClientEntity> createDentists(int count) {
        log.info("Creating {} dentists…", count);

        for (int i = 1; i <= count; i++) {

            ClientEntity c = new ClientEntity();
            c.setFirstName("Dentist" + i);
            c.setLastName("Demo");
            c.setDisplayName("Dentist " + i);
            clientRepository.save(c);

            DentistProfileEntity dp = new DentistProfileEntity();
            dp.setClient(c);
            dp.setClinicName("Dental Clinic " + i);

            c.setDentistProfile(dp);
            dentistRepo.save(dp);
        }

        return dentistRepo.findAll()
                .stream().map(DentistProfileEntity::getClient).toList();
    }

    // -------------------------------------------------------------
    // CREATE CLIENTS → STUDENTS
    // -------------------------------------------------------------
    private List<ClientEntity> createStudents(int count) {
        log.info("Creating {} students…", count);

        for (int i = 1; i <= count; i++) {

            ClientEntity c = new ClientEntity();
            c.setFirstName("Student" + i);
            c.setLastName("Demo");
            c.setDisplayName("Student " + i);
            clientRepository.save(c);

            StudentProfileEntity sp = new StudentProfileEntity();
            sp.setClient(c);
            sp.setSemester(5);
            sp.setUniversityName("Dental University");

            c.setStudentProfile(sp);
            studentRepo.save(sp);
        }

        return studentRepo.findAll()
                .stream().map(StudentProfileEntity::getClient).toList();
    }

    // -------------------------------------------------------------
    // DEMO TECHNICIAN WORKERS
    // -------------------------------------------------------------
    private void createTechnicians(int count) {
        log.info("Creating {} demo technicians…", count);

        for (int i = 1; i <= count; i++) {

            UserAccountEntity ua = new UserAccountEntity();
            ua.setUsername("tech" + i);
            ua.setEmail("tech" + i + "@demo.com");
            ua.setPasswordHash(passwordEncoder.encode("tech123"));
            userRepository.save(ua);

            roleRepository.assignRole(ua.getId(), "TECHNICIAN");

            WorkerEntity worker = new WorkerEntity();
            worker.setUser(ua);
            worker.setFirstName("Tech" + i);
            worker.setLastName("Demo");
            worker.setDisplayName("Tech " + i + " Demo");
            workerRepository.save(worker);

            TechnicianProfileEntity tp = new TechnicianProfileEntity();
            tp.setWorker(worker);
            tp.setLabName("TechLab " + i);
            tp.setSpecialization("Crown/Bridge");
            techProfileRepo.save(tp);
        }
    }

    // -------------------------------------------------------------
    // MATERIALS
    // -------------------------------------------------------------
    private void createTestMaterials() {

        if (materialRepository.count() == 0) {
            materialRepository.save(new MaterialEntity("Zirconia", "CERAMIC", "g", 12.0));
            materialRepository.save(new MaterialEntity("e.max", "CERAMIC", "g", 15.0));
            materialRepository.save(new MaterialEntity("Resin", "TEMP", "g", 5.0));

            log.info("Inserted demo materials");
        }
    }

    // -------------------------------------------------------------
    // DEMO CROWN WORKS
    // -------------------------------------------------------------
    private void createCrownWorks(List<ClientEntity> clients, int count) {

        List<MaterialEntity> mats = materialRepository.findAll();
        Random rnd = new Random();

        log.info("Creating {} crown works…", count);

        for (int i = 1; i <= count; i++) {

            ClientEntity c = clients.get(rnd.nextInt(clients.size()));

            WorkModel base = new WorkModel();
            base.setClientId(c.getId());
            base.setWorkFamily("FIXED_PROSTHESIS");
            base.setType("CROWN");
            base.setDescription("Demo Crown " + i);
            base.setShade("A2");
            base.setStatus(workStatusList.get(rnd.nextInt(workStatusList.size())));

            CrownWorkModel ext = new CrownWorkModel();
            ext.setType("CROWN");
            ext.setConstitution(FixProstheticConstitution.MONOLITHIC);
            ext.setBuildingTechnique(BuildingTechnique.DIGITAL);
            ext.setCoreMaterialId(mats.get(rnd.nextInt(mats.size())).getId());
            ext.setToothNumber(String.valueOf(11 + rnd.nextInt(20))); // valid enough for demo

            FullWorkModel payload = new FullWorkModel();
            payload.setBase(base);
            payload.setExtension(ext);

            workService.create(payload);
        }
    }

    // -------------------------------------------------------------
    // DEMO BRIDGE WORKS
    // -------------------------------------------------------------
    private void createBridgeWorks(List<ClientEntity> clients, int count) {

        List<MaterialEntity> mats = materialRepository.findAll();
        Random rnd = new Random();

        log.info("Creating {} bridge works…", count);

        for (int i = 1; i <= count; i++) {

            ClientEntity c = clients.get(rnd.nextInt(clients.size()));

            WorkModel base = new WorkModel();
            base.setClientId(c.getId());
            base.setWorkFamily("FIXED_PROSTHESIS");
            base.setType("BRIDGE");
            base.setDescription("Demo Bridge " + i);
            base.setShade("A2");

            BridgeWorkModel ext = new BridgeWorkModel();
            ext.setType("BRIDGE");
            ext.setBuildingTechnique(BuildingTechnique.DIGITAL);

            // Randomly choose MONOLITHIC or BILAYERED
            boolean monolithic = rnd.nextBoolean();

            if (monolithic) {
                ext.setConstitution(FixProstheticConstitution.MONOLITHIC);
                ext.setCoreMaterialId(mats.get(rnd.nextInt(mats.size())).getId());
                ext.setVeneeringMaterialId(null);
            } else {
            	ext.setConstitution(FixProstheticConstitution.STRATIFIED);
                ext.setCoreMaterialId(mats.get(rnd.nextInt(mats.size())).getId());
                ext.setVeneeringMaterialId(mats.get(rnd.nextInt(mats.size())).getId());
            }

            FullWorkModel payload = new FullWorkModel();
            payload.setBase(base);
            payload.setExtension(ext);

            workService.create(payload);
        }
    }

}
