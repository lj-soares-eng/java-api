package com.example.usersapi.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createUser_returns201WithoutPlainPassword() throws Exception {
        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "name": "Jane Doe",
                                          "email": "jane@example.com",
                                          "password": "securepass123",
                                          "role": "USER"
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.passwordHash").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void createUser_returns400WhenInvalid() throws Exception {
        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "name": "",
                                          "email": "not-an-email",
                                          "password": "short",
                                          "role": "USER"
                                        }
                                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_returns409OnDuplicateEmail() throws Exception {
        String body =
                """
                {
                  "name": "Jane Doe",
                  "email": "duplicate@example.com",
                  "password": "securepass123",
                  "role": "USER"
                }
                """;

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void listAndGetUsers_includePasswordHashNotPlainPassword() throws Exception {
        MvcResult created =
                mockMvc.perform(
                                post("/api/users")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                                                {
                                                  "name": "List User",
                                                  "email": "list@example.com",
                                                  "password": "securepass123",
                                                  "role": "ADMIN"
                                                }
                                                """))
                        .andExpect(status().isCreated())
                        .andReturn();

        String responseBody = created.getResponse().getContentAsString();
        String id = responseBody.replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].passwordHash").isNotEmpty())
                .andExpect(jsonPath("$[0].password").doesNotExist());

        mockMvc.perform(get("/api/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passwordHash").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getUser_returns404WhenMissing() throws Exception {
        mockMvc.perform(get("/api/users/99999")).andExpect(status().isNotFound());
    }

    @Test
    void updateUser_returns200WithoutPlainPassword() throws Exception {
        MvcResult created =
                mockMvc.perform(
                                post("/api/users")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                                                {
                                                  "name": "Update User",
                                                  "email": "update@example.com",
                                                  "password": "securepass123",
                                                  "role": "USER"
                                                }
                                                """))
                        .andExpect(status().isCreated())
                        .andReturn();

        String id = created.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(
                        put("/api/users/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "name": "Updated Name",
                                          "email": "update@example.com",
                                          "role": "ADMIN"
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").isNotEmpty());
    }

    @Test
    void deleteUser_returns204() throws Exception {
        MvcResult created =
                mockMvc.perform(
                                post("/api/users")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                                                {
                                                  "name": "Delete User",
                                                  "email": "delete@example.com",
                                                  "password": "securepass123",
                                                  "role": "USER"
                                                }
                                                """))
                        .andExpect(status().isCreated())
                        .andReturn();

        String id = created.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(delete("/api/users/" + id)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/" + id)).andExpect(status().isNotFound());
    }
}
