package com.dms.service;

import com.dms.jackson.model.TransactionEvent;
import com.dms.util.JSONUtil;
import io.vertx.core.json.JsonObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.*;

public class EncryptionServiceTest {

    public static final String TRANSACTIONS_DIR = "/transactions";

    private Logger logger = LoggerFactory.getLogger(EncryptionServiceTest.class);

    private EncryptionService encryptionService = new EncryptionService();

    @Test
    public void testEncryptDecrypt() throws Exception {
        String plain = "simple plain text";
        String secret = "another secret";
        String encrypted = encryptionService.encrypt(plain, secret);
        String decrypted = encryptionService.decrypt(encrypted, secret);

        assertEquals(plain, decrypted);
    }

    @Test
    public void testDecryptTransactions() throws Exception {
        Path dir = Paths.get(getClass().getResource(TRANSACTIONS_DIR).toURI());
        for (Path path : Files.newDirectoryStream(dir, "*.json")) {
            logger.debug("Processing file {}", path.getFileName());
            String message = new String(Files.readAllBytes(path));

            Optional<TransactionEvent> transactionEvent = JSONUtil.parse(message, TransactionEvent.class);
            assertTrue(transactionEvent.isPresent());

            String payload = transactionEvent.get().getExtraData().getPayload();
            assertNotNull(payload);

            String decrypted = encryptionService.decrypt(payload, EncryptionService.DEFAULT_SECRET);
            logger.debug("Decrypted payload:\n{}", decrypted);

            JsonObject json = new JsonObject(decrypted);
            assertFalse(json.isEmpty());
        }
    }
}
