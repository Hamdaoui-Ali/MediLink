package com.medilink.medilink_backend.patient.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class PatientRegistrationIntegrationTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Test
	void registerCreatesPatientWithMigratedLocalDatabase() throws Exception {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		String email = "patient." + UUID.randomUUID() + "@example.com";

		mockMvc.perform(post("/v1/patients/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "fullName": "Jane Patient",
								  "email": "%s",
								  "password": "Patient@123",
								  "phoneNumber": "+15551234567",
								  "dateOfBirth": "1990-03-05",
								  "gender": "FEMALE",
								  "address": "100 Care Street"
								}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success", is(true)))
				.andExpect(jsonPath("$.data.email", is(email)))
				.andExpect(jsonPath("$.data.fullName", is("Jane Patient")));
	}

	@Test
	void registeredPatientCanLoginWithSameCredentials() throws Exception {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		String email = "patient." + UUID.randomUUID() + "@example.com";

		mockMvc.perform(post("/v1/patients/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "fullName": "Jane Patient",
								  "email": "%s",
								  "password": "Patient@123",
								  "phoneNumber": "+15551234567",
								  "dateOfBirth": "1990-03-05",
								  "gender": "FEMALE",
								  "address": "100 Care Street"
								}
								""".formatted(email)))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "Patient@123"
								}
								""".formatted(email)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success", is(true)))
				.andExpect(jsonPath("$.data.user.email", is(email)))
				.andExpect(jsonPath("$.data.user.role", is("PATIENT")));
	}
}
