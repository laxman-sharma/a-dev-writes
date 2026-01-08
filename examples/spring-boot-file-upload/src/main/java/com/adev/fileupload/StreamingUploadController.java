package com.adev.fileupload;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class StreamingUploadController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StreamingUploadController.class);

    /**
     * Handles file upload via streaming.
     * Unlike @RequestParam("file") MultipartFile, this method reads the standard InputStream
     * from the ServletRequest preventing the server from loading the entire file into memory (Heap).
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<String> uploadFile(HttpServletRequest request) {
        // 1. Check if the request contains multipart content
        boolean isMultipart = JakartaServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not a multipart request");
        }

        // 2. Create a new ServletFileUpload instance
        JakartaServletFileUpload upload = new JakartaServletFileUpload();

        try {
            // 3. Parse the request directly using streams
            FileItemInputIterator iter = upload.getItemIterator(request);

            while (iter.hasNext()) {
                FileItemInput item = iter.next();
                InputStream stream = item.getInputStream();

                if (!item.isFormField()) {
                    // This is a file part
                    String filename = item.getName();
                    log.info("Start processing file: {}", filename);
                    
                    // 4. Process the stream
                    
                    long size = streamToDb(stream, filename);

                    log.info("Finished processing file: {}. Size: {} bytes", filename, size);
                } else {
                    // Process form fields if any (metadata)
                }
            }
            return ResponseEntity.ok("File uploaded successfully via streaming!");

        } catch (Exception e) {
            log.error("Error streaming file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing upload: " + e.getMessage());
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private ProductRepository productRepository;

    /**
     * Reads from InputStream, parses CSV lines, and saves to H2 in batches.
     */
    private long streamToDb(InputStream inputStream, String originalFilename) throws Exception {
        long totalBytes = 0;
        int batchSize = 1000;
        java.util.List<Product> batch = new java.util.ArrayList<>(batchSize);
        
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                totalBytes += line.length(); // Approximation
                
                // Simple CSV parsing (Assuming Format: Name,Description,Price)
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    Product p = new Product(parts[0].trim(), parts[1].trim(), Double.parseDouble(parts[2].trim()));
                    batch.add(p);
                }

                if (batch.size() >= batchSize) {
                    productRepository.saveAll(batch);
                    productRepository.flush(); // Force write to DB
                    batch.clear();
                    
                    // Log memory snapshot
                    long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    log.info("Persisted {} records... [Heap Used: {} MB]", productRepository.count(), usedMemory / 1024 / 1024);
                }
            }
            // Save remaining
            if (!batch.isEmpty()) {
                productRepository.saveAll(batch);
            }
            return totalBytes;
        }
    }
}
