package com.gametime.Backend;

import com.gametime.Backend.config.SessionAuthorization;
import com.gametime.Backend.entity.User;
import com.gametime.Backend.repository.TournamentRepository;
import com.gametime.Backend.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthAndAdminApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void signupCreatesSessionAndStoresEncodedPassword() throws Exception {
        String email = "signup-" + System.nanoTime() + "@example.com";
        MvcResult signupResult = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Test User",
                                "email", email,
                                "password", "StrongPass123!"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn();

        User user = userRepository.findByEmail(email).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(user.getPassword().startsWith("$2"));

        mockMvc.perform(get("/api/auth/me").session((MockHttpSession) signupResult.getRequest().getSession(false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email.toUpperCase(Locale.ROOT),
                                "password", "StrongPass123!",
                                "remember", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andReturn();

        mockMvc.perform(get("/api/auth/me").session((MockHttpSession) loginResult.getRequest().getSession(false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void adminCanCreateTournamentWithQuestionsWithoutExposingAnswersPublicly() throws Exception {
        User admin = userRepository.save(User.builder()
                .name("Test Admin")
                .email("admin-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("StrongPass123!"))
                .role(User.UserRole.ADMIN)
                .build());
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionAuthorization.USER_ID_ATTRIBUTE, admin.getId());

        List<Map<String, Object>> questions = List.of(
                question("Question one", 1),
                question("Question two", 2),
                question("Question three", 0),
                question("Question four", 3),
                question("Question five", 1)
        );
        Map<String, Object> request = Map.of(
                "title", "Test Questions Tournament",
                "category", "Science",
                "prizePool", 100,
                "entryFeeNexus", 0,
                "maxPlayers", 10,
                "startsAt", LocalDateTime.now().plusDays(1).withNano(0).toString(),
                "questionCount", 5,
                "description", "",
                "questions", questions
        );

        MvcResult createResult = mockMvc.perform(post("/api/admin/tournaments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        long tournamentId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();
        org.junit.jupiter.api.Assertions.assertEquals(5,
                tournamentRepository.findById(tournamentId).orElseThrow().getQuestions().size());

        mockMvc.perform(get("/api/tournaments/{id}/questions", tournamentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].correctAnswerIndex").doesNotExist());

        mockMvc.perform(get("/api/admin/tournaments/{id}/questions", tournamentId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correctAnswerIndex").value(1));
    }

    @Test
    void regularUserCannotCreateAdminTournament() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Regular User")
                .email("user-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("StrongPass123!"))
                .build());
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionAuthorization.USER_ID_ATTRIBUTE, user.getId());

        mockMvc.perform(post("/api/admin/tournaments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Unauthorized Tournament",
                                "category", "Science",
                                "prizePool", 100,
                                "entryFeeNexus", 0,
                                "maxPlayers", 10,
                                "startsAt", LocalDateTime.now().plusDays(1).withNano(0).toString(),
                                "questionCount", 5,
                                "description", "",
                                "questions", List.of(
                                        question("Question one", 1),
                                        question("Question two", 2),
                                        question("Question three", 0),
                                        question("Question four", 3),
                                        question("Question five", 1)
                                )
                        ))))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyAdminsCanCreateUserAccounts() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Regular User")
                .email("regular-create-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("StrongPass123!"))
                .build());
        User admin = userRepository.save(User.builder()
                .name("Account Admin")
                .email("account-admin-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("StrongPass123!"))
                .role(User.UserRole.ADMIN)
                .build());
        MockHttpSession userSession = new MockHttpSession();
        userSession.setAttribute(SessionAuthorization.USER_ID_ATTRIBUTE, user.getId());
        MockHttpSession adminSession = new MockHttpSession();
        adminSession.setAttribute(SessionAuthorization.USER_ID_ATTRIBUTE, admin.getId());
        Map<String, Object> request = Map.of(
                "name", "Admin Created User",
                "email", "created-" + System.nanoTime() + "@example.com",
                "password", "StrongPass123!"
        );

        mockMvc.perform(post("/api/admin/users")
                        .session(userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/users")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.name").value("Admin Created User"));
    }

    @Test
    void approvedInrTopUpFundsWalletAndUserCanJoinTournament() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Wallet User")
                .email("wallet-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("StrongPass123!"))
                .build());
        User admin = userRepository.save(User.builder()
                .name("Wallet Admin")
                .email("wallet-admin-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("StrongPass123!"))
                .role(User.UserRole.ADMIN)
                .build());
        MockHttpSession userSession = new MockHttpSession();
        userSession.setAttribute(SessionAuthorization.USER_ID_ATTRIBUTE, user.getId());
        MockHttpSession adminSession = new MockHttpSession();
        adminSession.setAttribute(SessionAuthorization.USER_ID_ATTRIBUTE, admin.getId());

        String transactionId = "TXN-" + System.nanoTime();
        MvcResult topUpResult = mockMvc.perform(post("/api/wallet/topups")
                        .session(userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "amountInr", 250,
                                "paymentMethod", "UPI",
                                "transactionId", transactionId
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        long topUpId = objectMapper.readTree(topUpResult.getResponse().getContentAsString())
                .get("id").asLong();

        Map<String, Object> tournamentRequest = tournamentRequest("Wallet Join Tournament", 100);
        MvcResult createResult = mockMvc.perform(post("/api/admin/tournaments")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tournamentRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        long tournamentId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        Map<String, Object> edit = new HashMap<>(tournamentRequest);
        edit.put("title", "Updated Wallet Join Tournament");
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                                "/api/admin/tournaments/{id}", tournamentId)
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(edit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Wallet Join Tournament"));

        mockMvc.perform(post("/api/admin/wallet/topups/{id}/approve", topUpId).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
        mockMvc.perform(post("/api/admin/wallet/topups/{id}/approve", topUpId).session(adminSession))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/tournaments/{id}/join", tournamentId).session(userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPlayers").value(1));
        mockMvc.perform(get("/api/tournaments/mine").session(userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(tournamentId));
        mockMvc.perform(get("/api/auth/me").session(userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inrBalance").value(150));

        MvcResult rejectedTopUpResult = mockMvc.perform(post("/api/wallet/topups")
                        .session(userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "amountInr", 30,
                                "paymentMethod", "UPI",
                                "transactionId", transactionId + "-REJECTED"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
        long rejectedTopUpId = objectMapper.readTree(rejectedTopUpResult.getResponse().getContentAsString())
                .get("id").asLong();
        mockMvc.perform(post("/api/admin/wallet/topups/{id}/reject", rejectedTopUpId).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
        mockMvc.perform(get("/api/auth/me").session(userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inrBalance").value(150));

        edit.put("title", "Edit After Join");
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                                "/api/admin/tournaments/{id}", tournamentId)
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(edit)))
                .andExpect(status().isConflict());
    }

    private Map<String, Object> question(String text, int answerIndex) {
        return Map.of(
                "text", text,
                "options", List.of("A", "B", "C", "D"),
                "correctAnswerIndex", answerIndex,
                "timeLimitSeconds", 10
        );
    }

    private Map<String, Object> tournamentRequest(String title, long entryFeeInr) {
        return Map.of(
                "title", title,
                "category", "Science",
                "prizePool", 100,
                "entryFeeNexus", entryFeeInr,
                "maxPlayers", 10,
                "startsAt", LocalDateTime.now().plusDays(1).withNano(0).toString(),
                "questionCount", 5,
                "description", "",
                "questions", List.of(
                        question("Question one", 1),
                        question("Question two", 2),
                        question("Question three", 0),
                        question("Question four", 3),
                        question("Question five", 1)
                )
        );
    }
}
