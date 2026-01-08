package com.adev.fileupload;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
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
@Slf4j
public class StreamingUploadController {

    /**
     * Handles file upload via streaming.
     * Unlike @RequestParam("file") MultipartFile, this method reads the standard InputStream
     * from the ServletRequest preventing the server from loading the entire file into memory (Heap).
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<String> uploadFile(HttpServletRequest request) {
        // 1. Check if the request contains multipart content
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not a multipart request");
        }

        // 2. Create a new ServletFileUpload instance
        ServletFileUpload upload = new ServletFileUpload();

        try {
            // 3. Parse the request directly using streams
            FileItemIterator iter = upload.getItemIterator(request);

            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();

                if (!item.isFormField()) {
                    // This is a file part
                    String filename = item.getName();
                    log.info("Start processing file: {}", filename);
                    
                    // 4. Process the stream (e.g., pipe to file or S3)
                    // We artificially limit this demo to just counting bytes to prove consumption
                    // or write to a temp file on disk without heap buffering.
                    
                    long size = streamToFile(stream, filename);

                    log.info("Finished processing file: {}. Size: {} bytes", filename, size);
                } else {
                    // Process form fields if any (metadata)
                    // ...
                }
            }
            return ResponseEntity.ok("File uploaded successfully via streaming!");

        } catch (Exception e) {
            log.error("Error streaming file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing upload: " + e.getMessage());
        }
    }

    /**
     * Reads from InputStream and writes to a temp file using a small buffer.
     * This ensures only ~8KB of heap is used regardless of file size (1GB or 100GB).
     */
    private long streamToFile(InputStream inputStream, String originalFilename) throws Exception {
        Path tempFile = Files.createTempFile("upload-", "-" + UUID.randomUUID().toString());
        log.info("Streaming to temp file: {}", tempFile);
        
        try (OutputStream outputStream = Files.newOutputStream(tempFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            byte[] buffer = new byte[8192]; // 8KB buffer
            long totalBytes = 0;
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
                
                // Optional: Log progress every 100MB for demo
                if (totalBytes % (100 * 1024 * 1024) == 0) {
                     log.info("Processed {} MB...", totalBytes / 1024 / 1024);
                }
            }
            return totalBytes;
        }
    }
}
