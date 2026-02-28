package com.example.HrAssistance.seeders;

import com.example.HrAssistance.enums.MessageRole;
import com.example.HrAssistance.enums.Role;
import com.example.HrAssistance.model.*;
import com.example.HrAssistance.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepo userRepo;
    private final CandidateRepo candidateRepo;
    private final JobDescriptionRepo jobDescriptionRepo;
    private final MatchResultRepo matchResultRepo;
    private final ChatMessageRepo chatMessageRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // Only seed if DB is empty
        if (userRepo.count() > 0) {
            log.info("✅ Database already seeded, skipping...");
            return;
        }

        log.info("🌱 Seeding database...");

        seedUsers();
        seedCandidates();
        seedJobDescriptions();
        seedMatchResults();
        seedChatMessages();

        log.info("✅ Database seeding complete!");
    }

    // ─────────────────────────────────────────
    // USERS
    // ─────────────────────────────────────────
    private void seedUsers() {
        // Admin
        User admin = User.builder()
                .username("Admin User")
                .email("admin@hrapp.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build();

        // HR Staff 1
        User hr1 = User.builder()
                .username("Sokha Chan")
                .email("sokha@hrapp.com")
                .password(passwordEncoder.encode("hr123"))
                .role(Role.HR)
                .build();

        // HR Staff 2
        User hr2 = User.builder()
                .username("Dara Pich")
                .email("dara@hrapp.com")
                .password(passwordEncoder.encode("hr123"))
                .role(Role.HR)
                .build();

        userRepo.save(admin);
        userRepo.save(hr1);
        userRepo.save(hr2);

        log.info("✅ Users seeded");
    }

    // ─────────────────────────────────────────
    // CANDIDATES
    // ─────────────────────────────────────────
    private void seedCandidates() {

        User hr1 = userRepo.findByEmail("sokha@hrapp.com").orElseThrow();
        User hr2 = userRepo.findByEmail("dara@hrapp.com").orElseThrow();

        // Tech Candidates
        Candidate c1 = Candidate.builder()
                .name("Cheng Chaylim")
                .email("chaylim@gmail.com")
                .phone("+855 85 382 962")
                .domain("tech")
                .position("Software Engineer")
                .expYears(2)
                .skills("[\"Laravel\", \"Flutter\", \"React\", \"Node.js\", \"Firebase\"]")
                .stack("[\"Laravel\", \"Node.js\", \"Flask\", \"Firebase\", \"MySQL\"]")
                .fileName("cheng_chaylim_cv.pdf")
                .filePath("uploads/cheng_chaylim_cv.pdf")
                .cvRaw("Cheng Chaylim Software Engineer CADT Cambodia...")
                .cvJson("""
                        {
                          "name": "Cheng Chaylim",
                          "domain": "tech",
                          "position": "Software Engineer",
                          "exp_years": 2,
                          "skills": ["Laravel", "Flutter", "React", "Node.js"],
                          "stack": ["Laravel", "Node.js", "Firebase", "MySQL"],
                          "roles": ["Full Stack Dev|CADT Project|2yr|built rental system"],
                          "projects": ["UnitNest|lead|Flutter,Node.js|automated rental payments"],
                          "awards": ["Flutter Excellence|2025|1st place"],
                          "langs": ["Khmer|native", "English|fluent"]
                        }
                        """)
                .uploadedBy(hr1)
                .build();

        Candidate c2 = Candidate.builder()
                .name("Sophea Keo")
                .email("sophea.keo@gmail.com")
                .phone("+855 12 345 678")
                .domain("tech")
                .position("Backend Developer")
                .expYears(3)
                .skills("[\"Spring Boot\", \"Java\", \"Python\", \"REST API\", \"Docker\"]")
                .stack("[\"Spring Boot\", \"PostgreSQL\", \"Docker\", \"Redis\"]")
                .fileName("sophea_keo_cv.pdf")
                .filePath("uploads/sophea_keo_cv.pdf")
                .cvRaw("Sophea Keo Backend Developer 3 years experience Java Spring Boot...")
                .cvJson("""
                        {
                          "name": "Sophea Keo",
                          "domain": "tech",
                          "position": "Backend Developer",
                          "exp_years": 3,
                          "skills": ["Spring Boot", "Java", "Python", "REST API", "Docker"],
                          "stack": ["Spring Boot", "PostgreSQL", "Docker", "Redis"],
                          "roles": ["Backend Dev|Tech Co|3yr|built microservices"],
                          "projects": ["E-Commerce API|backend|Spring Boot,Redis|handled 10k req/s"],
                          "awards": [],
                          "langs": ["Khmer|native", "English|intermediate"]
                        }
                        """)
                .uploadedBy(hr1)
                .build();

        Candidate c3 = Candidate.builder()
                .name("Ratanak Lim")
                .email("ratanak.lim@gmail.com")
                .phone("+855 96 111 222")
                .domain("tech")
                .position("Mobile Developer")
                .expYears(2)
                .skills("[\"Flutter\", \"Dart\", \"Firebase\", \"REST API\", \"Git\"]")
                .stack("[\"Flutter\", \"Firebase\", \"Dart\"]")
                .fileName("ratanak_lim_cv.pdf")
                .filePath("uploads/ratanak_lim_cv.pdf")
                .cvRaw("Ratanak Lim Mobile Developer Flutter Firebase...")
                .cvJson("""
                        {
                          "name": "Ratanak Lim",
                          "domain": "tech",
                          "position": "Mobile Developer",
                          "exp_years": 2,
                          "skills": ["Flutter", "Dart", "Firebase", "REST API"],
                          "stack": ["Flutter", "Firebase", "Dart"],
                          "roles": ["Mobile Dev|Startup|2yr|built 3 apps on Play Store"],
                          "projects": ["Food Delivery App|solo|Flutter,Firebase|5000+ downloads"],
                          "awards": [],
                          "langs": ["Khmer|native", "English|basic"]
                        }
                        """)
                .uploadedBy(hr2)
                .build();

        // Sales Candidate
        Candidate c4 = Candidate.builder()
                .name("Pisey Noun")
                .email("pisey.noun@gmail.com")
                .phone("+855 77 999 888")
                .domain("sales")
                .position("Sales Manager")
                .expYears(5)
                .skills("[\"B2B Sales\", \"Negotiation\", \"CRM\", \"Cold Calling\", \"Team Leadership\"]")
                .stack("[]")
                .fileName("pisey_noun_cv.pdf")
                .filePath("uploads/pisey_noun_cv.pdf")
                .cvRaw("Pisey Noun Sales Manager 5 years B2B sales experience...")
                .cvJson("""
                        {
                          "name": "Pisey Noun",
                          "domain": "sales",
                          "position": "Sales Manager",
                          "exp_years": 5,
                          "skills": ["B2B Sales", "Negotiation", "CRM", "Cold Calling"],
                          "tools": ["Salesforce", "HubSpot", "Excel"],
                          "roles": ["Sales Manager|ABC Corp|3yr|grew revenue 40%"],
                          "achievements": ["$2M quota 2023", "Top seller Q3 2022"],
                          "channels": ["cold calling", "LinkedIn", "referrals"],
                          "metrics": ["$2M revenue", "120% quota attainment"],
                          "langs": ["Khmer|native", "English|fluent"]
                        }
                        """)
                .uploadedBy(hr2)
                .build();

        // Marketing Candidate
        Candidate c5 = Candidate.builder()
                .name("Lina Chan")
                .email("lina.chan@gmail.com")
                .phone("+855 89 555 444")
                .domain("marketing")
                .position("Digital Marketing Specialist")
                .expYears(3)
                .skills("[\"SEO\", \"Content Marketing\", \"Meta Ads\", \"Google Analytics\", \"Canva\"]")
                .stack("[]")
                .fileName("lina_chan_cv.pdf")
                .filePath("uploads/lina_chan_cv.pdf")
                .cvRaw("Lina Chan Digital Marketing Specialist 3 years SEO Content...")
                .cvJson("""
                        {
                          "name": "Lina Chan",
                          "domain": "marketing",
                          "position": "Digital Marketing Specialist",
                          "exp_years": 3,
                          "skills": ["SEO", "Content Marketing", "Meta Ads", "Google Analytics"],
                          "tools": ["Canva", "Meta Ads Manager", "Google Analytics"],
                          "roles": ["Digital Marketer|XYZ Brand|2yr|grew IG 200%"],
                          "achievements": ["500K impressions campaign", "35% CTR improvement"],
                          "channels": ["SEO", "paid social", "email marketing"],
                          "metrics": ["200% follower growth", "35% CTR"],
                          "langs": ["Khmer|native", "English|intermediate"]
                        }
                        """)
                .uploadedBy(hr1)
                .build();

        candidateRepo.save(c1);
        candidateRepo.save(c2);
        candidateRepo.save(c3);
        candidateRepo.save(c4);
        candidateRepo.save(c5);

        log.info("✅ Candidates seeded");
    }

    // ─────────────────────────────────────────
    // JOB DESCRIPTIONS
    // ─────────────────────────────────────────
    private void seedJobDescriptions() {

        User hr1 = userRepo.findByEmail("sokha@hrapp.com").orElseThrow();
        User hr2 = userRepo.findByEmail("dara@hrapp.com").orElseThrow();

        JobDescription jd1 = JobDescription.builder()
                .title("Senior Flutter Developer")
                .field("tech")
                .position("Mobile Developer")
                .requiredSkills("[\"Flutter\", \"Dart\", \"Firebase\", \"REST API\"]")
                .minExpYears(2)
                .description("""
                        We are looking for an experienced Flutter Developer to join our team.
                        You will be responsible for building and maintaining mobile applications.
                        Requirements: 2+ years Flutter experience, strong Dart knowledge,
                        experience with Firebase and REST APIs.
                        """)
                .createdBy(hr1)
                .build();

        JobDescription jd2 = JobDescription.builder()
                .title("Backend Java Developer")
                .field("tech")
                .position("Backend Developer")
                .requiredSkills("[\"Spring Boot\", \"Java\", \"MySQL\", \"REST API\"]")
                .minExpYears(2)
                .description("""
                        Looking for a Backend Java Developer with Spring Boot experience.
                        You will design and develop RESTful APIs and microservices.
                        Requirements: 2+ years Java, Spring Boot, relational databases.
                        """)
                .createdBy(hr2)
                .build();

        JobDescription jd3 = JobDescription.builder()
                .title("Sales Manager")
                .field("sales")
                .position("Sales Manager")
                .requiredSkills("[\"B2B Sales\", \"CRM\", \"Negotiation\", \"Team Leadership\"]")
                .minExpYears(3)
                .description("""
                        We need a results-driven Sales Manager to lead our sales team.
                        Requirements: 3+ years B2B sales, CRM experience, proven track record
                        of hitting quotas and leading teams.
                        """)
                .createdBy(hr1)
                .build();

        jobDescriptionRepo.save(jd1);
        jobDescriptionRepo.save(jd2);
        jobDescriptionRepo.save(jd3);

        log.info("✅ Job Descriptions seeded");
    }

    // ─────────────────────────────────────────
    // MATCH RESULTS
    // ─────────────────────────────────────────
    private void seedMatchResults() {

        JobDescription jd1 = jobDescriptionRepo.findAll().get(0); // Flutter JD
        JobDescription jd2 = jobDescriptionRepo.findAll().get(1); // Java JD
        JobDescription jd3 = jobDescriptionRepo.findAll().get(2); // Sales JD

        Candidate c1 = candidateRepo.findAll().get(0); // Chaylim
        Candidate c2 = candidateRepo.findAll().get(1); // Sophea
        Candidate c3 = candidateRepo.findAll().get(2); // Ratanak
        Candidate c4 = candidateRepo.findAll().get(3); // Pisey
        Candidate c5 = candidateRepo.findAll().get(4); // Lina

        // Flutter JD results
        MatchResult mr1 = MatchResult.builder()
                .jobDescription(jd1)
                .candidate(c3)          // Ratanak — best Flutter match
                .matchScore(92)
                .matchReasons("[\"2yr Flutter experience\", \"3 apps on Play Store\", \"Firebase expert\"]")
                .gaps("[\"no Dart certification\"]")
                .build();

        MatchResult mr2 = MatchResult.builder()
                .jobDescription(jd1)
                .candidate(c1)          // Chaylim — good Flutter match
                .matchScore(78)
                .matchReasons("[\"Flutter experience\", \"strong full stack background\"]")
                .gaps("[\"Flutter not primary skill\", \"limited mobile-only experience\"]")
                .build();

        // Java JD results
        MatchResult mr3 = MatchResult.builder()
                .jobDescription(jd2)
                .candidate(c2)          // Sophea — best Java match
                .matchScore(95)
                .matchReasons("[\"3yr Spring Boot\", \"Docker experience\", \"built microservices\"]")
                .gaps("[\"no AWS experience\"]")
                .build();

        // Sales JD results
        MatchResult mr4 = MatchResult.builder()
                .jobDescription(jd3)
                .candidate(c4)          // Pisey — best Sales match
                .matchScore(90)
                .matchReasons("[\"5yr B2B sales\", \"$2M quota achieved\", \"Salesforce CRM\"]")
                .gaps("[\"no formal sales certification\"]")
                .build();

        matchResultRepo.save(mr1);
        matchResultRepo.save(mr2);
        matchResultRepo.save(mr3);
        matchResultRepo.save(mr4);

        log.info("✅ Match Results seeded");
    }

    // ─────────────────────────────────────────
    // CHAT MESSAGES
    // ─────────────────────────────────────────
    private void seedChatMessages() {

        User hr1 = userRepo.findByEmail("sokha@hrapp.com").orElseThrow();
        JobDescription jd1 = jobDescriptionRepo.findAll().get(0);

        // Simulated chat conversation
        ChatMessage msg1 = ChatMessage.builder()
                .user(hr1)
                .jobDescription(jd1)
                .role(MessageRole.HR)
                .message("Show me candidates with Flutter experience")
                .build();

        ChatMessage msg2 = ChatMessage.builder()
                .user(hr1)
                .jobDescription(jd1)
                .role(MessageRole.ASSISTANT)
                .message("""
                        I found 2 candidates with Flutter experience:
                        1. Ratanak Lim — Match Score: 92% — 2yr Flutter, 3 apps on Play Store
                        2. Cheng Chaylim — Match Score: 78% — Flutter experience, strong full stack
                        """)
                .build();

        ChatMessage msg3 = ChatMessage.builder()
                .user(hr1)
                .jobDescription(jd1)
                .role(MessageRole.HR)
                .message("Who has the most experience overall?")
                .build();

        ChatMessage msg4 = ChatMessage.builder()
                .user(hr1)
                .jobDescription(jd1)
                .role(MessageRole.ASSISTANT)
                .message("""
                        Based on all candidates in the system:
                        1. Pisey Noun — 5 years (Sales)
                        2. Sophea Keo — 3 years (Backend)
                        3. Lina Chan — 3 years (Marketing)
                        4. Cheng Chaylim — 2 years (Full Stack)
                        5. Ratanak Lim — 2 years (Mobile)
                        """)
                .build();

        chatMessageRepo.save(msg1);
        chatMessageRepo.save(msg2);
        chatMessageRepo.save(msg3);
        chatMessageRepo.save(msg4);

        log.info("✅ Chat Messages seeded");
    }
}
//
//```
//
//        ---
//
//        ## What Gets Created on First Run
//```
//Users (3)
//├── admin@hrapp.com   → ADMIN  / password: admin123
//├── sokha@hrapp.com   → HR     / password: hr123
//└── dara@hrapp.com    → HR     / password: hr123
//
//Candidates (5)
//├── Cheng Chaylim     → tech    / Full Stack
//├── Sophea Keo        → tech    / Backend
//├── Ratanak Lim       → tech    / Mobile
//├── Pisey Noun        → sales   / Sales Manager
//└── Lina Chan         → marketing / Digital Marketing
//
//Job Descriptions (3)
//├── Senior Flutter Developer
//├── Backend Java Developer
//└── Sales Manager
//
//Match Results (4)
//├── Flutter JD → Ratanak (92%) + Chaylim (78%)
//├── Java JD    → Sophea (95%)
//└── Sales JD   → Pisey (90%)
//
//Chat Messages (4)
//└── Simulated HR conversation about Flutter candidates