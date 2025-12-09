package com.ia.knowledgeai.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.ia.knowledgeai.controller.impl.HealthControllerImpl;
import com.ia.knowledgeai.service.impl.HealthServiceImpl;

@WebMvcTest(controllers = HealthControllerImpl.class)
@Import(HealthServiceImpl.class)
class HealthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldReturnUpStatus() throws Exception {
		mockMvc.perform(get("/api/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.timestamp").value(notNullValue()));
	}
}
