package com.financialplanner.controller;

import com.financialplanner.dto.JsonImportDTO;
import com.financialplanner.service.JsonImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/import")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class JsonImportController {

    private final JsonImportService jsonImportService;

    /**
     * Import transactions from JSON file upload
     * POST /api/v1/import/file
     */
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JsonImportDTO.ImportResponse> importFromFile(
            @RequestParam("file") MultipartFile file) {

        log.info("Received file upload: {} ({}  bytes)",
                file.getOriginalFilename(), file.getSize());

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    createErrorResponse("File is empty")
            );
        }

        if (!file.getOriginalFilename().endsWith(".json")) {
            return ResponseEntity.badRequest().body(
                    createErrorResponse("Only JSON files are accepted")
            );
        }

        // Process import
        JsonImportDTO.ImportResponse response = jsonImportService.importFromFile(file);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
    }

    /**
     * Import transactions from JSON in request body
     * POST /api/v1/import/data
     */
    @PostMapping(value = "/data", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonImportDTO.ImportResponse> importFromJson(
            @Valid @RequestBody JsonImportDTO.ImportRequest request) {

        log.info("Received JSON import request for user: {}", request.getUser().getUserId());

        // Validate request
        List<String> validationErrors = jsonImportService.validateImportRequest(request);
        if (!validationErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    createValidationErrorResponse(validationErrors)
            );
        }

        // Process import
        JsonImportDTO.ImportResponse response = jsonImportService.importFromRequest(request);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
    }

    /**
     * Webhook endpoint for Digital Wallet integration
     * POST /api/v1/import/webhook
     * 
     * This endpoint can be called by the Digital Wallet microservice
     * to automatically sync transaction data
     */
    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonImportDTO.ImportResponse> webhookImport(
            @Valid @RequestBody JsonImportDTO.ImportRequest request,
            @RequestHeader(value = "X-Wallet-Api-Key", required = false) String apiKey) {

        log.info("Received webhook import from Digital Wallet for user: {}",
                request.getUser().getUserId());

        // TODO: Validate API key for security
        // if (!isValidApiKey(apiKey)) {
        //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        // }

        // Validate request
        List<String> validationErrors = jsonImportService.validateImportRequest(request);
        if (!validationErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    createValidationErrorResponse(validationErrors)
            );
        }

        // Process import
        JsonImportDTO.ImportResponse response = jsonImportService.importFromRequest(request);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
    }

    /**
     * Get import instructions and example JSON format
     * GET /api/v1/import/example
     */
    @GetMapping("/example")
    public ResponseEntity<Map<String, Object>> getImportExample() {
        Map<String, Object> example = new HashMap<>();

        example.put("description", "Example JSON format for importing transactions");
        example.put("format", createExampleFormat());
        example.put("endpoints", Map.of(
                "file_upload", "POST /api/v1/import/file (multipart/form-data)",
                "json_data", "POST /api/v1/import/data (application/json)",
                "webhook", "POST /api/v1/import/webhook (for Digital Wallet integration)"
        ));

        return ResponseEntity.ok(example);
    }

    /**
     * Health check for import service
     * GET /api/v1/import/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "JSON Import Service");
        health.put("endpoints", "file, data, webhook");
        return ResponseEntity.ok(health);
    }

    /**
     * Create example JSON format
     */
    private Map<String, Object> createExampleFormat() {
        return Map.of(
                "user", Map.of(
                        "userId", "user123",
                        "username", "john_doe",
                        "email", "john@example.com",
                        "walletId", "wallet456"
                ),
                "transactions", List.of(
                        Map.of(
                                "transactionId", "tx001",
                                "description", "Morning coffee",
                                "merchantName", "Starbucks",
                                "amount", 5.99,
                                "transactionDate", "2024-11-08T09:30:00",
                                "walletId", "wallet456",
                                "transactionType", "DEBIT"
                        ),
                        Map.of(
                                "transactionId", "tx002",
                                "description", "Grocery shopping",
                                "merchantName", "Whole Foods",
                                "amount", 156.30,
                                "transactionDate", "2024-11-07T18:45:00",
                                "walletId", "wallet456",
                                "transactionType", "DEBIT"
                        )
                ),
                "metadata", Map.of(
                        "source", "digital_wallet",
                        "version", "1.0",
                        "periodStart", "2024-11-01",
                        "periodEnd", "2024-11-30"
                )
        );
    }

    /**
     * Create error response
     */
    private JsonImportDTO.ImportResponse createErrorResponse(String message) {
        return JsonImportDTO.ImportResponse.builder()
                .success(false)
                .message(message)
                .errors(List.of(message))
                .build();
    }

    /**
     * Create validation error response
     */
    private JsonImportDTO.ImportResponse createValidationErrorResponse(List<String> errors) {
        return JsonImportDTO.ImportResponse.builder()
                .success(false)
                .message("Validation failed")
                .errors(errors)
                .build();
    }
}
