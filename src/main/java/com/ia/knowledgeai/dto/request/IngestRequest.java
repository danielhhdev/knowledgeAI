package com.ia.knowledgeai.dto.request;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for document ingestion via multipart file.
 */
public class IngestRequest {

	@NotBlank(message = "Source is required")
	@Size(max = 100, message = "Source too long")
	private String source;

	@NotBlank(message = "Title is required")
	@Size(max = 255, message = "Title too long")
	private String title;

	private List<@Size(max = 50, message = "Tag too long") String> tags = new ArrayList<>();

	@NotNull(message = "File is required")
	private MultipartFile file;

	public IngestRequest() {
	}

	public IngestRequest(String source, String title, List<String> tags, MultipartFile file) {
		this.source = source;
		this.title = title;
		if (tags != null) {
			this.tags = tags;
		}
		this.file = file;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		if (tags == null) {
			this.tags = new ArrayList<>();
			return;
		}
		this.tags = tags;
	}

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public boolean hasFile() {
		return file != null && !file.isEmpty();
	}
}
